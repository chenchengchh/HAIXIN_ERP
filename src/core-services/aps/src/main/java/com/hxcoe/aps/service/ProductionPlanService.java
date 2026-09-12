package com.hxcoe.aps.service;
import com.hxcoe.aps.entity.ProductionPlanEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;


public interface ProductionPlanService {

    /**
    * 创建生产计划
    * @param productionPlan 生产计划实体
    * @return 生产计划实体  
    */
    ProductionPlanEntity createProductionPlan(ProductionPlanEntity productionPlan);

    /**
    * 更新生产计划
    * @param id 计划ID
    * @param productionPlan 更新的生产计划实体
    * @return 更新后的生产计划实体
    */
    ProductionPlanEntity updateProductionPlan(Long id, ProductionPlanEntity productionPlan);

    /**
    * 删除生产计划
    * @param id 计划ID
    */
    void deleteProductionPlan(Long id);

    /**
    * 根据ID查询生产计划
    * @param id 计划ID
    * @return 生产计划实体
    */
    ProductionPlanEntity getProductionPlanById(Long id);

    /**
    * 根据计划编号查询生产计划
    * @param planNo 计划编号
    * @return 生产计划实体
    */
    ProductionPlanEntity getProductionPlanByPlanNo(String planNo);

    /**
    * 查询所有生产计划
    * @return 生产计划列表
    */
    List<ProductionPlanEntity> getAllProductionPlans();

    /**
    * 分页查询生产计划
    * @param pageable 分页参数
    * @return 生产计划分页列表
    */
    Page<ProductionPlanEntity> getProductionPlansByPage(Pageable pageable);

    /**
    * 根据计划状态查询生产计划
    * @param status 计划状态
    * @return 生产计划列表
    */
    List<ProductionPlanEntity> getProductionPlansByStatus(String status);
}

