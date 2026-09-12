package com.hxcoe.aps.repository;
import com.hxcoe.aps.entity.AlgorithmParamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlgorithmParamRepository extends JpaRepository<AlgorithmParamEntity, Long> {
    java.util.List<AlgorithmParamEntity> findByAlgorithmName(String algorithmName);
    
    void deleteByAlgorithmName(String algorithmName);
    
    java.util.List<AlgorithmParamEntity> findByAlgorithmNameAndIsDefault(String algorithmName, Boolean isDefault);
}