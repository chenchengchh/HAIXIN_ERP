package com.hxcoe.aps.service;
import com.hxcoe.aps.entity.ScheduleHistoryEntity;
import java.util.List;

public interface ScheduleHistoryService {

    List<ScheduleHistoryEntity> getAllHistories();

    ScheduleHistoryEntity createHistory(ScheduleHistoryEntity history);

/**
* 鏍规嵁ID鏌ヨ鎺掔鍘嗗彶
* @param id 鎺掔鍘嗗彶ID
* @return 鎺掔鍘嗗彶瀹炰綋;
*/
ScheduleHistoryEntity getHistoryById(Long id);

/**
* 鏍规嵁璁″垝ID鏌ヨ鎺掔鍘嗗彶
* @param planId 璁″垝ID
* @return 鎺掔鍘嗗彶鍒楄〃;
*/
List<ScheduleHistoryEntity> getHistoriesByPlanId(Long planId);

/**
* 鏍规嵁鎺掔缁撴灉ID鏌ヨ鎺掔鍘嗗彶
* @param scheduleId 鎺掔缁撴灉ID
* @return 鎺掔鍘嗗彶鍒楄〃;
*/
List<ScheduleHistoryEntity> getHistoriesByScheduleId(Long scheduleId);

/**
* 鍒犻櫎鎺掔鍘嗗彶
* @param id 鎺掔鍘嗗彶ID
*/
void deleteHistory(Long id);

/**
* 鎵归噺鍒涘缓鎺掔鍘嗗彶
* @param histories 鎺掔鍘嗗彶鍒楄〃
* @return 鎺掔鍘嗗彶鍒楄〃;
*/
List<ScheduleHistoryEntity> batchCreateHistories(List<ScheduleHistoryEntity> histories);
}

