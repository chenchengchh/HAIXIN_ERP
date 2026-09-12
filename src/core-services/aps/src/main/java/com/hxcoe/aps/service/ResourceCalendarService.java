package com.hxcoe.aps.service;
import com.hxcoe.aps.entity.ResourceCalendarEntity;
import java.time.LocalDate;
import java.util.List;

public interface ResourceCalendarService {

    List<ResourceCalendarEntity> getAllResourceCalendars();

    ResourceCalendarEntity createResourceCalendar(ResourceCalendarEntity resourceCalendar);

/**
* 鏍规嵁ID鏌ヨ璧勬簮鏃ュ巻
* @param id 璧勬簮鏃ュ巻ID
* @return 璧勬簮鏃ュ巻瀹炰綋;
*/
ResourceCalendarEntity getResourceCalendarById(Long id);

/**
* 鏍规嵁璧勬簮ID鏌ヨ璧勬簮鏃ュ巻
* @param resourceId 璧勬簮ID
* @return 璧勬簮鏃ュ巻鍒楄〃;
*/
List<ResourceCalendarEntity> getResourceCalendarsByResourceId(Long resourceId);

/**
* 鏍规嵁璧勬簮ID鍜屾棩鏈熻寖鍥存煡璇㈣祫婧愭棩鍘?     * @param resourceId 璧勬簮ID
* @param startDate 寮€濮嬫棩鏈?     * @param endDate 缁撴潫鏃ユ湡
* @return 璧勬簮鏃ュ巻鍒楄〃;
*/
List<ResourceCalendarEntity> getResourceCalendarsByResourceIdAndDateRange(Long resourceId, LocalDate startDate, LocalDate endDate);

/**
* 鏇存柊璧勬簮鏃ュ巻
* @param id 璧勬簮鏃ュ巻ID
* @param resourceCalendar 璧勬簮鏃ュ巻瀹炰綋
* @return 璧勬簮鏃ュ巻瀹炰綋;
*/
ResourceCalendarEntity updateResourceCalendar(Long id, ResourceCalendarEntity resourceCalendar);

/**
* 鍒犻櫎璧勬簮鏃ュ巻
* @param id 璧勬簮鏃ュ巻ID
*/
void deleteResourceCalendar(Long id);

/**
* 鎵归噺璁剧疆璧勬簮鏃ュ巻
* @param resourceCalendars 璧勬簮鏃ュ巻鍒楄〃
* @return 璧勬簮鏃ュ巻鍒楄〃;
*/
List<ResourceCalendarEntity> batchSetResourceCalendars(List<ResourceCalendarEntity> resourceCalendars);

/**
* 妫€鏌ヨ祫婧愬湪鎸囧畾鏃ユ湡鏄惁鍙敤
* @param resourceId 璧勬簮ID
* @param date 鏃ユ湡
* @return 鏄惁鍙敤;
*/
boolean isResourceAvailableOnDate(Long resourceId, LocalDate date);

/**
* 鑾峰彇璧勬簮鍦ㄦ寚瀹氭棩鏈熺殑鍙敤鏃堕暱
* @param resourceId 璧勬簮ID
* @param date 鏃ユ湡
* @return 鍙敤鏃堕暱锛堝垎閽燂級;
*/
Integer getAvailableMinutesOnDate(Long resourceId, LocalDate date);
}

