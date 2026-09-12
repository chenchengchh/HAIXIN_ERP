package com.hxcoe.oa.iam.util;

import com.hxcoe.oa.iam.config.OaJwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OaJwtUtils {

    @Autowired
    private OaJwtConfig oaJwtConfig;

    /**
     * 生成JWT令牌。
     * 注意：jjwt的setClaims会重置整个payload，必须先setClaims再setSubject，
     * 否则subject会被claims覆盖导致网关校验缺少用户声明而返回401。
     * @param subject 令牌主体（用户名）
     * @param claims 自定义声明
     * @return JWT令牌
     */
    public String generateToken(String subject, Map<String, Object> claims) {
        SecretKey key = getSigningKey();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + oaJwtConfig.getExpiration());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public Claims parseClaims(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        SecretKey key = getSigningKey();
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaims(token);
            if (claims == null) {
                return false;
            }
            Date exp = claims.getExpiration();
            return exp == null || exp.after(new Date());
        } catch (JwtException ex) {
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    private SecretKey getSigningKey() {
        byte[] raw = oaJwtConfig.getSecret() == null ? new byte[0] : oaJwtConfig.getSecret().getBytes();
        if (raw.length >= 64) {
            return Keys.hmacShaKeyFor(raw);
        }
        try {
            return Keys.hmacShaKeyFor(MessageDigest.getInstance("SHA-512").digest(raw));
        } catch (Exception ex) {
            return Keys.hmacShaKeyFor((oaJwtConfig.getSecret() + oaJwtConfig.getSecret() + oaJwtConfig.getSecret() + oaJwtConfig.getSecret()).getBytes());
        }
    }
}
