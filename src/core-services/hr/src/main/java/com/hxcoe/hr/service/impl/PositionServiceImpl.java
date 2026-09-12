package com.hxcoe.hr.service.impl;

import com.hxcoe.hr.entity.PositionEntity;
import com.hxcoe.hr.repository.PositionRepository;
import com.hxcoe.hr.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 岗位服务实现类
 */
@Service
@Transactional
public class PositionServiceImpl implements PositionService {

    @Autowired
    private PositionRepository positionRepository;

    /**
     * 创建岗位
     *
     * @param position 岗位实体
     * @return 创建后的岗位实体
     */
    @Override
    public PositionEntity createPosition(PositionEntity position) {
        // 可以添加岗位编号生成逻辑等
        return positionRepository.save(position);
    }

    /**
     * 更新岗位信息
     *
     * @param id 岗位ID
     * @param position 岗位实体
     * @return 更新后的岗位实体
     */
    @Override
    public PositionEntity updatePosition(Long id, PositionEntity position) {
        Optional<PositionEntity> existingPosition = positionRepository.findById(id);
        if (existingPosition.isPresent()) {
            position.setId(id);
            // 可以添加更新前后的逻辑，如审计日志等
            return positionRepository.save(position);
        }
        throw new RuntimeException("Position not found with id: " + id);
    }

    /**
     * 根据ID删除岗位
     *
     * @param id 岗位ID
     */
    @Override
    public void deletePosition(Long id) {
        if (positionRepository.existsById(id)) {
            positionRepository.deleteById(id);
        } else {
            throw new RuntimeException("Position not found with id: " + id);
        }
    }

    /**
     * 根据ID查询岗位
     *
     * @param id 岗位ID
     * @return 岗位实体
     */
    @Override
    public Optional<PositionEntity> getPositionById(Long id) {
        return positionRepository.findById(id);
    }

    /**
     * 根据岗位编号查询岗位
     *
     * @param positionCode 岗位编号
     * @return 岗位实体
     */
    @Override
    public Optional<PositionEntity> getPositionByCode(String positionCode) {
        return positionRepository.findByPositionCode(positionCode);
    }

    /**
     * 查询所有岗位
     *
     * @return 岗位列表
     */
    @Override
    public List<PositionEntity> getAllPositions() {
        return positionRepository.findAll();
    }

    /**
     * 分页查询岗位
     *
     * @param pageable 分页参数
     * @return 分页岗位列表
     */
    @Override
    public Page<PositionEntity> getPositionsByPage(Pageable pageable) {
        return positionRepository.findAll(pageable);
    }

    /**
     * 根据岗位等级查询岗位列表
     *
     * @param level 岗位等级
     * @return 岗位列表
     */
    @Override
    public List<PositionEntity> getPositionsByLevel(String level) {
        return positionRepository.findByLevel(level);
    }

    /**
     * 根据岗位状态查询岗位列表
     *
     * @param status 岗位状态
     * @return 岗位列表
     */
    @Override
    public List<PositionEntity> getPositionsByStatus(String status) {
        return positionRepository.findByStatus(status);
    }

    /**
     * 根据岗位等级和状态查询岗位列表
     *
     * @param level 岗位等级
     * @param status 岗位状态
     * @return 岗位列表
     */
    @Override
    public List<PositionEntity> getPositionsByLevelAndStatus(String level, String status) {
        return positionRepository.findByLevelAndStatus(level, status);
    }

    /**
     * 搜索岗位
     *
     * @param keyword 搜索关键词
     * @return 岗位列表
     */
    @Override
    public List<PositionEntity> searchPositions(String keyword) {
        return positionRepository.findByNameContaining(keyword);
    }

    /**
     * 批量删除岗位
     *
     * @param ids 岗位ID列表
     */
    @Override
    public void batchDeletePositions(List<Long> ids) {
        positionRepository.deleteAllById(ids);
    }
}