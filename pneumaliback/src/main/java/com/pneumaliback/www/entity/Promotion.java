package com.pneumaliback.www.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.pneumaliback.www.enums.PromotionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "promotions")
@Data
@EqualsAndHashCode(callSuper = true)
public class Promotion extends EntiteAuditable {
    @Column(nullable = false, length = 100)
    private String code;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromotionType type;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @OneToMany(mappedBy = "promotion")
    private List<Order> orders = new ArrayList<>();
}
