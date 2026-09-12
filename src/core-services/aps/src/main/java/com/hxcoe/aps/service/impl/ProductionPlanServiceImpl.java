package com.hxcoe.aps.service.impl;
import com.hxcoe.aps.entity.ProductionPlanEntity;
import com.hxcoe.aps.repository.ProductionPlanRepository;
import com.hxcoe.aps.service.ProductionPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class ProductionPlanServiceImpl implements ProductionPlanService {

    @Autowired
    private ProductionPlanRepository productionPlanRepository;

    // 构造方法
    public ProductionPlanServiceImpl() {
        // 默认构造方法
    }

    @Override
    public ProductionPlanEntity createProductionPlan(ProductionPlanEntity productionPlan) {
        return productionPlanRepository.save(productionPlan);
    }

    @Override
    public ProductionPlanEntity getProductionPlanById(Long id) {
        return productionPlanRepository.findById(id).orElse(null);
    }

    @Override
    public ProductionPlanEntity getProductionPlanByPlanNo(String planNo) {
        return productionPlanRepository.findByPlanNo(planNo);
    }

    @Override
    public List<ProductionPlanEntity> getAllProductionPlans() {
        return productionPlanRepository.findAll();
    }

    @Override
    public Page<ProductionPlanEntity> getProductionPlansByPage(Pageable pageable) {
        return productionPlanRepository.findAll(pageable);
    }

    @Override
    public List<ProductionPlanEntity> getProductionPlansByStatus(String status) {
        return productionPlanRepository.findByStatus(status);
    }

    @Override
    public ProductionPlanEntity updateProductionPlan(Long id, ProductionPlanEntity productionPlan) {
        productionPlan.setId(id);
        return productionPlanRepository.save(productionPlan);
    }

    @Override
    public void deleteProductionPlan(Long id) {
        productionPlanRepository.deleteById(id);
    }
}
