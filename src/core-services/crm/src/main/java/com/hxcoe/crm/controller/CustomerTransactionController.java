package com.hxcoe.crm.controller;

import com.hxcoe.crm.entity.CustomerTransactionEntity;
import com.hxcoe.crm.repository.CustomerTransactionRepository;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.util.List;

/**
 * 客户交易记录控制器
 */
@RestController
@RequestMapping("/api/v1/crm/transactions")
public class CustomerTransactionController {

    @Autowired
    private CustomerTransactionRepository transactionRepository;

    /**
     * 获取交易记录列表
     *
     * @param page 当前页码
     * @param size 每页大小
     * @param keyword 搜索关键字
     * @param status 订单状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param customerName 客户名称
     * @return 交易记录列表
     */
    @GetMapping("/my")
    public Result<PageResult<CustomerTransactionEntity>> getMyTransactions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String customerName) {
        // 由于目前没有实现完整的交易管理系统，这里返回模拟数据
        // 实际项目中应该根据参数查询数据库
        Pageable pageable = PageRequest.of(page - 1, size);
        
        // 获取所有交易记录
        List<CustomerTransactionEntity> transactions = transactionRepository.findAll();
        
        // 模拟分页，实际项目中应该使用JPA的分页查询
        int total = transactions.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        List<CustomerTransactionEntity> pageContent = transactions.subList(start, end);
        
        // 使用模拟数据创建Page对象
        Page<CustomerTransactionEntity> result = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, total);
        PageResult<CustomerTransactionEntity> pageResult = PageResult.build(
                result.getTotalElements(),
                result.getSize(),
                result.getNumber() + 1,
                result.getContent()
        );
        return Result.success(pageResult);
    }

    /**
     * 导出客户交易记录
     *
     * @param customerId 客户ID
     * @param response   HTTP响应
     * @throws IOException IO异常
     */
    @GetMapping("/customer/{customerId}/export")
    public void exportCustomerTransactions(
            @PathVariable(name = "customerId") Long customerId,
            HttpServletResponse response) throws IOException {
        // 查询客户的交易记录
        List<CustomerTransactionEntity> transactions = transactionRepository.findByCustomerIdOrderByDealDateDesc(customerId);

        // 创建Excel工作簿
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("客户交易记录");
            
            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            
            // 创建内容样式
            CellStyle contentStyle = workbook.createCellStyle();
            contentStyle.setBorderTop(BorderStyle.THIN);
            contentStyle.setBorderBottom(BorderStyle.THIN);
            contentStyle.setBorderLeft(BorderStyle.THIN);
            contentStyle.setBorderRight(BorderStyle.THIN);
            contentStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            
            // 创建表头行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"订单编号", "交易金额", "成交日期", "产品信息", "状态"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                // 移除autoSizeColumn调用，避免字体配置问题
                sheet.setColumnWidth(i, 20 * 256); // 手动设置列宽
            }
            
            // 填充数据行
            for (int rowIndex = 0; rowIndex < transactions.size(); rowIndex++) {
                CustomerTransactionEntity transaction = transactions.get(rowIndex);
                Row row = sheet.createRow(rowIndex + 1);
                
                // 订单编号
                Cell orderNoCell = row.createCell(0);
                orderNoCell.setCellValue(transaction.getOrderNo());
                orderNoCell.setCellStyle(contentStyle);
                
                // 交易金额
                Cell amountCell = row.createCell(1);
                amountCell.setCellValue(transaction.getAmount().doubleValue());
                amountCell.setCellStyle(contentStyle);
                
                // 成交日期
                Cell dealDateCell = row.createCell(2);
                dealDateCell.setCellValue(transaction.getDealDate().toString());
                dealDateCell.setCellStyle(contentStyle);
                
                // 产品信息
                Cell productInfoCell = row.createCell(3);
                productInfoCell.setCellValue(transaction.getProductInfo());
                productInfoCell.setCellStyle(contentStyle);
                
                // 状态
                Cell statusCell = row.createCell(4);
                statusCell.setCellValue(transaction.getStatus());
                statusCell.setCellStyle(contentStyle);
            }
            
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=customer_transactions.xlsx");
            
            // 写入响应流
            workbook.write(response.getOutputStream());
        }
    }
}
