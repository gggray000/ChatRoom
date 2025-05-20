package com.chatroom.room;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;


@Service
public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secret;


    public String generateUserToken(String username, String roomId, boolean isAdmin) {
        long expirationInMs = 60 * 60 * 1000;
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationInMs);
        return Jwts.builder()
                .setSubject(username)
                .setId(UUID.randomUUID().toString())
                .claim("roomId", roomId)
                .claim("isAdmin", isAdmin)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public JwtUserDetails validateUserToken(String tokenId, String roomId) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(tokenId)
                    .getBody();

            String roomIdInToken = claims.get("roomId", String.class);
            String username = claims.getSubject();
            boolean isAdmin = claims.get("isAdmin", Boolean.class);

            logger.info("Token validation - Room: {}, User: {}, Admin: {}",
                    roomIdInToken, username, isAdmin);

            JwtUserDetails jwtUserDetails = new JwtUserDetails(
                    claims.getSubject(),
                    claims.getId(),
                    claims.get("roomId", String.class),
                    claims.get("isAdmin", Boolean.class));

            if (!jwtUserDetails.getRoomId().equals(roomId)) {
                logger.warn("Room ID mismatch: token has {}, but got {}", jwtUserDetails.getRoomId(), roomId);
                return null;
            }
            return jwtUserDetails;

        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return null;
        }
    }

}
