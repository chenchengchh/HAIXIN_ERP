package com.hxcoe.erp.service;

import com.hxcoe.erp.entity.OrganizationEntity;
import com.hxcoe.common.result.PageResult;

/**
 * 缁勭粐鏈嶅姟鎺ュ彛
 */
public interface OrganizationService {

    /**
     * 鍒涘缓缁勭粐
     *
     * @param organizationEntity 缁勭粐瀹炰綋
     * @return 鍒涘缓缁撴灉
     */
    OrganizationEntity createOrganization(OrganizationEntity organizationEntity);

    /**
     * 鏍规嵁ID鏌ヨ缁勭粐
     *
     * @param id 涓婚敭ID
     * @return 鏌ヨ缁撴灉
     */
    OrganizationEntity getOrganizationById(Long id);

    /**
     * 鏇存柊缁勭粐
     *
     * @param organizationEntity 缁勭粐瀹炰綋
     * @return 鏇存柊缁撴灉
     */
    OrganizationEntity updateOrganization(OrganizationEntity organizationEntity);

    /**
     * 鍒犻櫎缁勭粐
     *
     * @param id 涓婚敭ID
     * @return 鍒犻櫎缁撴灉
     */
    boolean deleteOrganization(Long id);

    /**
     * 鍒嗛〉鏌ヨ缁勭粐鍒楄〃
     *
     * @param page 褰撳墠椤电爜
     * @param size 姣忛〉鏉℃暟
     * @param name 缁勭粐鍚嶇О
     * @param code 缁勭粐浠ｇ爜
     * @return 鍒嗛〉缁撴灉
     */
    PageResult<OrganizationEntity> getOrganizationList(Integer page, Integer size, String name, String code);
}

