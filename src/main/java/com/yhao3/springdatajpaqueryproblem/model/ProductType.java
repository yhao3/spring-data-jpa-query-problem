package com.yhao3.springdatajpaqueryproblem.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table
@Entity
public class ProductType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_type_id")
    private Long productTypeId;

    @Column(name = "product_type_name")
    private String productTypeName;

    @OneToMany(
        mappedBy = "productType", 
        // fetch = FetchType.EAGER, 
        fetch = FetchType.LAZY, 
        cascade = CascadeType.PERSIST
    )
    private List<Product> products;
}
