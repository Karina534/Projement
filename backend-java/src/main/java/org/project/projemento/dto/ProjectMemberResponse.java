package org.project.projemento.dto;

import org.project.projemento.domain.enums.ProjectMemberRole;

public record ProjectMemberResponse(
        Long id,
        UserResponse user,
        ProjectMemberRole role
) {
}
