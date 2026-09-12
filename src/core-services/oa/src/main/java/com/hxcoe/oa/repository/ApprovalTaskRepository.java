package com.hxcoe.oa.repository;

import com.hxcoe.oa.entity.ApprovalTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 审批任务Repository
 */
@Repository
public interface ApprovalTaskRepository extends JpaRepository<ApprovalTaskEntity, Long>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<ApprovalTaskEntity> {

    /**
     * 根据审批人ID和状态查询审批任务列表
     */
    List<ApprovalTaskEntity> findByAssigneeIdAndStatus(Long assigneeId, String status);

    /**
     * 根据实例ID查询审批任务列表
     */
    List<ApprovalTaskEntity> findByInstanceId(Long instanceId);

    /**
     * 根据实例ID查询审批任务列表（按创建时间升序）
     */
    List<ApprovalTaskEntity> findByInstanceIdOrderByCreateTimeAsc(Long instanceId);

    /**
     * 根据状态统计任务数量
     * @param status 任务状态
     * @return 任务数量
     */
    long countByStatus(String status);

    /**
     * 根据实例ID和节点ID查询审批任务
     */
    List<ApprovalTaskEntity> findByInstanceIdAndNodeId(Long instanceId, String nodeId);

    /**
     * 根据审批人ID查询所有审批任务
     */
    List<ApprovalTaskEntity> findByAssigneeId(Long assigneeId);

    /**
     * 统计指定审批实例+节点下未完成（非completed、非cancelled）的任务数。
     * <p>用于会签流转判断：当该计数为0时，表示当前节点所有会签任务均已完成，可流转到下一节点。</p>
     *
     * @param instanceId 审批实例ID
     * @param nodeId     节点ID
     * @return 未完成任务数
     */
    @Query("SELECT COUNT(t) FROM ApprovalTaskEntity t WHERE t.instanceId = :instanceId " +
           "AND t.nodeId = :nodeId AND t.status <> 'completed' AND t.status <> 'cancelled'")
    long countUnfinishedByInstanceAndNode(@Param("instanceId") Long instanceId, @Param("nodeId") String nodeId);

    /**
     * 批量取消指定审批实例+节点下的所有 pending 任务。
     * <p>用于会签拒绝场景：任一审批人拒绝后，同节点的其他待审批任务需全部置为 cancelled，
     * 避免其他审批人继续操作已无效的任务。</p>
     *
     * @param instanceId 审批实例ID
     * @param nodeId     节点ID
     * @return 受影响行数
     */
    @Modifying
    @Query("UPDATE ApprovalTaskEntity t SET t.status = 'cancelled' " +
           "WHERE t.instanceId = :instanceId AND t.nodeId = :nodeId AND t.status = 'pending'")
    int cancelPendingTasksByInstanceAndNode(@Param("instanceId") Long instanceId, @Param("nodeId") String nodeId);
}