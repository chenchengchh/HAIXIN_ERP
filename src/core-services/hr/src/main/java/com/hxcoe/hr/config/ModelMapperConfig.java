package com.hxcoe.hr.config;

import com.hxcoe.hr.dto.DepartmentDTO;
import com.hxcoe.hr.dto.EmployeeDTO;
import com.hxcoe.hr.dto.PositionDTO;
import com.hxcoe.hr.entity.DepartmentEntity;
import com.hxcoe.hr.entity.EmployeeEntity;
import com.hxcoe.hr.entity.PositionEntity;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ModelMapper配置类
 */
@Configuration
public class ModelMapperConfig {

    /**
     * 配置ModelMapper实例
     * @return ModelMapper实例
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        // 设置匹配策略为严格匹配
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        
        // 配置PositionEntity到PositionDTO的映射
        modelMapper.typeMap(PositionEntity.class, PositionDTO.class)
                .addMappings(mapper -> {
                    // ID由数据库生成，但需要在查询时映射
                    mapper.map(src -> src.getId(), PositionDTO::setId);
                    mapper.map(src -> src.getCreatedTime(), PositionDTO::setCreatedTime);
                    mapper.map(src -> src.getUpdatedTime(), PositionDTO::setUpdatedTime);
                    mapper.map(src -> src.getDepartment().getId(), PositionDTO::setDepartmentId);
                });
        
        // 配置EmployeeEntity到EmployeeDTO的映射
        modelMapper.typeMap(EmployeeEntity.class, EmployeeDTO.class)
                .addMappings(mapper -> {
                    mapper.map(src -> src.getId(), EmployeeDTO::setId); // 保留ID映射
                    mapper.map(src -> src.getCreatedTime(), EmployeeDTO::setCreatedAt);
                    mapper.map(src -> src.getUpdatedTime(), EmployeeDTO::setUpdatedAt);
                    mapper.map(src -> src.getEmployeeCode(), EmployeeDTO::setEmployeeCode);
                    mapper.map(src -> src.getDepartment().getId(), EmployeeDTO::setDepartmentId);
                    mapper.map(src -> src.getDepartment().getName(), EmployeeDTO::setDepartmentName);
                    mapper.map(src -> src.getPosition().getId(), EmployeeDTO::setPositionId);
                    mapper.map(src -> src.getPosition().getName(), EmployeeDTO::setPositionName);
                });
        
        // 配置DepartmentEntity到DepartmentDTO的映射
        modelMapper.typeMap(DepartmentEntity.class, DepartmentDTO.class)
                .addMappings(mapper -> {
                    mapper.map(src -> src.getId(), DepartmentDTO::setId); // 保留ID映射
                    mapper.map(src -> src.getCreatedTime(), DepartmentDTO::setCreatedAt);
                    mapper.map(src -> src.getUpdatedTime(), DepartmentDTO::setUpdatedAt);
                    mapper.map(src -> src.getDepartmentCode(), DepartmentDTO::setDepartmentCode);
                });
        
        return modelMapper;
    }
}