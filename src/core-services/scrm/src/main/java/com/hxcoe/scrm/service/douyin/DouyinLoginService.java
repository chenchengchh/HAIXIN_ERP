package com.hxcoe.scrm.service.douyin;

import com.hxcoe.scrm.dto.douyin.DouyinLoginStatusDTO;
import com.hxcoe.scrm.exception.douyin.DouyinException;

/**
 * 抖音登录服务接口
 */
public interface DouyinLoginService {

    /**
     * 生成抖音登录二维码
     * @return 包含二维码URL和状态的DTO
     * @throws DouyinException 抖音登录异常
     */
    DouyinLoginStatusDTO generateLoginQRCode() throws DouyinException;

    /**
     * 验证抖音登录状态
     * @param sessionId 会话ID
     * @return 登录状态DTO
     * @throws DouyinException 抖音登录异常
     */
    DouyinLoginStatusDTO checkLoginStatus(String sessionId) throws DouyinException;

    /**
     * 获取当前登录状态
     * @return 登录状态DTO
     * @throws DouyinException 抖音登录异常
     */
    DouyinLoginStatusDTO getCurrentLoginStatus() throws DouyinException;

    /**
     * 保存抖音登录Cookies
     * @param cookies Cookies信息，JSON格式
     * @throws DouyinException 抖音登录异常
     */
    void saveLoginCookies(String cookies) throws DouyinException;

    /**
     * 获取抖音登录Cookies
     * @return Cookies信息，JSON格式
     * @throws DouyinException 抖音登录异常
     */
    String getLoginCookies() throws DouyinException;

    /**
     * 登出抖音账号
     * @throws DouyinException 抖音登录异常
     */
    void logout() throws DouyinException;
}
