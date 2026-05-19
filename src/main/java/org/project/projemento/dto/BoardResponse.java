package org.project.projemento.dto;

import java.util.List;

public record BoardResponse(
        Long id,
        Long projectId,
        List<BoardColumnResponse> columns
) {
}
