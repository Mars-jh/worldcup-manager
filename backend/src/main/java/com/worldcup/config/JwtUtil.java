package com.worldcup.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.worldcup.model.Role;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类 - 负责生成和验证 JWT Token
 * Token 包含用户名和角色信息，有效期 24 小时
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expiration;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expiration}") long expiration) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET must contain at least 32 characters");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /** 生成 JWT Token */
    public String generateToken(String username, Role role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    /** 从 Token 提取用户名 */
    public String getUsername(String token) {
        return getClaims(token).getPayload().getSubject();
    }

    /** 从 Token 提取角色 */
    public Role getRole(String token) {
        String role = getClaims(token).getPayload().get("role", String.class);
        try {
            return Role.valueOf(role);
        } catch (IllegalArgumentException e) {
            return Role.VIEWER; // 无效角色默认降级为 VIEWER
        }
    }

    /** 验证 Token 是否有效 */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Jws<Claims> getClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }
}
