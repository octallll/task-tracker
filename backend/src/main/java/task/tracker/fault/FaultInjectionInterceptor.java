package task.tracker.fault;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
public class FaultInjectionInterceptor implements HandlerInterceptor {

    private final FaultProperties props;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws InterruptedException {
        if (props.delayMs() > 0) {
            Thread.sleep(props.delayMs());
        }
        if (ThreadLocalRandom.current().nextDouble() < props.errorRate()) {
            throw new FaultInjectedException();
        }
        return true;
    }
}
