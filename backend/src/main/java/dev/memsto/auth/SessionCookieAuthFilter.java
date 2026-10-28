package dev.memsto.auth;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Authenticates requests from the session cookie. An invalid or expired cookie is cleared and the
 * request continues anonymously, so public endpoints such as login keep working.
 */
@RequiredArgsConstructor
public class SessionCookieAuthFilter extends OncePerRequestFilter {

    private static final List<SimpleGrantedAuthority> AUTHORITIES = List.of(new SimpleGrantedAuthority("ROLE_USER"));

    private final TokenService tokens;
    private final SessionCookies cookies;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Cookie cookie = WebUtils.getCookie(request, cookies.name());
        if (cookie != null && !cookie.getValue().isEmpty()) {
            tokens.verify(cookie.getValue()).ifPresentOrElse(
                    user -> SecurityContextHolder.getContext().setAuthentication(
                            UsernamePasswordAuthenticationToken.authenticated(user, null, AUTHORITIES)),
                    () -> response.addHeader(HttpHeaders.SET_COOKIE, cookies.clear().toString()));
        }
        chain.doFilter(request, response);
    }
}
