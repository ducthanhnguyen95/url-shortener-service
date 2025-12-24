package com.example.urlshortener.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "click_stats")
@Data
@NoArgsConstructor
public class ClickStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String shortCode;

    private String ipAddress;

    private String userAgent; 

    private String referer;   

    @Column(nullable = false)
    private LocalDateTime clickedAt;
    
    public ClickStat(String shortCode, String ipAddress, String userAgent, String referer) {
        this.shortCode = shortCode;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.referer = referer;
        this.clickedAt = LocalDateTime.now();
    }
}