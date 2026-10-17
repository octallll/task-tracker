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
import task.tracker.service.TagService;
import task.tracker.web.dto.TagRequest;
import task.tracker.web.dto.TagResponse;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tags;

    @GetMapping
    public List<TagResponse> list() {
        return tags.findAll();
    }

    @GetMapping("/{id}")
    public TagResponse get(@PathVariable Long id) {
        return tags.findById(id);
    }

    @PostMapping
    public ResponseEntity<TagResponse> create(@Valid @RequestBody TagRequest request) {
        TagResponse created = tags.create(request);
        return ResponseEntity.created(URI.create("/api/tags/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public TagResponse update(@PathVariable Long id, @Valid @RequestBody TagRequest request) {
        return tags.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tags.delete(id);
        return ResponseEntity.noContent().build();
    }
}
