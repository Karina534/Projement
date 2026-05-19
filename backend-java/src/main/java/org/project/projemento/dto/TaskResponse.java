package org.project.projemento.dto;

import org.project.projemento.domain.enums.BoardColumnType;
import org.project.projemento.domain.enums.TaskPriority;

import java.time.Instant;
import java.time.LocalDate;

public record TaskResponse(
        Long id,
        String title,
        String description,
        Long projectId,
        BoardColumnType status,
        UserResponse assignee,
        LocalDate deadline,
        TaskPriority priority,
        Instant createdAt
) {
}
