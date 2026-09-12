package com.hxcoe.oa.iam.security;

import com.hxcoe.oa.iam.config.OaJwtConfig;
import com.hxcoe.oa.iam.util.OaJwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

public class OaJwtAuthenticationFilter extends OncePerRequestFilter {

    private final OaJwtConfig oaJwtConfig;
    private final OaJwtUtils oaJwtUtils;

    public OaJwtAuthenticationFilter(OaJwtConfig oaJwtConfig, OaJwtUtils oaJwtUtils) {
        this.oaJwtConfig = oaJwtConfig;
        this.oaJwtUtils = oaJwtUtils;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
        if (currentAuthentication == null || currentAuthentication instanceof AnonymousAuthenticationToken) {
            String token = resolveToken(request);
            if (token != null && oaJwtUtils.validateToken(token)) {
                Claims claims = oaJwtUtils.parseClaims(token);
                if (claims != null) {
                    String username = claims.getSubject();
                    Long userId = toLong(claims.get("userId"));
                    Long employeeId = toLong(claims.get("employeeId"));
                    OaPrincipal principal = new OaPrincipal(userId, employeeId, username);
                    Collection<? extends GrantedAuthority> authorities = buildAuthorities(claims);
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            authorities
                    );
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String headerName = oaJwtConfig.getHeader();
        if (headerName == null || headerName.isBlank()) {
            headerName = HttpHeaders.AUTHORIZATION;
        }
        String raw = request.getHeader(headerName);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String prefix = oaJwtConfig.getPrefix();
        if (prefix == null || prefix.isBlank()) {
            prefix = "Bearer";
        }
        String withSpace = prefix + " ";
        if (raw.startsWith(withSpace)) {
            return raw.substring(withSpace.length()).trim();
        }
        if (raw.startsWith(prefix)) {
            return raw.substring(prefix.length()).trim();
        }
        return raw.trim();
    }

    private Collection<? extends GrantedAuthority> buildAuthorities(Claims claims) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        Object roles = claims.get("roles");
        if (roles instanceof Collection<?> coll) {
            for (Object v : coll) {
                String role = v == null ? null : String.valueOf(v).trim();
                if (role != null && !role.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }
            }
        }
        Object permissions = claims.get("permissions");
        if (permissions instanceof Collection<?> coll) {
            for (Object v : coll) {
                String perm = v == null ? null : String.valueOf(v).trim();
                if (perm != null && !perm.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority(perm));
                }
            }
        }
        authorities.removeIf(Objects::isNull);
        return authorities;
    }

    private Long toLong(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        String s = String.valueOf(v);
        if (s.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(s);
        } catch (Exception ex) {
            return null;
        }
    }
}
