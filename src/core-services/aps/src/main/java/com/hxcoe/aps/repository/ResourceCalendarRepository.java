package com.hxcoe.aps.repository;
import com.hxcoe.aps.entity.ResourceCalendarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ResourceCalendarRepository extends JpaRepository<ResourceCalendarEntity, Long> {

/**
* 鏍规嵁璧勬簮ID鏌ヨ璧勬簮鏃ュ巻
* @param resourceId 璧勬簮ID
* @return 璧勬簮鏃ュ巻鍒楄〃;
*/
List<ResourceCalendarEntity> findByResourceId(Long resourceId);

/**
* 鏍规嵁璧勬簮ID鍜屾棩鏈熸煡璇㈣祫婧愭棩鍘?     * @param resourceId 璧勬簮ID
* @param date 鏃ユ湡
* @return 璧勬簮鏃ュ巻;
*/
ResourceCalendarEntity findByResourceIdAndDate(Long resourceId, LocalDate date);

/**
* 鏍规嵁璧勬簮ID鍜屾棩鏈熻寖鍥存煡璇㈣祫婧愭棩鍘?     * @param resourceId 璧勬簮ID
* @param startDate 寮€濮嬫棩鏈?     * @param endDate 缁撴潫鏃ユ湡
* @return 璧勬簮鏃ュ巻鍒楄〃;
*/
List<ResourceCalendarEntity> findByResourceIdAndDateBetween(Long resourceId, LocalDate startDate, LocalDate endDate);

/**
* 鏍规嵁鏃ユ湡鏌ヨ璧勬簮鏃ュ巻
* @param date 鏃ユ湡
* @return 璧勬簮鏃ュ巻鍒楄〃;
*/
List<ResourceCalendarEntity> findByDate(LocalDate date);

/**
    * 根据资源ID和是否可用查询资源日历
    * @param resourceId 资源ID
    * @param available 是否可用
    * @return 资源日历列表;
    */
    List<ResourceCalendarEntity> findByResourceIdAndAvailable(Long resourceId, Boolean available);
}

