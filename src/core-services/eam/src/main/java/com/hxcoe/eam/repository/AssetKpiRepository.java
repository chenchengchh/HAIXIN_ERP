package com.hxcoe.eam.repository;

import com.hxcoe.eam.entity.AssetKpiEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssetKpiRepository extends JpaRepository<AssetKpiEntity, Long> {
    Optional<AssetKpiEntity> findByAssetIdAndRecordMonth(Long assetId, String recordMonth);
    List<AssetKpiEntity> findByRecordMonth(String recordMonth);
    List<AssetKpiEntity> findByRecordMonthBetween(String startMonth, String endMonth);

    /**
     * 查询最近有KPI数据的月份（用于看板当月无数据时回退展示）
     * @return 最新的record_month，无数据时返回empty
     */
    @Query("SELECT MAX(k.recordMonth) FROM AssetKpiEntity k")
    Optional<String> findLatestRecordMonth();
}
