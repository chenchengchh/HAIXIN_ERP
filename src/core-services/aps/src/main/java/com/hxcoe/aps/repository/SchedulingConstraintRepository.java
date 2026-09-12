package com.hxcoe.aps.repository;
import com.hxcoe.aps.entity.SchedulingConstraintEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SchedulingConstraintRepository extends JpaRepository<SchedulingConstraintEntity, Long> {

/**
* 根据约束类型查询调度约束
* @param constraintType 约束类型
* @return 调度约束列表;
*/
List<SchedulingConstraintEntity> findByConstraintType(String constraintType);

/**
* 查询所有指定状态的调度约束
* @param status 状态
* @return 调度约束列表;
*/
List<SchedulingConstraintEntity> findByStatus(String status);
}

