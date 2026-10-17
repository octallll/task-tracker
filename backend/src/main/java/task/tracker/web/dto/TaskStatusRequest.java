package task.tracker.web.dto;

import jakarta.validation.constraints.NotNull;
import task.tracker.domain.TaskStatus;

public record TaskStatusRequest(@NotNull TaskStatus status) {}
