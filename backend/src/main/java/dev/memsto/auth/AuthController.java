package dev.memsto.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.memsto.auth.AuthDtos.LoginRequest;
import dev.memsto.auth.AuthDtos.RegisterRequest;
import dev.memsto.auth.AuthDtos.UserResponse;
import dev.memsto.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService auth;
    private final TokenService tokens;
    private final SessionCookies cookies;

    /** Makes sure the XSRF-TOKEN cookie is set before the SPA sends its first POST. */
    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(CsrfToken token) {
        token.getToken();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        return withSession(HttpStatus.CREATED, auth.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest req) {
        return withSession(HttpStatus.OK, auth.login(req));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookies.clear().toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal AuthUser principal) {
        // The token can outlive the account; treat a deleted user as signed out.
        return auth.find(principal.id())
                .map(user -> ResponseEntity.ok(UserResponse.of(user)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .header(HttpHeaders.SET_COOKIE, cookies.clear().toString())
                        .build());
    }

    private ResponseEntity<UserResponse> withSession(HttpStatus status, User user) {
        String token = tokens.issue(new AuthUser(user.getId(), user.getEmail()));
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookies.create(token).toString())
                .body(UserResponse.of(user));
    }
}
