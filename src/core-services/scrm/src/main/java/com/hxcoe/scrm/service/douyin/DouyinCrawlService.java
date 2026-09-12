package com.hxcoe.scrm.service.douyin;

import com.hxcoe.scrm.entity.douyin.DouyinVideoEntity;
import com.hxcoe.scrm.entity.douyin.DouyinCustomerEntity;
import com.hxcoe.scrm.exception.douyin.DouyinException;

import java.util.List;

/**
 * 抖音数据采集服务接口
 */
public interface DouyinCrawlService {

    /**
     * 根据关键词搜索抖音视频
     * @param keyword 搜索关键词
     * @param taskId 任务ID
     * @return 采集到的视频列表
     * @throws DouyinException 抖音采集异常
     */
    List<DouyinVideoEntity> searchVideos(String keyword, Long taskId) throws DouyinException;

    /**
     * 获取视频评论
     * @param videoUrl 视频URL
     * @param taskId 任务ID
     * @return 采集到的评论列表（包含意向客户信息）
     * @throws DouyinException 抖音采集异常
     */
    List<DouyinCustomerEntity> getVideoComments(String videoUrl, Long taskId) throws DouyinException;

    /**
     * 识别意向客户
     * @param comments 评论内容列表
     * @param intentKeywords 意向关键词列表
     * @param excludeKeywords 排除关键词列表
     * @param taskId 任务ID
     * @return 意向客户列表
     */
    List<DouyinCustomerEntity> identifyPotentialCustomers(List<String> comments, List<String> intentKeywords, List<String> excludeKeywords, Long taskId);

    /**
     * 检查是否已登录抖音
     * @return 是否已登录
     * @throws DouyinException 抖音登录异常
     */
    boolean checkLoginStatus() throws DouyinException;
}
