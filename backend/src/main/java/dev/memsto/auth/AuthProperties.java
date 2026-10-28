package dev.memsto.auth;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth")
public record AuthProperties(
        String jwtSecret,
        Duration tokenTtl,
        String cookieName,
        boolean cookieSecure) {
}
