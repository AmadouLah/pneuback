package com.pneumaliback.www.repository;

import com.pneumaliback.www.entity.Delivery;
import com.pneumaliback.www.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByOrder(Order order);
}