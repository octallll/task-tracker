package task.tracker.fault;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(FaultProperties.class)
@RequiredArgsConstructor
@Slf4j
public class FaultInjectionConfig implements WebMvcConfigurer {

    private final FaultProperties props;

    @PostConstruct
    void logEnabled() {
        if (props.enabled()) {
            log.atWarn()
                    .addKeyValue("fault.error_rate", props.errorRate())
                    .addKeyValue("fault.delay_ms", props.delayMs())
                    .log("Fault injection enabled: error rate {}, delay {} ms", props.errorRate(), props.delayMs());
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (props.enabled()) {
            registry.addInterceptor(new FaultInjectionInterceptor(props)).addPathPatterns("/api/**");
        }
    }
}
