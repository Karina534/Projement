package org.project.projemento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectCreateRequest(
        @NotBlank(message = "Project name is required")
        @Size(max = 120, message = "Project name must be at most {max} characters")
        String name,

        @Size(max = 2000, message = "Project description must be at most {max} characters")
        String description
) {
}
