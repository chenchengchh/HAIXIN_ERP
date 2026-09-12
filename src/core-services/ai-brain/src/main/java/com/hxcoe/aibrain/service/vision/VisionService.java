package com.hxcoe.aibrain.service.vision;

import java.util.Map;

/**
 * 单据/仪表视觉理解接口（S11，P3）
 * 设计原则：与 LlmService 一致——可信算法优先。OCR 在前端浏览器完成（Tesseract.js），
 * 本服务接收 OCR 纯文本做规则化结构提取；未来接入多模态 LLM 时新增实现并切换配置即可，
 * 调用方无需改动（面向接口编程 + 条件装配）。
 *
 * 自动化级别：CONFIRM——提取结果仅用于预填表单草稿，人工确认后才走现有业务端点写入，
 * 本服务自身只读、不落库、不开后门。
 */
public interface VisionService {

    /** 支持的文档类型：发票/送货单 */
    String DOC_TYPE_INVOICE = "invoice";

    /** 支持的文档类型：现场仪表盘读数 */
    String DOC_TYPE_GAUGE = "gauge";

    /**
     * 将 OCR 文本结构化为业务字段
     *
     * @param docType 文档类型（invoice/gauge）
     * @param ocrText OCR 识别纯文本（前端 Tesseract.js 产出或人工粘贴）
     * @param hint    辅助提示（如仪表点位编码，可为空）
     * @return 结构化提取结果（失败时 success=false 且必须给出 message，禁止抛异常阻断主流程）
     */
    VisionParseResult parseDocument(String docType, String ocrText, String hint);

    /**
     * 当前实现提供者标识（rule/multimodal-llm...），用于状态端点展示与日志审计
     *
     * @return 提供者标识
     */
    String provider();

    /**
     * 视觉理解结构化结果（统一信封：success + fields + evidence）
     */
    class VisionParseResult {
        private boolean success;
        private String message;
        /** 提取出的业务字段（invoice: invoiceNo/invoiceDate/totalAmount/supplierName；gauge: reading/unit/tagCode） */
        private Map<String, Object> fields;
        /** 提取置信度 0~1（按命中字段数加权，仅供人工参考） */
        private double confidence;
        /** 证据链：来源与解析时间 */
        private Map<String, Object> evidence;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Map<String, Object> getFields() {
            return fields;
        }

        public void setFields(Map<String, Object> fields) {
            this.fields = fields;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public Map<String, Object> getEvidence() {
            return evidence;
        }

        public void setEvidence(Map<String, Object> evidence) {
            this.evidence = evidence;
        }
    }
}
