package org.project.projemento.dto;

import jakarta.validation.constraints.NotNull;
import org.project.projemento.domain.enums.BoardColumnType;

public record TaskStatusUpdateRequest(
        @NotNull(message = "Task status is required")
        BoardColumnType status
) {
}
