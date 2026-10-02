package az.nizami.smartdirectaze.identity;

import az.nizami.smartdirectaze.identity.config.JwtAuthenticationFilter;
import az.nizami.smartdirectaze.identity.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

// A stale cookie left in the browser must not break requests, the login request included
class StaleJwtCookieTest {

    private final JwtService jwt = mock(JwtService.class);
    private final UserDetailsService users = mock(UserDetailsService.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwt, users);

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void passesAsAnonymous() throws Exception {
        ReflectionTestUtils.setField(filter, "urlRegister", "/api/v1/auth/register");
        filter.init();
        var request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setCookies(new Cookie("jwt_token", "old"));
        var chain = mock(FilterChain.class);
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        verify(chain).doFilter(any(), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void expiredToken() throws Exception {
        when(jwt.extractEmail("old")).thenThrow(new ExpiredJwtException(null, null, "JWT expired"));
        passesAsAnonymous();
    }

    @Test
    void brokenToken() throws Exception {
        when(jwt.extractEmail("old")).thenThrow(new MalformedJwtException("bad"));
        passesAsAnonymous();
    }

    @Test
    void deletedUser() throws Exception {
        when(jwt.extractEmail("old")).thenReturn("gone@example.com");
        when(users.loadUserByUsername("gone@example.com")).thenThrow(new UsernameNotFoundException("gone"));
        passesAsAnonymous();
    }
}
