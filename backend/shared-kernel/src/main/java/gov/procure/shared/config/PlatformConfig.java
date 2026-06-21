package gov.procure.shared.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Cross-cutting platform beans. {@link Clock} is injected everywhere time is needed (never call
 * {@code Instant.now()} directly in services) so behavior is deterministically testable.
 * Scheduling is enabled here for the outbox relay and SLA escalations.
 */
@Configuration
@EnableScheduling
public class PlatformConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
