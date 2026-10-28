package dev.memsto.storage;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component("storage")
@RequiredArgsConstructor
public class StorageHealthIndicator implements HealthIndicator {

    private final StorageService storage;

    @Override
    public Health health() {
        try {
            storage.checkBucket();
            return Health.up().build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
