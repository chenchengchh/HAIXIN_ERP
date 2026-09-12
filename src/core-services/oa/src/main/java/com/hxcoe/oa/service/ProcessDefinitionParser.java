package com.hxcoe.oa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.oa.service.dto.ProcessNodeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 流程定义解析器。
 * <p>解析审批流程模板的 process_definition JSON，提取审批节点列表，
 * 支持获取首节点与下一节点，是多节点会签流转的基础。</p>
 * <p>JSON格式示例：
 * <pre>{"nodes":[
 *   {"id":"node_1","name":"销售经理审批","assigneeRole":"sales_manager","action":"approve"},
 *   {"id":"node_2","name":"财务审批","assigneeRole":"finance_manager","action":"approve"}
 * ]}</pre>
 * </p>
 */
@Component
public class ProcessDefinitionParser {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionParser.class);

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 解析流程定义JSON，返回全部审批节点（按数组顺序，填充 nodeIndex）。
     *
     * @param processDefinition 流程定义JSON字符串
     * @return 节点列表（解析失败或无节点时返回空列表）
     */
    @SuppressWarnings("unchecked")
    public List<ProcessNodeDTO> parse(String processDefinition) {
        if (processDefinition == null || processDefinition.isBlank()) {
            logger.warn("流程定义为空，返回空节点列表");
            return Collections.emptyList();
        }
        try {
            Map<String, Object> root = objectMapper.readValue(processDefinition, Map.class);
            Object nodesObj = root.get("nodes");
            if (!(nodesObj instanceof List)) {
                logger.warn("流程定义中无 nodes 数组: {}", processDefinition);
                return Collections.emptyList();
            }
            List<Map<String, Object>> rawNodes = (List<Map<String, Object>>) nodesObj;
            List<ProcessNodeDTO> nodes = new ArrayList<>(rawNodes.size());
            for (int i = 0; i < rawNodes.size(); i++) {
                Map<String, Object> raw = rawNodes.get(i);
                ProcessNodeDTO node = new ProcessNodeDTO();
                node.setId(asString(raw.get("id")));
                node.setName(asString(raw.get("name")));
                node.setAssigneeRole(asString(raw.get("assigneeRole")));
                node.setAction(asString(raw.get("action")));
                node.setNodeIndex(i);
                nodes.add(node);
            }
            return nodes;
        } catch (Exception e) {
            logger.error("解析流程定义失败: {}, error: {}", processDefinition, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 获取流程定义的首节点。
     *
     * @param processDefinition 流程定义JSON字符串
     * @return 首节点（无节点时返回 null）
     */
    public ProcessNodeDTO getFirstNode(String processDefinition) {
        List<ProcessNodeDTO> nodes = parse(processDefinition);
        return nodes.isEmpty() ? null : nodes.get(0);
    }

    /**
     * 获取指定节点的下一节点。
     *
     * @param processDefinition 流程定义JSON字符串
     * @param currentNodeId     当前节点ID
     * @return 下一节点（已是末节点或未找到当前节点时返回 null）
     */
    public ProcessNodeDTO getNextNode(String processDefinition, String currentNodeId) {
        List<ProcessNodeDTO> nodes = parse(processDefinition);
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getId() != null && nodes.get(i).getId().equals(currentNodeId)) {
                if (i + 1 < nodes.size()) {
                    return nodes.get(i + 1);
                }
                return null; // 末节点
            }
        }
        logger.warn("未找到当前节点，无法定位下一节点: currentNodeId={}", currentNodeId);
        return null;
    }

    /**
     * 安全转换为字符串。
     *
     * @param obj 原始对象
     * @return 字符串（null 返回 null）
     */
    private String asString(Object obj) {
        return obj == null ? null : String.valueOf(obj);
    }
}
