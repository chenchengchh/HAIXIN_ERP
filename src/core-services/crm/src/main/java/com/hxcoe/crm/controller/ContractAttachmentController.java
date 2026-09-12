package com.hxcoe.crm.controller;

import com.hxcoe.crm.entity.ContractAttachmentEntity;
import com.hxcoe.crm.service.ContractAttachmentService;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * 合同附件管理控制器
 */
@RestController
@RequestMapping("/api/v1/crm/contracts/attachments")
public class ContractAttachmentController {

    @Autowired
    private ContractAttachmentService attachmentService;

    /**
     * 登记附件元数据
     *
     * @param attachment 附件实体（contractId/fileName/fileUrl必填）
     * @return 创建结果
     */
    @PostMapping
    public Result<ContractAttachmentEntity> createAttachment(@RequestBody ContractAttachmentEntity attachment) {
        if (attachment.getContractId() == null || attachment.getFileName() == null || attachment.getFileUrl() == null) {
            return Result.fail("合同ID、文件名、文件路径不能为空");
        }
        ContractAttachmentEntity created = attachmentService.createAttachment(attachment);
        return Result.success("附件登记成功", created);
    }

    /**
     * 分页查询附件列表
     *
     * @param page       页码，默认1
     * @param size       每页大小，默认10
     * @param keyword    文件名关键词（可选）
     * @param contractId 合同ID过滤（可选）
     * @return 附件分页数据
     */
    @GetMapping("/list")
    public Result<PageResult<ContractAttachmentEntity>> getAttachmentList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long contractId) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "id"));
        Page<ContractAttachmentEntity> attachmentPage = attachmentService.getAttachmentList(keyword, contractId, pageable);
        return Result.success("成功", PageResult.build(
                attachmentPage.getTotalElements(),
                attachmentPage.getSize(),
                attachmentPage.getNumber() + 1,
                attachmentPage.getContent()
        ));
    }

    /**
     * 查询附件详情
     *
     * @param id 附件ID
     * @return 附件详情
     */
    @GetMapping("/{id}")
    public Result<ContractAttachmentEntity> getAttachmentById(@PathVariable Long id) {
        ContractAttachmentEntity attachment = attachmentService.getAttachmentById(id);
        return attachment != null ? Result.success("成功", attachment) : Result.fail("附件不存在");
    }

    /**
     * 删除附件
     *
     * @param id 附件ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteAttachment(@PathVariable Long id) {
        boolean deleted = attachmentService.deleteAttachment(id);
        return deleted ? Result.success("附件删除成功", true) : Result.fail("附件不存在");
    }
}
