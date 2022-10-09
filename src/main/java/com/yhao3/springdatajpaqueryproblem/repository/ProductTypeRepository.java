package com.yhao3.springdatajpaqueryproblem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yhao3.springdatajpaqueryproblem.model.ProductType;

@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {
    @EntityGraph(
        attributePaths = { "products" }
    )
    List<ProductType> findAll();
}
