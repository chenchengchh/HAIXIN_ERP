package com.hxcoe.qms.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.dto.inspection.InspectionStandardDTO;
import org.springframework.data.domain.Pageable;

/**
 * 检验标准服务接口
 */
public interface InspectionStandardService {

    /**
     * 分页查询检验标准
     *
     * @param standardNo 标准编号（模糊）
     * @param materialName 物料名称（模糊）
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    Result<PageResult<InspectionStandardDTO>> page(String standardNo, String materialName, String status, Pageable pageable);

    /**
     * 根据ID获取检验标准
     *
     * @param id 标准ID
     * @return 检验标准
     */
    Result<InspectionStandardDTO> getById(Long id);

    /**
     * 根据标准编号获取检验标准
     *
     * @param standardNo 标准编号
     * @return 检验标准
     */
    Result<InspectionStandardDTO> getByStandardNo(String standardNo);

    /**
     * 创建检验标准
     *
     * @param dto 检验标准
     * @return 创建结果
     */
    Result<InspectionStandardDTO> create(InspectionStandardDTO dto);

    /**
     * 更新检验标准
     *
     * @param id 标准ID
     * @param dto 更新数据
     * @return 更新结果
     */
    Result<InspectionStandardDTO> update(Long id, InspectionStandardDTO dto);

    /**
     * 删除检验标准
     *
     * @param id 标准ID
     * @return 删除结果
     */
    Result<Void> delete(Long id);

    /**
     * 启用检验标准
     *
     * @param id 标准ID
     * @return 更新结果
     */
    Result<Void> activate(Long id);

    /**
     * 停用检验标准
     *
     * @param id 标准ID
     * @return 更新结果
     */
    Result<Void> deactivate(Long id);
}
