package com.hxcoe.aps.service;
import com.hxcoe.aps.entity.ScheduleResultEntity;
import com.hxcoe.aps.enums.SchedulingAlgorithm;
import java.util.List;
import java.util.Map;

public interface SchedulingEngineService {

/**
* 鎵ц鎺掔璁＄畻
* @param planId 鐢熶骇璁″垝ID
* @param algorithm 绠楁硶绫诲瀷
* @param objectives 浼樺寲鐩爣鍒楄〃
* @return 鎺掔缁撴灉;
*/
ScheduleResultEntity executeScheduling(Long planId, SchedulingAlgorithm algorithm, List<String> objectives);

/**
* 鎵ц鎺掔璁＄畻锛堝甫绾︽潫閰嶇疆锛?     * @param planId 鐢熶骇璁″垝ID
* @param algorithm 绠楁硶绫诲瀷
* @param objectives 浼樺寲鐩爣鍒楄〃
* @param constraints 绾︽潫閰嶇疆
* @return 鎺掔缁撴灉;
*/
ScheduleResultEntity executeScheduling(Long planId, SchedulingAlgorithm algorithm, List<String> objectives, Map<String, Object> constraints);

/**
* 鏌ヨ鎺掔缁撴灉
* @param planId 鐢熶骇璁″垝ID
* @return 鎺掔缁撴灉;
*/
ScheduleResultEntity getSchedulingResult(Long planId);

/**
* 楠岃瘉鎺掔鍙鎬?     * @param scheduleResultId 鎺掔缁撴灉ID
* @return 鏄惁鍙;
*/
boolean validateSchedule(Long scheduleResultId);

/**
* 妫€娴嬫帓绋嬪啿绐?     * @param scheduleResultId 鎺掔缁撴灉ID
* @return 鍐茬獊淇℃伅鍒楄〃;
*/
List<String> detectConflicts(Long scheduleResultId);

/**
* 浼樺寲鐜版湁鎺掔
* @param scheduleResultId 鎺掔缁撴灉ID
* @param objectives 浼樺寲鐩爣鍒楄〃
* @return 浼樺寲鍚庣殑鎺掔缁撴灉;
*/
ScheduleResultEntity optimizeSchedule(Long scheduleResultId, List<String> objectives);

/**
* 鎵嬪姩璋冩暣浠诲姟
* @param taskId 浠诲姟ID
* @param startTime 鏂扮殑寮€濮嬫椂闂?     * @param endTime 鏂扮殑缁撴潫鏃堕棿
* @param resourceId 鏂扮殑璧勬簮ID
* @return 鏇存柊鍚庣殑鎺掔缁撴灉;
*/
ScheduleResultEntity manualAdjustTask(Long taskId, String startTime, String endTime, Long resourceId);

    /**
     * 下达排程结果到MES
     */
    void releaseSchedule(Long scheduleResultId);
}

