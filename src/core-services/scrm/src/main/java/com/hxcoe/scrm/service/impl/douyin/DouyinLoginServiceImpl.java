package com.hxcoe.scrm.service.impl.douyin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.scrm.dto.douyin.DouyinLoginStatusDTO;
import com.hxcoe.scrm.exception.douyin.DouyinException;
import com.hxcoe.scrm.service.douyin.DouyinLoginService;
import com.hxcoe.scrm.service.douyin.DouyinModuleGuard;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 抖音登录服务实现类
 */
@Slf4j
@Service
public class DouyinLoginServiceImpl implements DouyinLoginService {

    private static final String DOUYIN_LOGIN_URL = "https://www.douyin.com";
    private static final String REDIS_KEY_LOGIN_STATUS = "douyin:login:status:";
    private static final String REDIS_KEY_COOKIES = "douyin:login:cookies";
    private static final long QR_CODE_EXPIRE_TIME = 5 * 60; // 5分钟
    private static final long COOKIES_EXPIRE_TIME = 24 * 60 * 60; // 24小时

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DouyinModuleGuard moduleGuard;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public DouyinLoginStatusDTO generateLoginQRCode() throws DouyinException {
        moduleGuard.requireRedisAvailable("生成抖音登录二维码");
        moduleGuard.requireBrowserAvailable("生成抖音登录二维码");
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
             BrowserContext context = browser.newContext()) {

            // 创建新页面
            Page page = context.newPage();

            // 访问抖音登录页面
            page.navigate(DOUYIN_LOGIN_URL);
            page.waitForLoadState(LoadState.NETWORKIDLE);

            // 点击登录按钮
            page.click("button[aria-label='登录']");
            page.waitForTimeout(1000);

            // 切换到扫码登录
            page.click("text=扫码登录");
            page.waitForTimeout(1000);

            // 等待二维码出现
            page.waitForSelector("img[alt*='二维码']", new Page.WaitForSelectorOptions().setTimeout(10000.0));

            // 获取二维码图片URL
            String qrCodeUrl = page.locator("img[alt*='二维码']").getAttribute("src");
            if (qrCodeUrl == null || qrCodeUrl.isEmpty()) {
                throw new DouyinException("无法获取登录二维码");
            }

            // 生成会话ID
            String sessionId = UUID.randomUUID().toString();

            // 保存登录状态到Redis
            DouyinLoginStatusDTO statusDTO = new DouyinLoginStatusDTO();
            statusDTO.setLogged(false);
            statusDTO.setQrCodeUrl(qrCodeUrl);
            statusDTO.setSessionId(sessionId);
            statusDTO.setMessage("请使用抖音APP扫码登录");

            saveLoginStatus(sessionId, statusDTO);

            // 启动异步任务检查登录状态
            checkLoginStatusAsync(sessionId, context);

            return statusDTO;
        } catch (DouyinException e) {
            throw e;
        } catch (Exception e) {
            log.error("生成抖音登录二维码失败: {}", e.getMessage(), e);
            throw new DouyinException("生成抖音登录二维码失败", e);
        }
    }

    @Override
    public DouyinLoginStatusDTO checkLoginStatus(String sessionId) throws DouyinException {
        moduleGuard.requireRedisAvailable("检查抖音登录状态");
        try {
            String statusJson = redisTemplate.opsForValue().get(REDIS_KEY_LOGIN_STATUS + sessionId);
            if (statusJson == null) {
                DouyinLoginStatusDTO statusDTO = new DouyinLoginStatusDTO();
                statusDTO.setLogged(false);
                statusDTO.setMessage("登录会话已过期，请重新生成二维码");
                return statusDTO;
            }

            return objectMapper.readValue(statusJson, DouyinLoginStatusDTO.class);
        } catch (DouyinException e) {
            throw e;
        } catch (JsonProcessingException e) {
            log.error("解析登录状态失败: {}", e.getMessage(), e);
            throw new DouyinException("获取登录状态失败", e);
        }
    }

    @Override
    public DouyinLoginStatusDTO getCurrentLoginStatus() throws DouyinException {
        moduleGuard.requireRedisAvailable("获取当前抖音登录状态");
        try {
            String cookiesJson = redisTemplate.opsForValue().get(REDIS_KEY_COOKIES);
            if (cookiesJson == null || cookiesJson.isEmpty()) {
                DouyinLoginStatusDTO statusDTO = new DouyinLoginStatusDTO();
                statusDTO.setLogged(false);
                statusDTO.setMessage("未登录，请先扫码登录");
                return statusDTO;
            }

            // 检查cookies是否有效（简单检查）
            // 实际应通过访问抖音页面验证
            DouyinLoginStatusDTO statusDTO = new DouyinLoginStatusDTO();
            statusDTO.setLogged(true);
            statusDTO.setMessage("已登录");
            return statusDTO;
        } catch (DouyinException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取当前登录状态失败: {}", e.getMessage(), e);
            throw new DouyinException("获取当前登录状态失败", e);
        }
    }

    @Override
    public void saveLoginCookies(String cookies) throws DouyinException {
        moduleGuard.requireRedisAvailable("保存抖音登录 Cookies");
        try {
            redisTemplate.opsForValue().set(REDIS_KEY_COOKIES, cookies, COOKIES_EXPIRE_TIME, TimeUnit.SECONDS);
        } catch (DouyinException e) {
            throw e;
        } catch (Exception e) {
            log.error("保存登录Cookies失败: {}", e.getMessage(), e);
            throw new DouyinException("保存登录Cookies失败", e);
        }
    }

    @Override
    public String getLoginCookies() throws DouyinException {
        moduleGuard.requireRedisAvailable("获取抖音登录 Cookies");
        try {
            String cookies = redisTemplate.opsForValue().get(REDIS_KEY_COOKIES);
            if (cookies == null || cookies.isEmpty()) {
                throw new DouyinException("未找到有效的登录Cookies");
            }
            return cookies;
        } catch (DouyinException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取登录Cookies失败: {}", e.getMessage(), e);
            throw new DouyinException("获取登录Cookies失败", e);
        }
    }

    @Override
    public void logout() throws DouyinException {
        moduleGuard.requireRedisAvailable("抖音账号登出");
        try {
            // 删除登录状态和Cookies
            redisTemplate.delete(REDIS_KEY_COOKIES);
            // 删除所有登录会话
            redisTemplate.delete(redisTemplate.keys(REDIS_KEY_LOGIN_STATUS + "*"));
        } catch (DouyinException e) {
            throw e;
        } catch (Exception e) {
            log.error("登出失败: {}", e.getMessage(), e);
            throw new DouyinException("登出失败", e);
        }
    }

    /**
     * 异步检查登录状态
     */
    private void checkLoginStatusAsync(String sessionId, BrowserContext context) {
        Thread thread = new Thread(() -> {
            try {
                Page page = context.newPage();
                
                // 最多检查5分钟
                for (int i = 0; i < 30; i++) {
                    Thread.sleep(10000); // 每10秒检查一次

                    // 检查是否登录成功（通过访问个人主页或检查cookies）
                    try {
                        page.navigate("https://www.douyin.com/user/");
                        page.waitForLoadState(LoadState.NETWORKIDLE);

                        // 检查是否包含用户信息
                        if (page.locator(".user-avatar").isVisible()) {
                            // 登录成功，保存cookies
                            String cookiesJson = objectMapper.writeValueAsString(page.context().cookies());
                            saveLoginCookies(cookiesJson);

                            // 更新登录状态
                            DouyinLoginStatusDTO statusDTO = new DouyinLoginStatusDTO();
                            statusDTO.setLogged(true);
                            statusDTO.setSessionId(sessionId);
                            statusDTO.setMessage("登录成功");
                            statusDTO.setNickname(page.locator(".nickname").innerText());
                            statusDTO.setAvatar(page.locator(".user-avatar img").getAttribute("src"));

                            saveLoginStatus(sessionId, statusDTO);

                            // 关闭浏览器
                            context.close();
                            return;
                        }
                    } catch (Exception e) {
                        log.error("检查登录状态失败: {}", e.getMessage(), e);
                    }
                }

                // 超时，更新状态
                DouyinLoginStatusDTO statusDTO = new DouyinLoginStatusDTO();
                statusDTO.setLogged(false);
                statusDTO.setSessionId(sessionId);
                statusDTO.setMessage("登录超时，请重新生成二维码");
                saveLoginStatus(sessionId, statusDTO);

                // 关闭浏览器
                context.close();
            } catch (Exception e) {
                log.error("异步检查登录状态失败: {}", e.getMessage(), e);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 保存登录状态到Redis
     */
    private void saveLoginStatus(String sessionId, DouyinLoginStatusDTO statusDTO) {
        try {
            String statusJson = objectMapper.writeValueAsString(statusDTO);
            redisTemplate.opsForValue().set(REDIS_KEY_LOGIN_STATUS + sessionId, statusJson, QR_CODE_EXPIRE_TIME, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            log.error("保存登录状态失败: {}", e.getMessage(), e);
        }
    }
}
