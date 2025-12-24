package com.example.urlshortener.controller;

import com.example.urlshortener.AbstractIntegrationTest;
import com.example.urlshortener.dto.ShortenRequest;
import com.example.urlshortener.service.UrlShortenerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class UrlControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc; 

    @Autowired
    private ObjectMapper objectMapper; 

    @Autowired
    private UrlShortenerService urlShortenerService; 


    @Test
    public void testShortenUrl_Success() throws Exception {
        
        var originalUrl = "https://www.google.com/maps";
        ShortenRequest request = new ShortenRequest();
        request.setLongUrl(originalUrl);
        
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) 
                .andDo(print())
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.originalUrl", is(originalUrl))) 
                .andExpect(jsonPath("$.shortUrl", containsString("http://localhost:8080/"))); 
    }

    
    @Test
    public void testRedirect_Success() throws Exception {
        
        var longUrl = "https://www.youtube.com";
        var shortCode = urlShortenerService.shortenUrl(longUrl);

        mockMvc.perform(get("/" + shortCode))
                .andDo(print())
                .andExpect(status().isFound()) 
                .andExpect(header().string("Location", longUrl)); 
    }
    
    @Test
    public void testRedirect_NotFound() throws Exception {
        var nonExistentCode = "Kh0ngTonTai";

        mockMvc.perform(get("/" + nonExistentCode))
                .andDo(print())
                .andExpect(status().isNotFound()) 
                .andExpect(status().reason("Link not found")); 
    }
}