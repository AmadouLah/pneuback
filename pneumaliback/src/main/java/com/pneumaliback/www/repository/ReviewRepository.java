package com.pneumaliback.www.repository;

import com.pneumaliback.www.entity.Review;
import com.pneumaliback.www.entity.Product;
import com.pneumaliback.www.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct(Product product);
    List<Review> findByUser(User user);
}