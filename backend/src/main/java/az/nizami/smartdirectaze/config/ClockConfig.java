package az.nizami.smartdirectaze.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {

    // Injected where time matters (pauses, throttling), so tests can fix it
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
