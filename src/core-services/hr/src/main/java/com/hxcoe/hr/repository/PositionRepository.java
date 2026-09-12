package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.PositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 岗位仓库接口
 */
@Repository
public interface PositionRepository extends JpaRepository<PositionEntity, Long>, JpaSpecificationExecutor<PositionEntity> {



    /**
     * 根据岗位名称查询岗位
     *
     * @param name 岗位名称
     * @return 岗位实体
     */
    Optional<PositionEntity> findByName(String name);

    /**
     * 根据岗位编号查询岗位
     *
     * @param positionCode 岗位编号
     * @return 岗位实体
     */
    Optional<PositionEntity> findByPositionCode(String positionCode);

    /**
     * 根据岗位等级查询岗位列表
     *
     * @param level 岗位等级
     * @return 岗位列表
     */
    List<PositionEntity> findByLevel(String level);

    /**
     * 根据岗位状态查询岗位列表
     *
     * @param status 岗位状态
     * @return 岗位列表
     */
    List<PositionEntity> findByStatus(String status);

    /**
     * 根据岗位名称模糊查询
     *
     * @param name 岗位名称
     * @return 岗位列表
     */
    List<PositionEntity> findByNameContaining(String name);

    /**
     * 根据岗位等级和状态查询岗位列表
     *
     * @param level 岗位等级
     * @param status 岗位状态
     * @return 岗位列表
     */
    List<PositionEntity> findByLevelAndStatus(String level, String status);
}