package com.hxcoe.aibrain.service.vision;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * 规则版视觉理解实现（S11 降级实现，零外部依赖，永远可用）
 * 对 OCR 纯文本做正则结构化提取：
 * - invoice：发票号码 / 开票日期 / 价税合计 / 销售方名称
 * - gauge ：仪表读数（数值 + 可选单位），可结合点位编码 hint
 * 未来接入多模态 LLM 时新增实现替换即可，调用方无感知。
 */
@Service
public class RuleBasedVisionService implements VisionService {

    /** 发票号码：标签后 8~20 位数字（兼容 OCR 把标签识别丢的情况，兜底取独立长数字串） */
    private static final Pattern INVOICE_NO_LABELED =
            Pattern.compile("发票号码[:：\\s]*([0-9]{8,20})");
    private static final Pattern INVOICE_NO_FALLBACK =
            Pattern.compile("(?<![0-9])([0-9]{8})(?![0-9])");

    /** 开票日期：支持 2026年08月01日 / 2026-08-01 / 2026/08/01（长分支在前，避免"28"被截断为"2"） */
    private static final Pattern INVOICE_DATE =
            Pattern.compile("(20[0-9]{2})[年\\-/](1[0-2]|0?[1-9])[月\\-/](3[01]|[12][0-9]|0?[1-9])日?");

    /** 价税合计：标签附近首个金额；兼容"价税合计(大写)...(小写)¥1234.56"版式 */
    private static final Pattern TOTAL_AMOUNT =
            Pattern.compile("价税合计[^0-9¥￥]{0,40}[¥￥]?\\s*([0-9][0-9,]*\\.?[0-9]{0,2})");
    private static final Pattern AMOUNT_FALLBACK =
            Pattern.compile("[¥￥]\\s*([0-9][0-9,]*\\.?[0-9]{0,2})");

    /** 销售方名称：标签后的公司名（以 公司/厂/中心/商行 结尾），"名称"标签可选，兼容换行版式 */
    private static final Pattern SUPPLIER_NAME =
            Pattern.compile("销售方(?:[\\s\\S]{0,20}?名\\s*称)?[:：\\s]+([\\u4e00-\\u9fa5A-Za-z0-9（）()]{2,40}?(?:公司|工厂|厂|中心|商行))");
    private static final Pattern COMPANY_FALLBACK =
            Pattern.compile("([\\u4e00-\\u9fa5]{2,30}(?:有限公司|股份有限公司))");

    /**
     * 仪表读数：首个带符号小数/整数。
     * 前向排除 数字/小数点/下划线/冒号/横杠/字母（避免点位编码 TEMP_001、时间 21:50、m3/h 单位误判），
     * 后向排除 数字/小数点/冒号/横杠/"号"/斜杠（避免日期 2026-08-02、"1号反应釜"设备名、分数误判）
     */
    private static final Pattern GAUGE_READING =
            Pattern.compile("(?<![0-9._:\\-A-Za-z])(-?[0-9]{1,6}\\.[0-9]{1,3}|-?[0-9]{1,4})(?![0-9.:\\-号/])");

    /** 常见单位 */
    private static final Pattern UNIT_PATTERN =
            Pattern.compile("(°C|℃|bar|MPa|kPa|Pa|m3/h|m³/h|L/min|kWh|kW|A|V|mA|%|rpm|mm|m)");

    @Override
    public VisionParseResult parseDocument(String docType, String ocrText, String hint) {
        VisionParseResult result = new VisionParseResult();
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("provider", provider());
        evidence.put("sources", java.util.List.of("ocr_text(rule-based)"));
        evidence.put("parseTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.setEvidence(evidence);

        if (ocrText == null || ocrText.isBlank()) {
            result.setSuccess(false);
            result.setMessage("OCR 文本为空，无法提取");
            result.setFields(Map.of());
            return result;
        }
        String text = ocrText.trim();

        if (DOC_TYPE_INVOICE.equals(docType)) {
            return parseInvoice(text, result);
        }
        if (DOC_TYPE_GAUGE.equals(docType)) {
            return parseGauge(text, hint, result);
        }
        result.setSuccess(false);
        result.setMessage("不支持的文档类型: " + docType + "（支持 invoice/gauge）");
        result.setFields(Map.of());
        return result;
    }

    @Override
    public String provider() {
        return "rule";
    }

    /**
     * 发票文本结构化：号码/日期/价税合计/销售方
     */
    private VisionParseResult parseInvoice(String text, VisionParseResult result) {
        Map<String, Object> fields = new HashMap<>();
        int hit = 0;

        // 发票号码
        String invoiceNo = matchFirst(INVOICE_NO_LABELED, text);
        if (invoiceNo == null) {
            invoiceNo = matchFirst(INVOICE_NO_FALLBACK, text);
        }
        if (invoiceNo != null) {
            fields.put("invoiceNo", invoiceNo);
            hit++;
        }

        // 开票日期（统一输出 yyyy-MM-dd）
        Matcher dm = INVOICE_DATE.matcher(text);
        if (dm.find()) {
            String date = String.format("%s-%02d-%02d",
                    dm.group(1), Integer.parseInt(dm.group(2)), Integer.parseInt(dm.group(3)));
            fields.put("invoiceDate", date);
            hit++;
        }

        // 价税合计
        String amountText = matchFirst(TOTAL_AMOUNT, text);
        if (amountText == null) {
            amountText = matchFirst(AMOUNT_FALLBACK, text);
        }
        if (amountText != null) {
            try {
                BigDecimal amount = new BigDecimal(amountText.replace(",", ""));
                fields.put("totalAmount", amount);
                hit++;
            } catch (NumberFormatException ignored) {
                // 金额解析失败按未命中处理，不阻断其他字段
            }
        }

        // 销售方名称
        String supplier = matchFirst(SUPPLIER_NAME, text);
        if (supplier == null) {
            // 兜底：限定在"销售方"关键词之后找公司名，避免误取购买方
            int sellerIdx = text.indexOf("销售方");
            if (sellerIdx >= 0) {
                supplier = matchFirst(COMPANY_FALLBACK, text.substring(sellerIdx));
            }
            if (supplier == null) {
                supplier = matchFirst(COMPANY_FALLBACK, text);
            }
        }
        if (supplier != null) {
            fields.put("supplierName", supplier);
            hit++;
        }

        result.setFields(fields);
        result.setConfidence(hit / 4.0);
        result.setSuccess(hit > 0);
        result.setMessage(hit == 4 ? "发票字段全部提取成功"
                : hit > 0 ? "部分字段提取成功（命中 " + hit + "/4），请人工补全其余字段"
                        : "未能提取发票关键字段，请人工录入");
        return result;
    }

    /**
     * 仪表读数结构化：数值 + 单位 + 点位（hint 优先）
     */
    private VisionParseResult parseGauge(String text, String hint, VisionParseResult result) {
        Map<String, Object> fields = new HashMap<>();

        // 读数：取第一个非年份数值
        Matcher rm = GAUGE_READING.matcher(text);
        Double reading = null;
        while (rm.find()) {
            String numText = rm.group(1);
            // 排除四位整数的年份误判（如 2026）
            if (numText.matches("20[0-9]{2}") && !numText.contains(".")) {
                continue;
            }
            try {
                reading = Double.parseDouble(numText);
                break;
            } catch (NumberFormatException ignored) {
                // 继续找下一个候选
            }
        }
        if (reading != null) {
            fields.put("reading", reading);
        }

        // 单位
        String unit = matchFirst(UNIT_PATTERN, text);
        if (unit != null) {
            fields.put("unit", unit);
        }

        // 点位编码：hint 优先，其次尝试从文本识别 TAG 风格编码（如 TEMP_001）
        String tagCode = (hint != null && !hint.isBlank()) ? hint.trim() : null;
        if (tagCode == null) {
            Matcher tm = Pattern.compile("([A-Z]{2,10}_[0-9]{3})").matcher(text);
            if (tm.find()) {
                tagCode = tm.group(1);
            }
        }
        if (tagCode != null) {
            fields.put("tagCode", tagCode);
        }

        result.setFields(fields);
        boolean ok = reading != null;
        result.setSuccess(ok);
        result.setConfidence(ok ? (unit != null || tagCode != null ? 0.9 : 0.75) : 0.0);
        result.setMessage(ok ? "读数提取成功，请人工核对后确认" : "未能识别有效读数，请人工录入");
        return result;
    }

    /**
     * 返回正则首个捕获组，未命中返回 null
     */
    private String matchFirst(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        return m.find() ? m.group(1).trim() : null;
    }
}
