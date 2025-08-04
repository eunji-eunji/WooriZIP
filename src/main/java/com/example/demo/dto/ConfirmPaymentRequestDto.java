package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmPaymentRequestDto {
    private String paymentKey;
    private String orderId;
    private String amount;
}
