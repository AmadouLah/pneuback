package com.pneumaliback.www.repository;

import com.pneumaliback.www.entity.Order;
import com.pneumaliback.www.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}