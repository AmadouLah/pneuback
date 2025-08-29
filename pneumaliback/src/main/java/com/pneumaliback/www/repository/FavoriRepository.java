package com.pneumaliback.www.repository;

import com.pneumaliback.www.entity.Favori;
import com.pneumaliback.www.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriRepository extends JpaRepository<Favori, Long> {
    List<Favori> findByUser(User user);
    void deleteByUser(User user);
}