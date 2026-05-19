package org.project.projemento.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import org.project.projemento.domain.enums.BoardColumnType;

@Getter
@Entity
@Table(
        name = "board_columns",
        uniqueConstraints = @UniqueConstraint(name = "uk_board_columns_board_type", columnNames = {"board_id", "type"})
)
public class BoardColumn extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private ProjectBoard board;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private BoardColumnType type;

    @Column(name = "title", nullable = false, length = 80)
    private String title;

    @Column(name = "position", nullable = false)
    private int position;

    protected BoardColumn() {
    }

    public BoardColumn(ProjectBoard board, BoardColumnType type) {
        this.board = board;
        this.type = type;
        this.title = type.getTitle();
        this.position = type.getPosition();
    }
}
