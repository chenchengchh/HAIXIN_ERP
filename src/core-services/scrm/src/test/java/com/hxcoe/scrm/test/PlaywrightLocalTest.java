package com.hxcoe.scrm.test;

import com.microsoft.playwright.Playwright;

public class PlaywrightLocalTest {

    public static void main(String[] args) {
        System.out.println("=== 测试本地浏览器初始化 ===");
        
        // 测试使用本地已安装的浏览器
        Playwright playwright = null;
        try {
            System.out.println("尝试创建Playwright实例...");
            playwright = Playwright.create();
            System.out.println("✓ 成功创建Playwright实例");
            
            System.out.println("尝试获取Chromium浏览器类型...");
            var chromium = playwright.chromium();
            System.out.println("✓ 成功获取Chromium浏览器类型");
            
            System.out.println("尝试获取浏览器可执行路径...");
            String executablePath = chromium.executablePath();
            System.out.println("✓ 成功获取浏览器可执行路径: " + executablePath);
            
            System.out.println("尝试启动浏览器...");
            var launchOptions = new com.microsoft.playwright.BrowserType.LaunchOptions();
            launchOptions.setHeadless(true); // 无头模式，不显示浏览器窗口
            launchOptions.setArgs(java.util.List.of(
                "--no-sandbox",
                "--disable-setuid-sandbox",
                "--disable-dev-shm-usage"
            ));
            
            var browser = chromium.launch(launchOptions);
            System.out.println("✓ 成功启动浏览器");
            
            System.out.println("尝试创建新页面...");
            var page = browser.newPage();
            System.out.println("✓ 成功创建新页面");
            
            System.out.println("尝试导航到百度...");
            page.navigate("https://www.baidu.com");
            System.out.println("✓ 成功导航到百度");
            
            String title = page.title();
            System.out.println("✓ 页面标题: " + title);
            
            // 关闭资源
            page.close();
            browser.close();
            playwright.close();
            
            System.out.println("\n=== 测试完成：本地浏览器初始化成功 ===");
            
        } catch (Exception e) {
            System.err.println("\n=== 测试失败 ===");
            System.err.println("错误信息: " + e.getMessage());
            System.err.println("错误类型: " + e.getClass().getName());
            e.printStackTrace();
        } finally {
            if (playwright != null) {
                try {
                    playwright.close();
                } catch (Exception e) {
                    System.err.println("关闭Playwright实例时出错: " + e.getMessage());
                }
            }
        }
    }
}