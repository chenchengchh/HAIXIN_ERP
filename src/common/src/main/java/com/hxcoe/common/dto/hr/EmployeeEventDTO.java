package com.hxcoe.common.dto.hr;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * HR 员工事件 DTO（HR → OA/ERP）。
 * <p>HR 员工入职/调动/离职审批通过后通过 Outbox 推送，
 * OA 同步账号启停、ERP 同步员工主数据镜像，完成 HR 事件落地闭环。</p>
 * <p>注意：跨服务事件契约固定使用小驼峰命名，通过 @JsonNaming 显式声明，
 * 避免受各服务全局 Jackson 命名策略（如 ERP 的 SNAKE_CASE）影响。</p>
 */
@Data
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class EmployeeEventDTO implements Serializable {

    /** 事件 ID（UUID） */
    private String eventId;

    /** 链路追踪 ID */
    private String traceId;

    /** 事件业务键（{eventType}:{employeeNo}） */
    private String eventKey;

    /** 幂等键（与 eventKey 一致） */
    private String idempotencyKey;

    /** 生产者（hr-service） */
    private String producer;

    /** 事件版本 */
    private Integer eventVersion;

    /** 分区键（员工编号） */
    private String partitionKey;

    /** 事件发生时间 */
    private LocalDateTime eventTime;

    /** 事件类型（ONBOARDED-入职, TRANSFERRED-调动, RESIGNED-离职） */
    private String eventType;

    /** 员工载荷 */
    private EmployeePayload employee;

    /**
     * 员工载荷。
     */
    @Data
    @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
    public static class EmployeePayload implements Serializable {
        /** 员工 ID（HR 主键，用于 OA 账号关联） */
        private Long employeeId;
        /** 员工编号 */
        private String employeeNo;
        /** 员工姓名 */
        private String employeeName;
        /** 部门 ID */
        private Long departmentId;
        /** 部门编码 */
        private String departmentCode;
        /** 岗位 ID */
        private Long positionId;
        /** 岗位编码 */
        private String positionCode;
        /** 员工状态 */
        private String status;
        /** 入职日期 */
        private LocalDateTime hireDate;
        /** 离职日期 */
        private LocalDateTime leaveDate;
        /** 事件原因（调动/离职原因） */
        private String reason;
    }
}
