package task.tracker.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import task.tracker.domain.Task;
import task.tracker.domain.TaskStatus;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @EntityGraph(attributePaths = "tags")
    List<Task> findAllByBoardIdOrderByIdAsc(Long boardId);

    @EntityGraph(attributePaths = "tags")
    List<Task> findAllByBoardIdAndStatusOrderByIdAsc(Long boardId, TaskStatus status);

    long countByStatus(TaskStatus status);

    long countByDueDateBeforeAndStatusNot(LocalDate date, TaskStatus status);

    @Query("select t.board.id, count(t) from Task t group by t.board.id")
    List<Object[]> countGroupedByBoard();

    default Map<Long, Long> countByBoard() {
        return countGroupedByBoard().stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
    }
}
