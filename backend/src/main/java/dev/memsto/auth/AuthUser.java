package dev.memsto.auth;

/** The authenticated principal, as carried in the session token. */
public record AuthUser(long id, String email) {
}
