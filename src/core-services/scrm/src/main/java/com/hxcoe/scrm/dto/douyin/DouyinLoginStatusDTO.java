package com.hxcoe.scrm.dto.douyin;

import lombok.Data;

/**
 * 抖音登录状态DTO
 */
@Data
public class DouyinLoginStatusDTO {
    /**
     * 是否登录成功
     */
    private boolean isLogged;
    
    /**
     * 登录二维码URL
     */
    private String qrCodeUrl;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 登录状态信息
     */
    private String message;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 头像URL
     */
    private String avatar;
}
