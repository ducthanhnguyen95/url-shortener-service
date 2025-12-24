package com.example.urlshortener.service;

import com.example.urlshortener.entity.ClickStat;
import com.example.urlshortener.repository.ClickStatRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@Slf4j
public class AnalyticsService {

    private final ClickStatRepository clickStatRepository;
    
    private final Queue<ClickStat> logBuffer = new ConcurrentLinkedQueue<>();

    public AnalyticsService(ClickStatRepository clickStatRepository) {
        this.clickStatRepository = clickStatRepository;
    }

    public void logClick(String shortCode, String ip, String userAgent, String referer) {
        ClickStat stat = new ClickStat(shortCode, ip, userAgent, referer);
        logBuffer.add(stat);
    }

    @Scheduled(fixedDelay = 5000)
    public void flushLogsToDb() {
        if (logBuffer.isEmpty()) return;

        List<ClickStat> logsToSave = new ArrayList<>();

        while (!logBuffer.isEmpty()) {
            logsToSave.add(logBuffer.poll());
        }
        
        if (!logsToSave.isEmpty()) {
            clickStatRepository.saveAll(logsToSave);
            log.info("📊 Saved {} click logs to Database.", logsToSave.size());
        }
    }
}