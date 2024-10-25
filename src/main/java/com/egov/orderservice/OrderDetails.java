package com.egov.orderservice;


import lombok.*;

import java.io.Serializable;

@Data
@Setter @Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderDetails implements Serializable {

    private PurchaseOrder orderDetails;
    private Product product;
}
