package com.pneumaliback.www.repository;

import com.pneumaliback.www.entity.Product;
import com.pneumaliback.www.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(Category category);
    Optional<Product> findByName(String name);
    boolean existsByName(String name);
}