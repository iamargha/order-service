package com.egov.orderservice;

import jakarta.persistence.*;
import lombok.Data;
//import javax.persistence.*;


@Entity
@Table(name = "products")
@Data

public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Double price;

    private Integer quantity;

    private String category;

}
