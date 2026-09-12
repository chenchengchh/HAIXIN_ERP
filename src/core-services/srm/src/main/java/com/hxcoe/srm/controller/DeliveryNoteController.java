package com.hxcoe.srm.controller;

import com.hxcoe.srm.entity.DeliveryNoteEntity;
import com.hxcoe.srm.service.DeliveryNoteService;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/srm")
public class DeliveryNoteController {

    @Autowired
    private DeliveryNoteService deliveryNoteService;

    @PostMapping("/delivery-notes")
    public Result<DeliveryNoteEntity> createDeliveryNote(@RequestBody DeliveryNoteEntity deliveryNote) {
        DeliveryNoteEntity result = deliveryNoteService.createDeliveryNote(deliveryNote);
        return Result.success("送货单创建成功", result);
    }

    @GetMapping("/delivery-notes")
    public Result<PageResult<DeliveryNoteEntity>> getDeliveryNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<DeliveryNoteEntity> result = deliveryNoteService.getDeliveryNotes(pageable);
        PageResult<DeliveryNoteEntity> pageResult = PageResult.build(
                result.getTotalElements(),
                result.getSize(),
                result.getNumber() + 1,
                result.getContent()
        );
        return Result.success("送货单列表查询成功", pageResult);
    }

    @GetMapping("/delivery-notes/{id}")
    public Result<DeliveryNoteEntity> getDeliveryNoteById(@PathVariable Long id) {
        Optional<DeliveryNoteEntity> result = deliveryNoteService.getDeliveryNoteById(id);
        return result.map(deliveryNoteEntity -> Result.success("送货单查询成功", deliveryNoteEntity)).orElseGet(() -> Result.fail("送货单不存在"));
    }

    @PutMapping("/delivery-notes/{id}")
    public Result<DeliveryNoteEntity> updateDeliveryNote(@PathVariable Long id, @RequestBody DeliveryNoteEntity deliveryNote) {
        DeliveryNoteEntity result = deliveryNoteService.updateDeliveryNote(id, deliveryNote);
        if (result != null) {
            return Result.success("送货单更新成功", result);
        } else {
            return Result.fail("送货单不存在");
        }
    }

    @DeleteMapping("/delivery-notes/{id}")
    public Result<String> deleteDeliveryNote(@PathVariable Long id) {
        deliveryNoteService.deleteDeliveryNote(id);
        return Result.success("送货单删除成功");
    }

    @PutMapping("/delivery-notes/{id}/ship")
    public Result<DeliveryNoteEntity> shipDeliveryNote(@PathVariable Long id) {
        DeliveryNoteEntity result = deliveryNoteService.shipDeliveryNote(id);
        if (result != null) {
            return Result.success("送货单发货成功", result);
        } else {
            return Result.fail("送货单不存在");
        }
    }

    @PutMapping("/delivery-notes/{id}/receive")
    public Result<DeliveryNoteEntity> receiveDeliveryNote(@PathVariable Long id) {
        DeliveryNoteEntity result = deliveryNoteService.receiveDeliveryNote(id);
        if (result != null) {
            return Result.success("送货单收货成功", result);
        } else {
            return Result.fail("送货单不存在");
        }
    }
}
