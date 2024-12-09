package com.chatroom.room;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.UUID;

import java.util.Date;


@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.admin.secret}")
    private String adminSecret;

    public String generateAdminToken(String roomId) {
        long expirationInMs = 60 * 60 * 1000; // 1 hour in milliseconds
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationInMs);

        return Jwts.builder()
                .setSubject(roomId)
                .claim("role", "ADMIN")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, adminSecret)
                .compact();
    }

    public String generateUserToken(String username, String roomId) {
        long expirationInMs = 60 * 60 * 1000;
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationInMs);
        return Jwts.builder()
                .setSubject(username)
                .setId(UUID.randomUUID().toString()) // Unique identifier for each token
                .claim("roomId", roomId)
                .claim("role", "USER")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public JwtUserDetails validateUserToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();

            return new JwtUserDetails(
                    claims.getSubject(), // username
                    claims.getId(), // unique token id
                    claims.get("roomId", String.class),
                    claims.get("role", String.class)
            );
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validateAdminToken(String token, String roomId) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(adminSecret)
                    .parseClaimsJws(token)
                    .getBody();

            return roomId.equals(claims.getSubject()) &&
                    "ADMIN".equals(claims.get("role", String.class));
        } catch (Exception e) {
            return false;
        }
    }
}
