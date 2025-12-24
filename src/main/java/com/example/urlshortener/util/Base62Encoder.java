package com.example.urlshortener.util;

import org.springframework.stereotype.Component;

@Component
public class Base62Encoder {

    
    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final char[] BASE62_CHARS = BASE62.toCharArray();
    private static final int BASE = 62;

    
    public String encode(long id) {
        var sb = new StringBuilder();
        if (id == 0) {
            return String.valueOf(BASE62_CHARS[0]);
        }
        while (id > 0) {
            int remainder = (int) (id % BASE);
            sb.append(BASE62_CHARS[remainder]);
            id /= BASE;
        }
        return sb.reverse().toString();
    }

    
    public long decode(String str) {
        var id = 0;
        for (var c : str.toCharArray()) {
            id = id * BASE + BASE62.indexOf(c);
        }
        return id;
    }
}