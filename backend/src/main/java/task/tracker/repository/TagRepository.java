package task.tracker.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import task.tracker.domain.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findAllByOrderByNameAsc();
}
