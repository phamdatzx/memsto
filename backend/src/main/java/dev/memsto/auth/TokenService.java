package dev.memsto.auth;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

/** Issues and verifies HS256-signed session tokens. */
@Service
public class TokenService {

    private static final String ISSUER = "memsto";

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final AuthProperties props;

    public TokenService(AuthProperties props) {
        byte[] secret = props.jwtSecret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("auth.jwt-secret must be at least 32 bytes");
        }
        SecretKey key = new SecretKeySpec(secret, "HmacSHA256");
        this.encoder = NimbusJwtEncoder.withSecretKey(key).build();
        this.decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        this.props = props;
    }

    public String issue(AuthUser user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(Long.toString(user.id()))
                .claim("email", user.email())
                .issuedAt(now)
                .expiresAt(now.plus(props.tokenTtl()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    /** Returns the user for a valid, unexpired token; empty otherwise. */
    public Optional<AuthUser> verify(String token) {
        try {
            Jwt jwt = decoder.decode(token);
            if (!ISSUER.equals(jwt.getClaimAsString("iss"))) {
                return Optional.empty();
            }
            return Optional.of(new AuthUser(Long.parseLong(jwt.getSubject()), jwt.getClaimAsString("email")));
        } catch (JwtException | NumberFormatException e) {
            return Optional.empty();
        }
    }
}
