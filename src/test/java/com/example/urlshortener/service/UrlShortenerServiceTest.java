package com.example.urlshortener.service;

import com.example.urlshortener.AbstractIntegrationTest;
import com.example.urlshortener.entity.UrlMapping;
import com.example.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Optional;

public class UrlShortenerServiceTest extends AbstractIntegrationTest {

    @Autowired
    private UrlShortenerService urlShortenerService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UrlMappingRepository urlMappingRepository; 

    @BeforeEach
    public void setUp() {
        
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        urlMappingRepository.deleteAll();
    }

    @Test
    public void testShortenUrl_FreshRequest_ShouldSaveToCacheAndDb() {
        
        var longUrl = "https://www.google.com/search?q=testcontainers";

        var shortCode = urlShortenerService.shortenUrl(longUrl);
        
        Assertions.assertNotNull(shortCode);
        Assertions.assertFalse(shortCode.isEmpty());

        var cachedUrl = redisTemplate.opsForValue().get(shortCode);
        Assertions.assertEquals(longUrl, cachedUrl);

        var cachedReverse = redisTemplate.opsForValue().get("REVERSE:" + longUrl);
        Assertions.assertEquals(shortCode, cachedReverse);
        
        Optional<UrlMapping> dbRecord = urlMappingRepository.findByOriginalUrl(longUrl);
        Assertions.assertTrue(dbRecord.isPresent(), "Dữ liệu phải được lưu vào MySQL");
        Assertions.assertEquals(shortCode, dbRecord.get().getShortCode());
    }

    @Test
    public void testShortenUrl_Idempotency_ShouldReturnSameCodeForSameUrl() {
        var longUrl = "https://github.com/testcontainers";

        var code1 = urlShortenerService.shortenUrl(longUrl);
        var code2 = urlShortenerService.shortenUrl(longUrl);

        Assertions.assertEquals(code1, code2);

        long count = urlMappingRepository.count();
        Assertions.assertEquals(1, count, "Chỉ được phép có 1 bản ghi trong DB");
    }

    @Test
    public void testShortenUrl_WhenRedisLost_ShouldRecoverFromDb() {
        
        var longUrl = "https://stackoverflow.com";
        var existingCode = "dbCode123";

        UrlMapping mapping = new UrlMapping(123L, existingCode, longUrl); 
        urlMappingRepository.save(mapping);

        redisTemplate.delete("REVERSE:" + longUrl);
        redisTemplate.delete(existingCode);

        var result = urlShortenerService.shortenUrl(longUrl);
        Assertions.assertEquals(existingCode, result);
        var restoredCache = redisTemplate.opsForValue().get("REVERSE:" + longUrl);
        Assertions.assertEquals(existingCode, restoredCache);
    }

    @Test
    public void testGetOriginalUrl_ShouldWorkWithLazyLoading() {
        
        var code = "lazyCode";
        var url = "https://lazy-load.com";

        UrlMapping mapping = new UrlMapping(456L, code, url);
        urlMappingRepository.save(mapping);

        var result = urlShortenerService.getOriginalUrl(code);

        Assertions.assertEquals(url, result);

        var cachedUrl = redisTemplate.opsForValue().get(code);
        Assertions.assertEquals(url, cachedUrl);
    }

    @Test
    public void testGetOriginalUrl_WhenCacheHit_ShouldReturnFast() {
        
        var code = "fastCode";
        var url = "https://fast-response.com";

        redisTemplate.opsForValue().set(code, url);

        var result = urlShortenerService.getOriginalUrl(code);

        Assertions.assertEquals(url, result);

        Optional<UrlMapping> dbRecord = urlMappingRepository.findByShortCode(code);
        Assertions.assertFalse(dbRecord.isPresent(), "Dữ liệu phải được lấy từ Redis, không phải DB");
    }
}