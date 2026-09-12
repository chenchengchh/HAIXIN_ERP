package com.hxcoe.hr.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.hr.entity.HrWorkTimeRecordEntity;
import com.hxcoe.hr.repository.HrWorkTimeRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * HR接收MES集成事件控制器
 *
 * <p>接收MES推送的生产报工工时数据并落库（hr_work_time_record），
 * 形成 MES→HR 报工工时闭环。按 eventId 幂等防重。
 */
@RestController
@RequestMapping({"/api/v1/hr/integration", "/hr/integration"})
public class HrWorkTimeIntegrationController {

    private static final Logger logger = LoggerFactory.getLogger(HrWorkTimeIntegrationController.class);

    @Autowired
    private HrWorkTimeRecordRepository workTimeRecordRepository;

    /**
     * 接收MES报工工时事件并落库。
     *
     * <p>处理逻辑：按 eventId 判重，已存在则返回 duplicated=true；
     * 否则组装报工工时记录落库并返回 duplicated=false。
     *
     * @param body 报工工时事件体（eventId/employeeNo/employeeName/workOrderNo/
     *             workHours/outputQuantity/reportDate/workstation）
     * @return 处理结果，data 携带 received 与 duplicated
     */
    @PostMapping("/mes/work-report")
    public Result<Map<String, Object>> receiveWorkReport(@RequestBody Map<String, Object> body) {
        Map<String, Object> data = new HashMap<>();
        if (body == null) {
            return Result.error("请求体不能为空");
        }
        String eventId = asString(body.get("eventId"));
        if (eventId == null || eventId.isBlank()) {
            return Result.error("eventId 不能为空");
        }

        // 幂等判重：同一事件ID只落库一次
        Optional<HrWorkTimeRecordEntity> existing = workTimeRecordRepository.findByEventId(eventId);
        if (existing.isPresent()) {
            data.put("received", true);
            data.put("duplicated", true);
            return Result.success("报工工时事件已处理过，跳过重复落库", data);
        }

        HrWorkTimeRecordEntity record = new HrWorkTimeRecordEntity();
        record.setEventId(eventId);
        record.setEmployeeNo(asString(body.get("employeeNo")));
        record.setEmployeeName(asString(body.get("employeeName")));
        record.setWorkOrderNo(asString(body.get("workOrderNo")));
        record.setWorkHours(asDouble(body.get("workHours")));
        record.setOutputQuantity(asInteger(body.get("outputQuantity")));
        record.setReportDate(asLocalDate(body.get("reportDate")));
        record.setWorkstation(asString(body.get("workstation")));
        workTimeRecordRepository.save(record);

        logger.info("MES报工工时已落库: eventId={}, employeeNo={}, workOrderNo={}, workHours={}",
                eventId, record.getEmployeeNo(), record.getWorkOrderNo(), record.getWorkHours());
        data.put("received", true);
        data.put("duplicated", false);
        return Result.success("报工工时接收成功", data);
    }

    /**
     * 将请求体中的值安全转换为字符串
     *
     * @param value 原始值
     * @return 字符串形式，null 返回 null
     */
    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 将请求体中的值安全转换为 Double（兼容数字与字符串形式）
     *
     * @param value 原始值
     * @return Double 形式，无法转换时返回 null
     */
    private Double asDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 将请求体中的值安全转换为 Integer（兼容数字与字符串形式）
     *
     * @param value 原始值
     * @return Integer 形式，无法转换时返回 null
     */
    private Integer asInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 将请求体中的值安全转换为 LocalDate（兼容 yyyy-MM-dd 与 ISO 日期时间字符串）
     *
     * @param value 原始值
     * @return LocalDate 形式，无法转换时返回 null
     */
    private LocalDate asLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        try {
            String text = String.valueOf(value);
            return text.length() > 10
                    ? LocalDate.parse(text.substring(0, 10))
                    : LocalDate.parse(text);
        } catch (Exception e) {
            return null;
        }
    }
}
