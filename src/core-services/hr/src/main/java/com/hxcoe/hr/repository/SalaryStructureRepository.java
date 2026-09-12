package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.SalaryStructureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 薪酬结构仓库接口
 */
@Repository
public interface SalaryStructureRepository extends JpaRepository<SalaryStructureEntity, Long> {
}