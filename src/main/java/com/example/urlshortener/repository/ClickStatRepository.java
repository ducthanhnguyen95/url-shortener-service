package com.example.urlshortener.repository;

import com.example.urlshortener.entity.ClickStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClickStatRepository extends JpaRepository<ClickStat, Long> {
    
}