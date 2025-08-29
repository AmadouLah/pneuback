package com.pneumaliback.www.repository;

import com.pneumaliback.www.entity.OrderItem;
import com.pneumaliback.www.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);
}