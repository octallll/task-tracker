package task.tracker.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import task.tracker.domain.Tag;
import task.tracker.repository.TagRepository;
import task.tracker.web.dto.TagRequest;
import task.tracker.web.dto.TagResponse;

@Service
@RequiredArgsConstructor
public class TagService {

    private static final String DEFAULT_COLOR = "#6b7280";

    private final TagRepository tags;

    @Transactional(readOnly = true)
    public List<TagResponse> findAll() {
        return tags.findAllByOrderByNameAsc().stream().map(TagResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TagResponse findById(Long id) {
        return TagResponse.from(get(id));
    }

    @Transactional
    public TagResponse create(TagRequest request) {
        Tag tag = new Tag();
        apply(tag, request);
        return TagResponse.from(tags.saveAndFlush(tag));
    }

    @Transactional
    public TagResponse update(Long id, TagRequest request) {
        Tag tag = get(id);
        apply(tag, request);
        return TagResponse.from(tags.saveAndFlush(tag));
    }

    @Transactional
    public void delete(Long id) {
        tags.delete(get(id));
    }

    Tag get(Long id) {
        return tags.findById(id).orElseThrow(() -> new NotFoundException("Тег", id));
    }

    private static void apply(Tag tag, TagRequest request) {
        tag.setName(request.name().trim());
        tag.setColor(request.color() == null ? DEFAULT_COLOR : request.color());
    }
}
