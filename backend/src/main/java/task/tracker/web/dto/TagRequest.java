package task.tracker.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TagRequest(
        @NotBlank @Size(max = 50) String name,
        @Pattern(regexp = "^#[0-9a-fA-F]{6}$") String color) {}
