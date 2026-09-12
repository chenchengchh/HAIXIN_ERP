package com.hxcoe.gateway.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * JWT工具类，用于生成、解析和验证JWT令牌
 */
@Component
public class JwtUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    
    @Autowired
    private com.hxcoe.gateway.config.JwtConfig jwtConfig;
    
    /**
     * 生成JWT令牌
     * @param claims 自定义声明
     * @return JWT令牌
     */
    public String generateToken(Map<String, Object> claims) {
        SecretKey key = getSigningKey();
        
        // 设置过期时间
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());
        
        // 生成JWT令牌
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * 从JWT令牌中获取声明
     * @param token JWT令牌
     * @return 声明
     */
    public Claims getClaimsFromToken(String token) {
        try {
            SecretKey key = getSigningKey();
            
            // 解析JWT令牌
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            logger.error("JWT令牌已过期: {}", e.getMessage());
            throw new RuntimeException("JWT令牌已过期", e);
        } catch (MalformedJwtException e) {
            logger.error("JWT令牌格式错误: {}", e.getMessage());
            throw new RuntimeException("JWT令牌格式错误", e);
        } catch (SignatureException e) {
            logger.error("JWT签名验证失败: {}", e.getMessage());
            throw new RuntimeException("JWT签名验证失败", e);
        } catch (IllegalArgumentException e) {
            logger.error("JWT令牌为空或无效: {}", e.getMessage());
            throw new RuntimeException("JWT令牌为空或无效", e);
        }
    }

    private SecretKey getSigningKey() {
        byte[] raw = jwtConfig.getSecret() == null ? new byte[0] : jwtConfig.getSecret().getBytes();
        if (raw.length >= 64) {
            return Keys.hmacShaKeyFor(raw);
        }
        try {
            return Keys.hmacShaKeyFor(MessageDigest.getInstance("SHA-512").digest(raw));
        } catch (Exception ex) {
            return Keys.hmacShaKeyFor((jwtConfig.getSecret() + jwtConfig.getSecret() + jwtConfig.getSecret() + jwtConfig.getSecret()).getBytes());
        }
    }
    
    /**
     * 验证JWT令牌
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (Exception e) {
            logger.error("JWT令牌验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 从JWT令牌中获取用户名
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String subject = claims.getSubject();
        if (subject != null && !subject.isBlank()) {
            return subject;
        }
        return getClaimAsString(claims, "username");
    }
    
    /**
     * 从JWT令牌中获取用户ID
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String value = getClaimAsString(claims, "userId");
        return value == null || value.isBlank() ? null : Long.parseLong(value);
    }
    
    /**
     * 从JWT令牌中获取角色信息
     * @param token JWT令牌
     * @return 角色信息
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return getClaimAsString(claims, "role");
    }

    public List<String> getRolesFromToken(String token) {
        return getStringListClaim(getClaimsFromToken(token), "roles");
    }

    public List<String> getPermissionsFromToken(String token) {
        return getStringListClaim(getClaimsFromToken(token), "permissions");
    }
    
    /**
     * 检查JWT令牌是否过期
     * @param token JWT令牌
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    private String getClaimAsString(Claims claims, String claimName) {
        if (claims == null || claimName == null || claimName.isBlank()) {
            return null;
        }
        Object value = claims.get(claimName);
        return value == null ? null : String.valueOf(value);
    }

    private List<String> getStringListClaim(Claims claims, String claimName) {
        if (claims == null || claimName == null || claimName.isBlank()) {
            return Collections.emptyList();
        }
        Object raw = claims.get(claimName);
        if (raw instanceof Collection<?> collection) {
            List<String> values = new ArrayList<>();
            for (Object item : collection) {
                if (item != null) {
                    String value = String.valueOf(item).trim();
                    if (!value.isBlank()) {
                        values.add(value);
                    }
                }
            }
            return values;
        }
        if (raw instanceof String value && !value.isBlank()) {
            return List.of(value.trim());
        }
        return Collections.emptyList();
    }
}
