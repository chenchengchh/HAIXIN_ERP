package com.hxcoe.srm.repository;

import com.hxcoe.srm.entity.ScoreCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScoreCardRepository extends JpaRepository<ScoreCardEntity, Long> {
    List<ScoreCardEntity> findBySupplierId(Long supplierId);
}
