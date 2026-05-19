package org.project.projemento.dto;

import org.project.projemento.domain.enums.BoardColumnType;

import java.util.List;

public record BoardColumnResponse(
        Long id,
        BoardColumnType type,
        String title,
        int position,
        List<TaskResponse> tasks
) {
}
