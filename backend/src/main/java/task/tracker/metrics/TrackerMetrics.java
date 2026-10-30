package task.tracker.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.stereotype.Component;
import task.tracker.domain.Task;
import task.tracker.domain.TaskStatus;
import task.tracker.repository.TaskRepository;

@Component
public class TrackerMetrics {

    private final MeterRegistry registry;
    private final Counter boardsCreated;
    private final Counter tasksCompleted;
    private final Timer taskLeadTime;

    public TrackerMetrics(MeterRegistry registry, TaskRepository tasks) {
        this.registry = registry;
        this.boardsCreated = Counter.builder("tracker.board.creations")
                .description("Number of created boards")
                .register(registry);
        this.tasksCompleted = Counter.builder("tracker.tasks.completed")
                .description("Number of tasks moved to DONE")
                .register(registry);
        this.taskLeadTime = Timer.builder("tracker.task.lead.time")
                .description("Time from task creation to moving it to DONE")
                .publishPercentileHistogram()
                .minimumExpectedValue(Duration.ofSeconds(1))
                .maximumExpectedValue(Duration.ofDays(30))
                .register(registry);

        for (TaskStatus status : TaskStatus.values()) {
            Gauge.builder("tracker.tasks", tasks, r -> r.countByStatus(status))
                    .description("Current number of tasks by status")
                    .tag("status", status.name())
                    .register(registry);
        }
        Gauge.builder("tracker.tasks.overdue", tasks,
                        r -> r.countByDueDateBeforeAndStatusNot(LocalDate.now(), TaskStatus.DONE))
                .description("Current number of overdue unfinished tasks")
                .register(registry);
    }

    public void boardCreated() {
        boardsCreated.increment();
    }

    public void taskCreated(Task task) {
        Counter.builder("tracker.task.creations")
                .description("Number of created tasks by priority")
                .tag("priority", task.getPriority().name())
                .register(registry)
                .increment();
    }

    public void taskStatusChanged(Task task, TaskStatus from) {
        TaskStatus to = task.getStatus();
        if (from == to) {
            return;
        }
        Counter.builder("tracker.task.status.changes")
                .description("Number of task status transitions")
                .tag("from", from.name())
                .tag("to", to.name())
                .register(registry)
                .increment();
        if (to == TaskStatus.DONE) {
            tasksCompleted.increment();
            taskLeadTime.record(Duration.between(task.getCreatedAt(), Instant.now()));
        }
    }
}
