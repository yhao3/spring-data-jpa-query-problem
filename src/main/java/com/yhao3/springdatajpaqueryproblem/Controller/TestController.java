package com.yhao3.springdatajpaqueryproblem.Controller;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yhao3.springdatajpaqueryproblem.model.Product;
import com.yhao3.springdatajpaqueryproblem.model.ProductType;
import com.yhao3.springdatajpaqueryproblem.repository.ProductRepository;
import com.yhao3.springdatajpaqueryproblem.repository.ProductTypeRepository;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
public class TestController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductTypeRepository productTypeRepository;
    
    /** 新增 5 筆商品類別，並在每筆商品類別中各新增一筆商品 */
    @GetMapping("/insert")
    public String insertProductTypesAndProudcts() {
        for (int i = 0; i < 5; i++) {

            // create Product
            List<Product> products = new LinkedList<>();
            Product product = new Product();
            product.setProductName("product" + i);
            products.add(product);

            // create ProductType
            ProductType productType = new ProductType();
            productType.setProductTypeName("productType" + i);
            productType.setProducts(products);
            productType = productTypeRepository.save(productType);

            // update product
            product.setProductType(productType);
            productRepository.save(product);
        }
        return "ok";
    }

    /** 模擬 N + 1 Problem */
    @GetMapping(value="/test")
    public String test() {
        productTypeRepository.findAll().forEach(
            productType -> {
                log.info(productType.getProductTypeName());
            }
        );
        return "ok";
    }

    /** 模擬 N + 1 Problem (2) */
    @GetMapping(value="/test2")
    public String test2() {
        productTypeRepository.findAll().forEach(
            productType -> {
                log.info(productType.getProductTypeName());
                log.info(productType.getProducts().get(0).getProductName());
            }
        );
        return "ok";
    }
    
}
