package org.project.projemento.repository;

import org.project.projemento.domain.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    List<TaskComment> findByTaskIdAndTaskProjectIdOrderByCreatedAtAsc(Long taskId, Long projectId);
}
