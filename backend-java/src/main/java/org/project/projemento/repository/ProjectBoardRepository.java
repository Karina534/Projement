package org.project.projemento.repository;

import org.project.projemento.domain.entity.ProjectBoard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectBoardRepository extends JpaRepository<ProjectBoard, Long> {
    Optional<ProjectBoard> findByProjectId(Long projectId);
}
