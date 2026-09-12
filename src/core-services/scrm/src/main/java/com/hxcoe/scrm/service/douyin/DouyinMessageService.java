package com.hxcoe.scrm.service.douyin;

import com.hxcoe.scrm.entity.douyin.DouyinCustomerEntity;
import com.hxcoe.scrm.exception.douyin.DouyinException;

/**
 * 抖音私信发送服务接口
 */
public interface DouyinMessageService {

    /**
     * 发送抖音私信
     * @param customer 意向客户信息
     * @param messageTemplate 私信模板
     * @return 是否发送成功
     * @throws DouyinException 抖音私信发送异常
     */
    boolean sendPrivateMessage(DouyinCustomerEntity customer, String messageTemplate) throws DouyinException;

    /**
     * 批量发送抖音私信
     * @param customers 意向客户列表
     * @param messageTemplate 私信模板
     * @param maxCount 最大发送数量
     * @return 成功发送的数量
     * @throws DouyinException 抖音私信发送异常
     */
    int sendBatchPrivateMessages(java.util.List<DouyinCustomerEntity> customers, String messageTemplate, int maxCount) throws DouyinException;

    /**
     * 检查私信发送频率限制
     * @return 是否可以发送私信
     */
    boolean checkSendFrequency();

    /**
     * 生成个性化私信内容
     * @param template 私信模板
     * @param customer 客户信息
     * @return 个性化私信内容
     */
    String generatePersonalizedMessage(String template, DouyinCustomerEntity customer);
}
