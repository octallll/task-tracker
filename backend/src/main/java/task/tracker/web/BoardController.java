package task.tracker.web;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import task.tracker.service.BoardService;
import task.tracker.web.dto.BoardRequest;
import task.tracker.web.dto.BoardResponse;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boards;

    @GetMapping
    public List<BoardResponse> list() {
        return boards.findAll();
    }

    @GetMapping("/{id}")
    public BoardResponse get(@PathVariable Long id) {
        return boards.findById(id);
    }

    @PostMapping
    public ResponseEntity<BoardResponse> create(@Valid @RequestBody BoardRequest request) {
        BoardResponse created = boards.create(request);
        return ResponseEntity.created(URI.create("/api/boards/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public BoardResponse update(@PathVariable Long id, @Valid @RequestBody BoardRequest request) {
        return boards.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boards.delete(id);
        return ResponseEntity.noContent().build();
    }
}
