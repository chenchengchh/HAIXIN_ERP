package com.hxcoe.oa.repository;

import com.hxcoe.oa.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户Repository
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * 根据用户名查询用户
     */
    UserEntity findByUsername(String username);

    /**
     * 根据部门ID查询用户列表
     */
    List<UserEntity> findByDepartmentId(Long departmentId);

    /**
     * 根据用户状态查询用户列表
     */
    List<UserEntity> findByStatus(Integer status);

    /**
     * 根据真实姓名模糊查询
     */
    List<UserEntity> findByRealNameContaining(String realName);
}