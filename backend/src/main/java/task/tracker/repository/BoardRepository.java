package task.tracker.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import task.tracker.domain.Board;

public interface BoardRepository extends JpaRepository<Board, Long> {

    List<Board> findAllByOrderByIdAsc();
}
