package dev.memsto.auth;

import java.util.Locale;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.memsto.auth.AuthDtos.LoginRequest;
import dev.memsto.auth.AuthDtos.RegisterRequest;
import dev.memsto.user.User;
import dev.memsto.user.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    /** Hash checked when the email is unknown, so both failure paths take similar time. */
    private String dummyHash;

    public static class EmailTakenException extends RuntimeException {
    }

    public static class BadCredentialsException extends RuntimeException {
    }

    @Transactional
    public User register(RegisterRequest req) {
        String email = normalize(req.email());
        if (users.existsByEmail(email)) {
            throw new EmailTakenException();
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        String displayName = req.displayName() == null ? "" : req.displayName().strip();
        user.setDisplayName(displayName.isEmpty() ? null : displayName);
        return users.save(user);
    }

    @Transactional(readOnly = true)
    public User login(LoginRequest req) {
        Optional<User> user = users.findByEmail(normalize(req.email()));
        String hash = user.map(User::getPasswordHash).orElseGet(this::dummyHash);
        if (!passwordEncoder.matches(req.password(), hash) || user.isEmpty()) {
            throw new BadCredentialsException();
        }
        return user.get();
    }

    @Transactional(readOnly = true)
    public Optional<User> find(long id) {
        return users.findById(id);
    }

    private String dummyHash() {
        if (dummyHash == null) {
            dummyHash = passwordEncoder.encode("not-a-real-password");
        }
        return dummyHash;
    }

    private static String normalize(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }
}
