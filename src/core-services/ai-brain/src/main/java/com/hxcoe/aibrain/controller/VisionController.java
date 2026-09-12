package com.hxcoe.aibrain.controller;

import com.hxcoe.aibrain.service.vision.VisionService;
import com.hxcoe.aibrain.service.vision.VisionService.VisionParseResult;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 单据/仪表视觉理解端点（S11）
 * 接收前端 OCR 文本（Tesseract.js 或人工粘贴），规则结构化后返回，
 * 仅用于预填表单草稿（CONFIRM 级），本端点只读不落库。
 */
@RestController
@RequestMapping("/api/v1/ai-brain/vision")
public class VisionController {

    @Autowired
    private VisionService visionService;

    /**
     * 单据/仪表 OCR 文本结构化
     *
     * @param request 解析请求（docType: invoice/gauge；ocrText: OCR 纯文本；hint: 可选点位编码）
     * @return 结构化提取结果（统一信封）
     */
    @PostMapping("/parse-document")
    public Result<VisionParseResult> parseDocument(@RequestBody VisionParseRequest request) {
        VisionParseResult parsed = visionService.parseDocument(
                request.getDocType(), request.getOcrText(), request.getHint());
        return Result.success(parsed.isSuccess() ? "提取完成" : "提取失败", parsed);
    }

    /** 视觉理解请求体 */
    public static class VisionParseRequest {
        /** 文档类型：invoice/gauge */
        private String docType;
        /** OCR 识别纯文本 */
        private String ocrText;
        /** 辅助提示（如仪表点位编码），可空 */
        private String hint;

        public String getDocType() {
            return docType;
        }

        public void setDocType(String docType) {
            this.docType = docType;
        }

        public String getOcrText() {
            return ocrText;
        }

        public void setOcrText(String ocrText) {
            this.ocrText = ocrText;
        }

        public String getHint() {
            return hint;
        }

        public void setHint(String hint) {
            this.hint = hint;
        }
    }
}
