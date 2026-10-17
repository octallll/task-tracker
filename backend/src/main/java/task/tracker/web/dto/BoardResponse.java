package task.tracker.web.dto;

import java.time.Instant;
import task.tracker.domain.Board;

public record BoardResponse(Long id, String name, String description, long taskCount, Instant createdAt) {

    public static BoardResponse from(Board board, long taskCount) {
        return new BoardResponse(
                board.getId(), board.getName(), board.getDescription(), taskCount, board.getCreatedAt());
    }
}
