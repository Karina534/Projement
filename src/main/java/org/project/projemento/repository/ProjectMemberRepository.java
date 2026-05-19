package org.project.projemento.repository;

import org.project.projemento.domain.entity.ProjectMember;
import org.project.projemento.domain.enums.ProjectMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    boolean existsByProjectIdAndUserEmail(Long projectId, String email);

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    boolean existsByProjectIdAndUserEmailAndRole(Long projectId, String email, ProjectMemberRole role);

    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);
}
