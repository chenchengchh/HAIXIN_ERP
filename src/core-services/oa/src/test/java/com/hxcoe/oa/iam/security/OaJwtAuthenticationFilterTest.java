package com.hxcoe.oa.iam.security;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hxcoe.oa.iam.config.OaJwtConfig;
import com.hxcoe.oa.iam.util.OaJwtUtils;
import io.jsonwebtoken.Claims;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

class OaJwtAuthenticationFilterTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateRequestBeforeAnonymousAuthenticationTakesOver() throws Exception {
        OaJwtConfig oaJwtConfig = new OaJwtConfig();
        oaJwtConfig.setHeader("Authorization");
        oaJwtConfig.setPrefix("Bearer");

        OaJwtUtils oaJwtUtils = mock(OaJwtUtils.class);
        Claims claims = mock(Claims.class);
        when(oaJwtUtils.validateToken("mock-token")).thenReturn(true);
        when(oaJwtUtils.parseClaims("mock-token")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("admin");
        when(claims.get("userId")).thenReturn(1L);
        when(claims.get("employeeId")).thenReturn(null);
        when(claims.get("roles")).thenReturn(List.of("ADMIN"));
        when(claims.get("permissions")).thenReturn(List.of("oa:approval:task:read"));

        OaJwtAuthenticationFilter filter = new OaJwtAuthenticationFilter(oaJwtConfig, oaJwtUtils);

        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken(
                        "test-key",
                        "anonymousUser",
                        AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
                )
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer mock-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        OaPrincipal principal = assertInstanceOf(OaPrincipal.class, authentication.getPrincipal());
        assertNotNull(principal);
        assertNotNull(
                authentication.getAuthorities().stream()
                        .filter((authority) -> "oa:approval:task:read".equals(authority.getAuthority()))
                        .findFirst()
                        .orElse(null)
        );
    }
}
