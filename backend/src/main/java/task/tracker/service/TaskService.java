package task.tracker.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import task.tracker.domain.Tag;
import task.tracker.domain.Task;
import task.tracker.domain.TaskPriority;
import task.tracker.domain.TaskStatus;
import task.tracker.metrics.TrackerMetrics;
import task.tracker.repository.TagRepository;
import task.tracker.repository.TaskRepository;
import task.tracker.web.dto.TaskRequest;
import task.tracker.web.dto.TaskResponse;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository tasks;
    private final TagRepository tags;
    private final BoardService boardService;
    private final TrackerMetrics metrics;

    @Transactional(readOnly = true)
    public List<TaskResponse> findByBoard(Long boardId, TaskStatus status) {
        boardService.get(boardId);
        List<Task> result = status == null
                ? tasks.findAllByBoardIdOrderByIdAsc(boardId)
                : tasks.findAllByBoardIdAndStatusOrderByIdAsc(boardId, status);
        return result.stream().map(TaskResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(Long id) {
        return TaskResponse.from(get(id));
    }

    @Transactional
    public TaskResponse create(Long boardId, TaskRequest request) {
        Task task = new Task();
        task.setBoard(boardService.get(boardId));
        apply(task, request);
        Task saved = tasks.saveAndFlush(task);
        metrics.taskCreated(saved);
        return TaskResponse.from(saved);
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = get(id);
        TaskStatus before = task.getStatus();
        apply(task, request);
        Task saved = tasks.saveAndFlush(task);
        metrics.taskStatusChanged(saved, before);
        return TaskResponse.from(saved);
    }

    @Transactional
    public TaskResponse changeStatus(Long id, TaskStatus status) {
        Task task = get(id);
        TaskStatus before = task.getStatus();
        task.setStatus(status);
        Task saved = tasks.saveAndFlush(task);
        metrics.taskStatusChanged(saved, before);
        return TaskResponse.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        tasks.delete(get(id));
    }

    private Task get(Long id) {
        return tasks.findById(id).orElseThrow(() -> new NotFoundException("Задача", id));
    }

    private void apply(Task task, TaskRequest request) {
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setStatus(request.status() == null ? TaskStatus.TODO : request.status());
        task.setPriority(request.priority() == null ? TaskPriority.MEDIUM : request.priority());
        task.setDueDate(request.dueDate());
        task.setTags(resolveTags(request.tagIds()));
    }

    private Set<Tag> resolveTags(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Tag> found = tags.findAllById(tagIds);
        if (found.size() != tagIds.size()) {
            Set<Long> missing = new HashSet<>(tagIds);
            found.forEach(t -> missing.remove(t.getId()));
            throw new NotFoundException("Тег", missing.iterator().next());
        }
        return new HashSet<>(found);
    }
}
