package com.pneumaliback.www.repository;

import com.pneumaliback.www.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    Optional<Category> findByNameIgnoreCase(String name);
    boolean existsByName(String name);
    boolean existsByNameIgnoreCase(String name);
    
    List<Category> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT c FROM Category c ORDER BY SIZE(c.products) DESC")
    List<Category> findByPopularity();
    
    @Query("SELECT c, COUNT(p) FROM Category c LEFT JOIN c.products p GROUP BY c ORDER BY COUNT(p) DESC")
    List<Object[]> findCategoriesWithProductCount();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    long countProductsInCategory(@Param("categoryId") Long categoryId);
}