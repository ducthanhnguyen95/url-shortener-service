package com.example.urlshortener.service;

import com.example.urlshortener.AbstractIntegrationTest;
import com.example.urlshortener.repository.ClickStatRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AnalyticsServiceTest extends AbstractIntegrationTest {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private ClickStatRepository clickStatRepository;

    @Test
    public void testLogClick_ShouldSaveToMysql() throws InterruptedException {
        
        clickStatRepository.deleteAll();
        analyticsService.logClick("xyz", "127.0.0.1", "Mozilla/5.0", "google.com");
        analyticsService.logClick("xyz", "127.0.0.1", "Mozilla/5.0", "facebook.com");
        analyticsService.logClick("abc", "192.168.1.1", "Chrome/10", "direct");
        analyticsService.flushLogsToDb();
        long count = clickStatRepository.count();
        Assertions.assertEquals(3, count);
        System.out.println("✅ Test Analytics Passed: Found 3 records in MySQL");
    }
}