package com.hxcoe.scrm.service.impl.douyin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.scrm.entity.douyin.DouyinCustomerEntity;
import com.hxcoe.scrm.entity.douyin.DouyinVideoEntity;
import com.hxcoe.scrm.exception.douyin.DouyinException;
import com.hxcoe.scrm.service.douyin.DouyinCrawlService;
import com.hxcoe.scrm.service.douyin.DouyinLoginService;
import com.hxcoe.scrm.service.douyin.DouyinModuleGuard;
import com.microsoft.playwright.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 抖音数据采集服务实现类
 */
@Slf4j
@Service
public class DouyinCrawlServiceImpl implements DouyinCrawlService {

    private static final String DOUYIN_SEARCH_URL = "https://www.douyin.com/search/%s";
    private static final int MAX_VIDEOS_PER_SEARCH = 10;
    private static final int MAX_COMMENTS_PER_VIDEO = 50;

    @Autowired
    private DouyinLoginService loginService;

    @Autowired
    private DouyinModuleGuard moduleGuard;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<DouyinVideoEntity> searchVideos(String keyword, Long taskId) throws DouyinException {
        moduleGuard.requireRedisAvailable("搜索抖音视频");
        moduleGuard.requireBrowserAvailable("搜索抖音视频");
        if (!checkLoginStatus()) {
            throw new DouyinException("未登录抖音，请先登录");
        }

        List<DouyinVideoEntity> videos = new ArrayList<>();

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true).setArgs(List.of("--no-sandbox", "--disable-setuid-sandbox", "--disable-dev-shm-usage")));
             BrowserContext context = browser.newContext()) {

            // 设置cookies
            setCookies(context);

            // 访问搜索页面
            String searchUrl = String.format(DOUYIN_SEARCH_URL, keyword);
            Page page = context.newPage();
            page.navigate(searchUrl);
            page.waitForTimeout(3000);

            // 等待视频列表加载
            page.waitForSelector("//*[@class='search-content']//*[contains(@class, 'video-item')]", new Page.WaitForSelectorOptions().setTimeout(10000.0));

            // 获取视频列表
            List<ElementHandle> videoElements = page.querySelectorAll("//*[@class='search-content']//*[contains(@class, 'video-item')]");

            int count = 0;
            for (ElementHandle videoElement : videoElements) {
                if (count >= MAX_VIDEOS_PER_SEARCH) {
                    break;
                }

                try {
                    // 提取视频信息
                    DouyinVideoEntity video = new DouyinVideoEntity();
                    video.setTaskId(taskId);

                    // 获取视频标题
                    ElementHandle titleElement = videoElement.querySelector(".title");
                    if (titleElement != null) {
                        video.setTitle(titleElement.innerText());
                    } else {
                        continue;
                    }

                    // 获取作者信息
                    ElementHandle authorElement = videoElement.querySelector(".author-name");
                    if (authorElement != null) {
                        video.setAuthor(authorElement.innerText());
                    }

                    // 获取视频URL
                    ElementHandle linkElement = videoElement.querySelector("a");
                    if (linkElement != null) {
                        String href = linkElement.getAttribute("href");
                        if (href != null) {
                            if (href.startsWith("//")) {
                                href = "https:" + href;
                            } else if (!href.startsWith("http")) {
                                href = "https://www.douyin.com" + href;
                            }
                            video.setVideoUrl(href);
                        }
                    }

                    if (video.getVideoUrl() != null) {
                        video.setCrawledTime(LocalDateTime.now());
                        videos.add(video);
                        count++;
                    }
                } catch (Exception e) {
                    log.error("提取视频信息失败: {}", e.getMessage(), e);
                    continue;
                } finally {
                    videoElement.dispose();
                }
            }

            log.info("成功采集到 {} 个视频", videos.size());
            return videos;
        } catch (DouyinException e) {
            throw e;
        } catch (Exception e) {
            log.error("搜索抖音视频失败: {}", e.getMessage(), e);
            throw new DouyinException("搜索抖音视频失败", e);
        }
    }

    @Override
    public List<DouyinCustomerEntity> getVideoComments(String videoUrl, Long taskId) throws DouyinException {
        moduleGuard.requireRedisAvailable("获取抖音视频评论");
        moduleGuard.requireBrowserAvailable("获取抖音视频评论");
        if (!checkLoginStatus()) {
            throw new DouyinException("未登录抖音，请先登录");
        }

        List<String> comments = new ArrayList<>();

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true).setArgs(List.of("--no-sandbox", "--disable-setuid-sandbox", "--disable-dev-shm-usage")));
             BrowserContext context = browser.newContext()) {

            // 设置cookies
            setCookies(context);

            // 访问视频页面
            Page page = context.newPage();
            page.navigate(videoUrl);
            page.waitForTimeout(3000);

            // 等待评论区加载
            page.waitForSelector(".comment-list", new Page.WaitForSelectorOptions().setTimeout(10000.0));

            // 滚动加载更多评论
            for (int i = 0; i < 3; i++) {
                page.evaluate("window.scrollBy(0, document.body.scrollHeight)");
                page.waitForTimeout(2000);
            }

            // 获取评论列表
            List<ElementHandle> commentElements = page.querySelectorAll(".comment-item");

            for (ElementHandle commentElement : commentElements) {
                try {
                    // 提取评论内容
                    ElementHandle contentElement = commentElement.querySelector(".comment-content");
                    if (contentElement != null) {
                        String commentText = contentElement.innerText();
                        if (!commentText.isEmpty()) {
                            comments.add(commentText);
                        }
                    }
                } catch (Exception e) {
                    log.error("提取评论信息失败: {}", e.getMessage(), e);
                    continue;
                } finally {
                    commentElement.dispose();
                }
            }

            log.info("成功采集到 {} 条评论", comments.size());

            // 识别意向客户（这里假设使用默认关键词，实际应从任务配置中获取）
            List<String> intentKeywords = List.of("多少钱", "怎么联系", "私信", "感兴趣", "求教程");
            List<String> excludeKeywords = List.of("广告", "骗子", "垃圾");

            return identifyPotentialCustomers(comments, intentKeywords, excludeKeywords, taskId);
        } catch (DouyinException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取视频评论失败: {}", e.getMessage(), e);
            throw new DouyinException("获取视频评论失败", e);
        }
    }

    @Override
    public List<DouyinCustomerEntity> identifyPotentialCustomers(List<String> comments, List<String> intentKeywords, List<String> excludeKeywords, Long taskId) {
        List<DouyinCustomerEntity> potentialCustomers = new ArrayList<>();

        for (String comment : comments) {
            if (comment == null || comment.isEmpty()) {
                continue;
            }

            // 检查是否包含排除关键词
            boolean containsExcludeKeyword = excludeKeywords.stream()
                    .anyMatch(exclude -> comment.contains(exclude));
            if (containsExcludeKeyword) {
                continue;
            }

            // 检查是否包含意向关键词
            Optional<String> matchedKeyword = intentKeywords.stream()
                    .filter(intent -> comment.contains(intent))
                    .findFirst();

            if (matchedKeyword.isPresent()) {
                DouyinCustomerEntity customer = new DouyinCustomerEntity();
                customer.setTaskId(taskId);
                customer.setNickname("未知用户"); // 实际应从评论中提取
                customer.setCommentContent(comment);
                customer.setMatchKeyword(matchedKeyword.get());
                customer.setStatus("pending");
                customer.setCreateTime(LocalDateTime.now());

                potentialCustomers.add(customer);
            }
        }

        log.info("识别出 {} 个意向客户", potentialCustomers.size());
        return potentialCustomers;
    }

    @Override
    public boolean checkLoginStatus() throws DouyinException {
        try {
            moduleGuard.requireRedisAvailable("检查抖音登录状态");
            String cookies = loginService.getLoginCookies();
            return cookies != null && !cookies.isEmpty();
        } catch (DouyinException e) {
            if (e.getStatusCode() == 503) {
                throw e;
            }
            log.warn("检查抖音登录状态失败: {}", e.getMessage());
            return false;
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
