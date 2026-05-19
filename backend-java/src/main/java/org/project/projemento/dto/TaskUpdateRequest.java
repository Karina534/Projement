package org.project.projemento.dto;

import jakarta.validation.constraints.Size;
import org.project.projemento.domain.enums.TaskPriority;

import java.time.LocalDate;

public record TaskUpdateRequest(
        @Size(max = 160, message = "Task title must be at most {max} characters")
        String title,

        @Size(max = 4000, message = "Task description must be at most {max} characters")
        String description,

        Long assigneeId,
        LocalDate deadline,
        TaskPriority priority
) {
}
