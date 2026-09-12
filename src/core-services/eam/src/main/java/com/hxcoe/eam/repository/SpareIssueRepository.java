package com.hxcoe.eam.repository;

import com.hxcoe.eam.entity.SpareIssueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpareIssueRepository extends JpaRepository<SpareIssueEntity, Long> {
}
