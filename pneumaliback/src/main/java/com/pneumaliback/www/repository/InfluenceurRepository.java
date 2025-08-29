package com.pneumaliback.www.repository;

import com.pneumaliback.www.entity.Influenceur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InfluenceurRepository extends JpaRepository<Influenceur, Long> {
    Optional<Influenceur> findByEmail(String email);
    boolean existsByEmail(String email);
}