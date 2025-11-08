package com.backend.auth.svc.service;

import com.backend.auth.svc.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private static final String SECRET = "h8J9kL2mQ4sT7vW9yB1eE4fG7hJ0lN3pR6uT9wX2zC5vY8bA3dF6hJ9kL2mQ4sT";

    public String generateToken(User user) {
        String tokenId = UUID.randomUUID().toString();
        return Jwts.builder()
                .setId(tokenId)
                .setSubject(user.getEmail())
                .claim("username", user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET.getBytes())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    public long getExpirationSeconds(String token) {
        Date exp = getClaims(token).getExpiration();
        Date now = new Date();
        return Math.max((exp.getTime() - now.getTime()) / 1000, 0);
    }

    public String getTokenId(String token) {
        return getClaims(token).getId();
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
