package com.hxcoe.scrm.repository.douyin;

import com.hxcoe.scrm.entity.douyin.DouyinCustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DouyinCustomerRepository extends JpaRepository<DouyinCustomerEntity, Long> {
    List<DouyinCustomerEntity> findByTaskId(Long taskId);
    List<DouyinCustomerEntity> findByTaskIdAndStatus(Long taskId, String status);
    long countByTaskId(Long taskId);
    long countByTaskIdAndStatus(Long taskId, String status);
}
