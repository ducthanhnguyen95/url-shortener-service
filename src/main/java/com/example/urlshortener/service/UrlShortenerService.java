package com.example.urlshortener.service;

import com.example.urlshortener.entity.UrlMapping;
import com.example.urlshortener.manager.RangeManager;
import com.example.urlshortener.repository.UrlMappingRepository;
import com.example.urlshortener.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UrlShortenerService {

    private final RangeManager rangeManager;
    private final Base62Encoder base62Encoder;
    private final StringRedisTemplate redisTemplate;
    private final UrlMappingRepository urlMappingRepository;
    private static final long CACHE_TTL_DAYS = 30;

    @Transactional 
    public String shortenUrl(String longUrl) {
        var cachedShortCode = redisTemplate.opsForValue().get("REVERSE:" + longUrl);
        if (cachedShortCode != null) {
            return cachedShortCode; 
        }

        var existingMapping = urlMappingRepository.findByOriginalUrl(longUrl);
        if (existingMapping.isPresent()) {
            var dbShortCode = existingMapping.get().getShortCode();
            saveToRedis(dbShortCode, longUrl);
            return dbShortCode;
        }

        var id = rangeManager.getNextId();
        var shortCode = base62Encoder.encode(id);
        var newMapping = new UrlMapping(id, shortCode, longUrl);
        urlMappingRepository.save(newMapping);
        saveToRedis(shortCode, longUrl);
        return shortCode;
    }

    
    public String getOriginalUrl(String shortCode) {
        var cachedUrl = redisTemplate.opsForValue().get(shortCode);
        if (cachedUrl != null) return cachedUrl;

        return urlMappingRepository.findByShortCode(shortCode)
                .map(mapping -> {
                    saveToRedis(shortCode, mapping.getOriginalUrl());
                    return mapping.getOriginalUrl();
                }).orElse(null);
    }

    
    private void saveToRedis(String shortCode, String longUrl) {
        redisTemplate.opsForValue().set(shortCode, longUrl, CACHE_TTL_DAYS, TimeUnit.DAYS);
        redisTemplate.opsForValue().set("REVERSE:" + longUrl, shortCode, CACHE_TTL_DAYS, TimeUnit.DAYS);
    }
}