package com.hxcoe.ems.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

@Service
public class CustomReportExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(CustomReportExecutionService.class);
    private final JdbcTemplate jdbcTemplate;

    public CustomReportExecutionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public byte[] executeSelectToCsv(String sql, int maxRows) {
        String normalized = sql == null ? "" : sql.trim();
        validateSql(normalized);
        int safeMaxRows = Math.max(1, Math.min(maxRows, 20000));

        String csv = jdbcTemplate.execute((ConnectionCallback<String>) con -> {
            try (var stmt = con.createStatement()) {
                stmt.setMaxRows(safeMaxRows);
                try (var rs = stmt.executeQuery(normalized)) {
                    return toCsv(rs);
                }
            } catch (Exception e) {
                throw new RuntimeException("执行自定义报表失败", e);
            }
        });

        return csv.getBytes(StandardCharsets.UTF_8);
    }

    private void validateSql(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("sqlQuery不能为空");
        }
        String lower = sql.toLowerCase();
        if (!lower.startsWith("select")) {
            throw new IllegalArgumentException("仅允许执行SELECT语句");
        }
        if (lower.contains(";")) {
            throw new IllegalArgumentException("SQL不允许包含分号");
        }
        if (lower.contains("--") || lower.contains("/*") || lower.contains("*/")) {
            throw new IllegalArgumentException("SQL不允许包含注释");
        }
        String[] forbidden = {"insert ", "update ", "delete ", "drop ", "alter ", "create ", "truncate ", "grant ", "revoke "};
        for (String kw : forbidden) {
            if (lower.contains(kw)) {
                throw new IllegalArgumentException("SQL包含不允许的关键字");
            }
        }
    }

    private String toCsv(ResultSet rs) throws Exception {
        StringBuilder sb = new StringBuilder();
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();
        for (int i = 1; i <= cols; i++) {
            sb.append(escape(meta.getColumnLabel(i)));
            if (i < cols) sb.append(",");
        }
        sb.append("\n");

        int rows = 0;
        while (rs.next()) {
            rows++;
            for (int i = 1; i <= cols; i++) {
                Object v = rs.getObject(i);
                sb.append(escape(v == null ? "" : String.valueOf(v)));
                if (i < cols) sb.append(",");
            }
            sb.append("\n");
        }

        logger.info("Custom report executed, rows={}", rows);
        return sb.toString();
    }

    private String escape(String value) {
        if (value == null) return "";
        String v = value.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\n") || v.contains("\"")) {
            return "\"" + v + "\"";
        }
        return v;
    }
}
