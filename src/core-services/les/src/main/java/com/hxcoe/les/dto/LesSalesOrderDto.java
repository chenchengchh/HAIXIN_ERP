package com.hxcoe.les.dto;

import lombok.Data;

@Data
public class LesSalesOrderDto {

    private Long id;
    private String orderNo;
    private String customerName;
    private String deliveryAddress;
    private String orderDate;
    private String requiredDate;
    private Double totalAmount;
}

