package com.hxcoe.scrm.repository.douyin;

import com.hxcoe.scrm.entity.douyin.DouyinVideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DouyinVideoRepository extends JpaRepository<DouyinVideoEntity, Long> {
    List<DouyinVideoEntity> findByTaskId(Long taskId);
    long countByTaskId(Long taskId);
}
