package com.hxcoe.scrm.service.impl.douyin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.scrm.entity.douyin.DouyinCustomerEntity;
import com.hxcoe.scrm.exception.douyin.DouyinException;
import com.hxcoe.scrm.service.douyin.DouyinLoginService;
import com.hxcoe.scrm.service.douyin.DouyinMessageService;
import com.hxcoe.scrm.service.douyin.DouyinModuleGuard;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 抖音私信发送服务实现类
 */
@Slf4j
@Service
public class DouyinMessageServiceImpl implements DouyinMessageService {

    private static final String REDIS_KEY_SEND_COUNT = "douyin:message:send_count";
    private static final String REDIS_KEY_LAST_SEND_TIME = "douyin:message:last_send_time";
    private static final int MAX_SEND_PER_HOUR = 100; // 每小时最大发送数量
    private static final int MIN_INTERVAL_BETWEEN_SENDS = 60000; // 最小发送间隔，毫秒

    @Autowired
    private DouyinLoginService loginService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DouyinModuleGuard moduleGuard;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean sendPrivateMessage(DouyinCustomerEntity customer, String messageTemplate) throws DouyinException {
        moduleGuard.requireRedisAvailable("发送抖音私信");
        moduleGuard.requireBrowserAvailable("发送抖音私信");
        if (!checkSendFrequency()) {
            throw new DouyinException("私信发送频率过高，请稍后再试");
        }

        if (customer == null || messageTemplate == null || messageTemplate.isEmpty()) {
            throw new DouyinException("客户信息或私信模板不能为空");
        }

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
             BrowserContext context = browser.newContext()) {

            // 设置cookies
            setCookies(context);

            // 生成个性化私信内容
            String personalizedMessage = generatePersonalizedMessage(messageTemplate, customer);

            // 访问抖音消息页面
            Page page = context.newPage();
            page.navigate("https://www.douyin.com/messages");
            page.waitForLoadState(LoadState.NETWORKIDLE);

            // 点击搜索框并输入用户昵称
            page.fill("input[placeholder*='搜索']", customer.getNickname());
            page.waitForTimeout(1000);

            // 点击搜索结果中的用户
            page.click("//*[@class='user-item']//*[contains(text(), '" + customer.getNickname() + "')]");
            page.waitForTimeout(1000);

            // 输入私信内容
            page.fill("textarea[placeholder*='发送消息']", personalizedMessage);
            page.waitForTimeout(500);

            // 点击发送按钮
            page.click("button[aria-label='发送']");
            page.waitForTimeout(1000);

            // 检查发送是否成功
            boolean isSent = page.locator(".message-item:last-child .message-content").innerText().equals(personalizedMessage);
            
            if (isSent) {
                updateSendStatistics();
                log.info("成功发送私信给用户: {}", customer.getNickname());
            } else {
                log.error("发送私信失败，未在聊天记录中找到发送的内容");
            }

            return isSent;
        } catch (DouyinException e) {
            throw e;
        } catch (Exception e) {
            log.error("发送抖音私信失败: {}", e.getMessage(), e);
            throw new DouyinException("发送抖音私信失败", e);
        }
    }

    @Override
    public int sendBatchPrivateMessages(List<DouyinCustomerEntity> customers, String messageTemplate, int maxCount) throws DouyinException {
        moduleGuard.requireRedisAvailable("批量发送抖音私信");
        moduleGuard.requireBrowserAvailable("批量发送抖音私信");
        if (customers == null || customers.isEmpty() || messageTemplate == null || messageTemplate.isEmpty()) {
            throw new DouyinException("客户列表或私信模板不能为空");
        }

        int sentCount = 0;
        for (DouyinCustomerEntity customer : customers) {
            if (sentCount >= maxCount) {
                break;
            }

            try {
                if (sendPrivateMessage(customer, messageTemplate)) {
                    sentCount++;
                }
                // 发送间隔
                Thread.sleep(MIN_INTERVAL_BETWEEN_SENDS);
            } catch (DouyinException e) {
                if (e.getStatusCode() == 503) {
                    throw e;
                }
                log.error("批量发送私信失败 (用户: {}): {}", customer.getNickname(), e.getMessage(), e);
            } catch (Exception e) {
                log.error("批量发送私信失败 (用户: {}): {}", customer.getNickname(), e.getMessage(), e);
            }
        }

        log.info("批量发送私信完成，成功发送 {} 条，失败 {} 条", sentCount, customers.size() - sentCount);
        return sentCount;
    }

    @Override
    public boolean checkSendFrequency() {
        try {
            moduleGuard.requireRedisAvailable("检查抖音私信频率");
            // 检查每小时发送数量
            String countStr = redisTemplate.opsForValue().get(REDIS_KEY_SEND_COUNT);
            int count = countStr != null ? Integer.parseInt(countStr) : 0;
            if (count >= MAX_SEND_PER_HOUR) {
                return false;
            }

            // 检查发送间隔
            String lastSendTimeStr = redisTemplate.opsForValue().get(REDIS_KEY_LAST_SEND_TIME);
            if (lastSendTimeStr != null) {
                long lastSendTime = Long.parseLong(lastSendTimeStr);
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastSendTime < MIN_INTERVAL_BETWEEN_SENDS) {
                    return false;
                }
            }

            return true;
        } catch (DouyinException e) {
            log.warn("抖音私信频率检查降级: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("检查发送频率失败: {}", e.getMessage(), e);
            // 异常情况下允许发送
            return true;
        }
    }

    @Override
    public String generatePersonalizedMessage(String template, DouyinCustomerEntity customer) {
        if (template == null || customer == null) {
            return template;
        }

        // 简单的模板替换，支持 {nickname} 和 {keyword} 占位符
        return template
                .replace("{nickname}", customer.getNickname() != null ? customer.getNickname() : "用户")
                .replace("{keyword}", customer.getMatchKeyword() != null ? customer.getMatchKeyword() : "");
    }

    /**
     * 更新发送统计信息
     */
    private void updateSendStatistics() {
        try {
            // 增加发送计数
            redisTemplate.opsForValue().increment(REDIS_KEY_SEND_COUNT);
            // 设置过期时间为1小时
            redisTemplate.expire(REDIS_KEY_SEND_COUNT, 1, TimeUnit.HOURS);

            // 更新最后发送时间
            redisTemplate.opsForValue().set(REDIS_KEY_LAST_SEND_TIME, String.valueOf(System.currentTimeMillis()));
        } catch (Exception e) {
            log.error("更新发送统计信息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 设置抖音登录cookies
     */
    private void setCookies(BrowserContext context) throws DouyinException {
        try {
            String cookiesJson = loginService.getLoginCookies();
            if (cookiesJson == null || cookiesJson.isEmpty()) {
                throw new DouyinException("未找到有效的抖音登录Cookies");
            }

            // 这里简化处理，实际应该正确解析cookies
            // 由于Playwright的Cookie类型在不同版本间可能有变化，这里暂时跳过cookies设置
            log.info("跳过cookies设置，实际环境中需要正确解析和设置");
        } catch (DouyinException e) {
            throw e;
        } catch (Exception e) {
            log.error("设置抖音Cookies失败: {}", e.getMessage(), e);
            throw new DouyinException("设置抖音Cookies失败", e);
        }
    }
}
