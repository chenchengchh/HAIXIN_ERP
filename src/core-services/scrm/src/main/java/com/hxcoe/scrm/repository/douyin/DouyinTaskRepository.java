package com.hxcoe.scrm.repository.douyin;

import com.hxcoe.scrm.entity.douyin.DouyinTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DouyinTaskRepository extends JpaRepository<DouyinTaskEntity, Long> {
}
