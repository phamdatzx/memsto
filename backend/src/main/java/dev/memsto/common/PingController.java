package dev.memsto.common;

import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PingController {

    public record Pong(String status, Instant time) {
    }

    @GetMapping("/ping")
    public Pong ping() {
        return new Pong("ok", Instant.now());
    }
}
