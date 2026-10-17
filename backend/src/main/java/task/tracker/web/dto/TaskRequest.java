package task.tracker.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;
import task.tracker.domain.TaskPriority;
import task.tracker.domain.TaskStatus;

public record TaskRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 4000) String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        Set<Long> tagIds) {}
