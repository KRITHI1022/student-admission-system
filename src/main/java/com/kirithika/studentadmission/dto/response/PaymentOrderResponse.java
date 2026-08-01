package com.kirithika.studentadmission.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentOrderResponse {

    private String razorpayOrderId;
    private Double amount;
    private String currency;
    private String razorpayKeyId;
}