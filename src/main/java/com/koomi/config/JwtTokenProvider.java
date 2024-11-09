package com.koomi.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Component
public class JwtTokenProvider {
    private final SecretKey secretKey;

    public JwtTokenProvider() {
        this.secretKey = Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes());
    }

    // Generate token
    public String generateToken(Authentication auth) {
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        String role = populateAuthorities(authorities);

        return Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JwtConstant.EXPIRATION_TIME))
                .claim("email", auth.getName())
                .claim("authorities", role)
                .signWith(secretKey)  // Sử dụng secretKey đã khởi tạo
                .compact();
    }

    public String generateTempToken(String email) {
        return Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JwtConstant.TEMP_EXPIRATION_TIME)) // Thời gian hết hạn cho token tạm thời
                .claim("email", email)
                .signWith(secretKey)  // Sử dụng secretKey đã khởi tạo
                .compact();
    }


    // Trích xuất email từ JWT
    public String getEmailFromToken(String jwt) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)  // Sử dụng lại biến secretKey
                .build()
                .parseClaimsJws(jwt)
                .getBody()
                .get("email", String.class);
    }

    // Xử lý các quyền (authorities)
    private String populateAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<String> auth = new HashSet<>();
        for(GrantedAuthority authority : authorities) {
            auth.add(authority.getAuthority());
        }

        return String.join(",", auth);
    }
}
