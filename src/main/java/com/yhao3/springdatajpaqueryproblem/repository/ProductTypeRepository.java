package com.yhao3.springdatajpaqueryproblem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yhao3.springdatajpaqueryproblem.model.ProductType;

@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {}
