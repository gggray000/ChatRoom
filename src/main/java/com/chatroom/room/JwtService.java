package com.chatroom.room;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(String roomId) {
        long expirationInMs = 60 * 60 * 1000; // 1 hour in milliseconds
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationInMs);
        return Jwts.builder()
                .setSubject(roomId)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public boolean validateToken(String token, String roomId) {
        try {
            String tokenRoomId = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            return roomId.equals(tokenRoomId);
        } catch (Exception e) {
            return false;
        }
    }
}
