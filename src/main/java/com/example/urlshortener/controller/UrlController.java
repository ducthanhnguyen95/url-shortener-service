package com.example.urlshortener.controller;

import com.example.urlshortener.dto.ShortenRequest;
import com.example.urlshortener.dto.ShortenResponse;
import com.example.urlshortener.service.AnalyticsService;
import com.example.urlshortener.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlShortenerService urlShortenerService;
    private final AnalyticsService analyticsService;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    
    @PostMapping("/api/shorten")
    public ResponseEntity<ShortenResponse> shorten(@RequestBody ShortenRequest request) {
        var shortCode = urlShortenerService.shortenUrl(request.getLongUrl());
        var cleanBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        var fullShortUrl = cleanBaseUrl + "/" + shortCode;
        return ResponseEntity.ok(new ShortenResponse(fullShortUrl, request.getLongUrl()));
    }

    
    @GetMapping("/{shortCode}")
    public void redirect(@PathVariable String shortCode, HttpServletResponse response, HttpServletRequest request) throws IOException {
        var originalUrl = urlShortenerService.getOriginalUrl(shortCode);

        if (originalUrl != null) {
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            String referer = request.getHeader("Referer");
            analyticsService.logClick(shortCode, ipAddress, userAgent, referer);
            response.sendRedirect(originalUrl);
        } else {
            response.sendError(HttpStatus.NOT_FOUND.value(), "Link not found");
        }
    }
}