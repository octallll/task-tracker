package task.tracker.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import task.tracker.domain.Board;
import task.tracker.repository.BoardRepository;
import task.tracker.repository.TaskRepository;
import task.tracker.web.dto.BoardRequest;
import task.tracker.web.dto.BoardResponse;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boards;
    private final TaskRepository tasks;

    @Transactional(readOnly = true)
    public List<BoardResponse> findAll() {
        Map<Long, Long> counts = tasks.countByBoard();
        return boards.findAllByOrderByIdAsc().stream()
                .map(b -> BoardResponse.from(b, counts.getOrDefault(b.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public BoardResponse findById(Long id) {
        return BoardResponse.from(get(id), tasks.countByBoard().getOrDefault(id, 0L));
    }

    @Transactional
    public BoardResponse create(BoardRequest request) {
        Board board = new Board();
        apply(board, request);
        return BoardResponse.from(boards.save(board), 0);
    }

    @Transactional
    public BoardResponse update(Long id, BoardRequest request) {
        Board board = get(id);
        apply(board, request);
        return BoardResponse.from(board, tasks.countByBoard().getOrDefault(id, 0L));
    }

    @Transactional
    public void delete(Long id) {
        boards.delete(get(id));
    }

    Board get(Long id) {
        return boards.findById(id).orElseThrow(() -> new NotFoundException("Доска", id));
    }

    private static void apply(Board board, BoardRequest request) {
        board.setName(request.name().trim());
        board.setDescription(request.description());
    }
}
