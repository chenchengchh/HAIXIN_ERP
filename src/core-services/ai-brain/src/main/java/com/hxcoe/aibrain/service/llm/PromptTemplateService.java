package com.hxcoe.aibrain.service.llm;

import com.hxcoe.aibrain.entity.AiPromptTemplateEntity;
import com.hxcoe.aibrain.repository.AiPromptTemplateRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 提示词模板服务（P4-7 Prompt 资产管理）
 * 职责：从 ai_prompt_template 表加载技能 system prompt（60s 缓存），渲染 ${var} 占位符；
 * 启动时自动播种默认模板（SEED），支持版本迭代与回滚（改库即生效，无需发版）。
 */
@Slf4j
@Service
public class PromptTemplateService {

    @Autowired
    private AiPromptTemplateRepository repository;

    /** 模板缓存：skillCode → 启用中最高版本模板内容 */
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    /**
     * 启动播种默认模板（表为空时插入 SEED 版本，幂等：按 skillCode+version 唯一约束跳过）
     */
    @PostConstruct
    public void seedDefaults() {
        List<String[]> defaults = List.of(
                new String[]{"MAINTENANCE_COPILOT_RERANK", "维修Copilot候选故障语义重排序",
                        "你是设备维修专家。给定故障症状描述和候选历史故障记录列表（JSON数组，含faultId/type/description），"
                                + "按症状与候选记录的语义相关性重新排序（同义/近义表述也算相关，如\"异常声响\"与\"异响\"）。"
                                + "只输出JSON数组，元素为faultId数字，按相关性降序，不要输出任何其他文字。"},
                new String[]{"EIGHT_D_ROOTCAUSE", "8D报告D4根因假设与D5永久对策草稿生成",
                        "你是资深质量工程师，擅长8D问题解决法。根据不合格品信息与历史同类处置记录，"
                                + "生成D4根因假设（最多3条，按可能性降序，须结合缺陷类型与物料特性）"
                                + "和D5永久纠正措施草稿（最多3条，须可执行、可验证、责任可归属）。"
                                + "只输出JSON：{\"d4\":[\"...\",\"...\"],\"d5\":[\"...\",\"...\"]}，不要输出任何其他文字。"},
                new String[]{"VISION_INVOICE_EXTRACT", "发票OCR文本字段结构化提取",
                        "你是票据结构化专家。从OCR识别的发票文本中提取关键字段，输出JSON："
                                + "{\"invoiceNo\":\"发票号码\",\"invoiceDate\":\"yyyy-MM-dd\",\"totalAmount\":价税合计数字,"
                                + "\"supplierName\":\"销售方名称\"}。确实无法识别的字段省略该键，不要猜测编造。"
                                + "兼容变体表述（如\"价税合计\"也可写作\"含税总额\"\"总计\"）。只输出JSON，不要输出任何其他文字。"},
                new String[]{"VISION_GAUGE_EXTRACT", "仪表OCR文本读数结构化提取",
                        "你是工业仪表读数识别专家。从OCR文本中提取仪表读数，输出JSON："
                                + "{\"reading\":读数数字,\"unit\":\"单位\",\"tagCode\":\"点位编码\"}。"
                                + "确实无法识别的字段省略该键，不要猜测编造；注意排除日期、年份、时间等非读数数字。"
                                + "只输出JSON，不要输出任何其他文字。"});
        int seeded = 0;
        for (String[] d : defaults) {
            if (repository.findBySkillCode(d[0]).isEmpty()) {
                AiPromptTemplateEntity t = new AiPromptTemplateEntity();
                t.setSkillCode(d[0]);
                t.setVersion(1);
                t.setDescription(d[1]);
                t.setTemplate(d[2]);
                t.setEnabled(true);
                t.setUpdatedTime(LocalDateTime.now());
                t.setUpdatedBy("SEED");
                repository.save(t);
                seeded++;
            }
        }
        if (seeded > 0) {
            log.info("提示词模板播种完成: 新增{}条", seeded);
        }
        refreshCache();
    }

    /**
     * 定时刷新模板缓存（60s，改库后一分钟内生效）
     */
    @Scheduled(initialDelay = 60000, fixedDelay = 60000)
    public void refreshCache() {
        try {
            List<AiPromptTemplateEntity> all = repository.findAll();
            Map<String, String> fresh = new ConcurrentHashMap<>();
            for (AiPromptTemplateEntity t : all) {
                if (Boolean.TRUE.equals(t.getEnabled())) {
                    // 同技能保留最高版本（findAll 无序，比较后写入）
                    fresh.merge(t.getSkillCode(), t.getVersion() + "\n" + t.getTemplate(),
                            (oldV, newV) -> Integer.parseInt(newV.substring(0, newV.indexOf('\n')))
                                    > Integer.parseInt(oldV.substring(0, oldV.indexOf('\n'))) ? newV : oldV);
                }
            }
            cache.clear();
            fresh.forEach((k, v) -> cache.put(k, v.substring(v.indexOf('\n') + 1)));
        } catch (Exception e) {
            log.warn("提示词模板缓存刷新失败: {}", e.getMessage());
        }
    }

    /**
     * 获取技能启用中最高版本的模板内容
     *
     * @param skillCode 技能编码
     * @return 模板内容（未注册返回 null）
     */
    public String getTemplate(String skillCode) {
        return cache.get(skillCode);
    }

    /**
     * 渲染模板：替换 ${var} 占位符为上下文值（缺失变量替换为空字符串，不抛异常）
     *
     * @param skillCode 技能编码
     * @param context   变量上下文
     * @return 渲染后的 prompt（模板未注册返回 null）
     */
    public String render(String skillCode, Map<String, Object> context) {
        String template = cache.get(skillCode);
        if (template == null) {
            return null;
        }
        String rendered = template;
        if (context != null) {
            for (Map.Entry<String, Object> e : context.entrySet()) {
                rendered = rendered.replace("${" + e.getKey() + "}",
                        e.getValue() == null ? "" : String.valueOf(e.getValue()));
            }
        }
        return rendered;
    }
}
