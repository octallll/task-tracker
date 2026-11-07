package task.tracker.fault;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fault")
public record FaultProperties(double errorRate, long delayMs) {

    public boolean enabled() {
        return errorRate > 0 || delayMs > 0;
    }
}
