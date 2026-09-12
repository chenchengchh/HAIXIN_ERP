package com.hxcoe.hr.service;

import com.hxcoe.hr.entity.PositionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 岗位服务接口
 */
public interface PositionService {

    /**
     * 创建岗位
     *
     * @param position 岗位实体
     * @return 创建后的岗位实体
     */
    PositionEntity createPosition(PositionEntity position);

    /**
     * 更新岗位信息
     *
     * @param id 岗位ID
     * @param position 岗位实体
     * @return 更新后的岗位实体
     */
    PositionEntity updatePosition(Long id, PositionEntity position);

    /**
     * 根据ID删除岗位
     *
     * @param id 岗位ID
     */
    void deletePosition(Long id);

    /**
     * 根据ID查询岗位
     *
     * @param id 岗位ID
     * @return 岗位实体
     */
    Optional<PositionEntity> getPositionById(Long id);

    /**
     * 根据岗位编号查询岗位
     *
     * @param positionCode 岗位编号
     * @return 岗位实体
     */
    Optional<PositionEntity> getPositionByCode(String positionCode);

    /**
     * 查询所有岗位
     *
     * @return 岗位列表
     */
    List<PositionEntity> getAllPositions();

    /**
     * 分页查询岗位
     *
     * @param pageable 分页参数
     * @return 分页岗位列表
     */
    Page<PositionEntity> getPositionsByPage(Pageable pageable);

    /**
     * 根据岗位等级查询岗位列表
     *
     * @param level 岗位等级
     * @return 岗位列表
     */
    List<PositionEntity> getPositionsByLevel(String level);

    /**
     * 根据岗位状态查询岗位列表
     *
     * @param status 岗位状态
     * @return 岗位列表
     */
    List<PositionEntity> getPositionsByStatus(String status);

    /**
     * 根据岗位等级和状态查询岗位列表
     *
     * @param level 岗位等级
     * @param status 岗位状态
     * @return 岗位列表
     */
    List<PositionEntity> getPositionsByLevelAndStatus(String level, String status);

    /**
     * 搜索岗位
     *
     * @param keyword 搜索关键词
     * @return 岗位列表
     */
    List<PositionEntity> searchPositions(String keyword);

    /**
     * 批量删除岗位
     *
     * @param ids 岗位ID列表
     */
    void batchDeletePositions(List<Long> ids);
}