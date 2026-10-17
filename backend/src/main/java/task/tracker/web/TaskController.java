package task.tracker.web;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import task.tracker.domain.TaskStatus;
import task.tracker.service.TaskService;
import task.tracker.web.dto.TaskRequest;
import task.tracker.web.dto.TaskResponse;
import task.tracker.web.dto.TaskStatusRequest;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService tasks;

    @GetMapping("/boards/{boardId}/tasks")
    public List<TaskResponse> list(
            @PathVariable Long boardId, @RequestParam(required = false) TaskStatus status) {
        return tasks.findByBoard(boardId, status);
    }

    @PostMapping("/boards/{boardId}/tasks")
    public ResponseEntity<TaskResponse> create(
            @PathVariable Long boardId, @Valid @RequestBody TaskRequest request) {
        TaskResponse created = tasks.create(boardId, request);
        return ResponseEntity.created(URI.create("/api/tasks/" + created.id())).body(created);
    }

    @GetMapping("/tasks/{id}")
    public TaskResponse get(@PathVariable Long id) {
        return tasks.findById(id);
    }

    @PutMapping("/tasks/{id}")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return tasks.update(id, request);
    }

    @PatchMapping("/tasks/{id}/status")
    public TaskResponse changeStatus(@PathVariable Long id, @Valid @RequestBody TaskStatusRequest request) {
        return tasks.changeStatus(id, request.status());
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tasks.delete(id);
        return ResponseEntity.noContent().build();
    }
}
