package com.hxcoe.wms.service;

import com.hxcoe.wms.entity.AsnEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface AsnService {
    AsnEntity createAsn(AsnEntity asn);
    AsnEntity updateAsn(Long id, AsnEntity asn);
    boolean deleteAsn(Long id);
    Page<AsnEntity> getAsns(Pageable pageable);
    /**
     * 按条件分页查询ASN列表
     * @param pageable 分页参数
     * @param asnNo ASN单号（模糊）
     * @param supplierName 供应商名称（模糊）
     * @param status 状态（精确）
     * @param startTime 预计到货时间起（yyyy-MM-dd HH:mm:ss）
     * @param endTime 预计到货时间止（yyyy-MM-dd HH:mm:ss）
     */
    Page<AsnEntity> getAsns(Pageable pageable, String asnNo, String supplierName, String status, String startTime, String endTime);
    /**
     * 按条件分页查询ASN列表（收货作业用，扩展仓库与送货单号筛选）
     * @param pageable 分页参数
     * @param asnNo ASN单号（模糊）
     * @param deliveryNoteNo 送货单号/PO单号（模糊）
     * @param supplierName 供应商名称（模糊）
     * @param warehouseCode 仓库编码（精确）
     * @param status 状态（精确）
     * @param startTime 预计到货时间起（yyyy-MM-dd HH:mm:ss）
     * @param endTime 预计到货时间止（yyyy-MM-dd HH:mm:ss）
     */
    Page<AsnEntity> getAsns(Pageable pageable, String asnNo, String deliveryNoteNo, String supplierName, String warehouseCode, String status, String startTime, String endTime);
    Optional<AsnEntity> getAsnById(Long id);
    AsnEntity receiveAsn(Long id);
    AsnEntity startAsn(Long id);
    AsnEntity scanAsn(Long id, String barcode, java.math.BigDecimal quantity);
    AsnEntity cancelAsn(Long id);
}
