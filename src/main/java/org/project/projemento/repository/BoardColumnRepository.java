package org.project.projemento.repository;

import org.project.projemento.domain.entity.BoardColumn;
import org.project.projemento.domain.enums.BoardColumnType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardColumnRepository extends JpaRepository<BoardColumn, Long> {
    List<BoardColumn> findByBoardProjectIdOrderByPositionAsc(Long projectId);

    Optional<BoardColumn> findByBoardProjectIdAndType(Long projectId, BoardColumnType type);
}
