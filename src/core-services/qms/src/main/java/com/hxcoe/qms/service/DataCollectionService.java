package com.hxcoe.qms.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.DataCollectionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 质量数据采集服务接口
 */
public interface DataCollectionService {

    /**
     * 分页查询质量数据采集
     *
     * @param collectionNo 采集编号（模糊）
     * @param dataType 数据类型
     * @param collectionDateStart 采集日期开始
     * @param collectionDateEnd 采集日期结束
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    Result<PageResult<DataCollectionEntity>> page(String collectionNo, String dataType, java.time.LocalDate collectionDateStart, java.time.LocalDate collectionDateEnd, String status, Pageable pageable);

    /**
     * 获取采集详情
     *
     * @param id 采集ID
     * @return 采集详情
     */
    Result<DataCollectionEntity> getById(Long id);

    /**
     * 创建质量数据采集
     *
     * @param entity 采集数据
     * @return 创建结果
     */
    Result<DataCollectionEntity> create(DataCollectionEntity entity);

    /**
     * 更新质量数据采集
     *
     * @param id 采集ID
     * @param entity 更新数据
     * @return 更新结果
     */
    Result<DataCollectionEntity> update(Long id, DataCollectionEntity entity);

    /**
     * 提交质量数据采集
     *
     * @param id 采集ID
     * @return 提交结果
     */
    Result<Void> submit(Long id);

    /**
     * 删除质量数据采集
     *
     * @param id 采集ID
     * @return 删除结果
     */
    Result<Void> delete(Long id);

    /**
     * 导入质量数据
     *
     * @param file 导入文件
     * @return 导入结果
     */
    Result<Map<String, Object>> importData(MultipartFile file);
}
