package task.tracker.web.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import task.tracker.domain.Tag;
import task.tracker.domain.Task;
import task.tracker.domain.TaskPriority;
import task.tracker.domain.TaskStatus;

public record TaskResponse(
        Long id,
        Long boardId,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        List<TagResponse> tags,
        Instant createdAt,
        Instant updatedAt) {

    public static TaskResponse from(Task task) {
        List<TagResponse> tags = task.getTags().stream()
                .sorted(Comparator.comparing(Tag::getName))
                .map(TagResponse::from)
                .toList();
        return new TaskResponse(
                task.getId(),
                task.getBoard().getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                tags,
                task.getCreatedAt(),
                task.getUpdatedAt());
    }
}
