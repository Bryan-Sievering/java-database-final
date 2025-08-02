package com.project.code.repo;

import com.project.code.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // No custom methods needed; basic CRUD operations are inherited.
}