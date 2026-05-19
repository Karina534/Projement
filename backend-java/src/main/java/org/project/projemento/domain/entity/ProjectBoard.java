package org.project.projemento.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "project_boards")
public class ProjectBoard extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    private Project project;

    protected ProjectBoard() {
    }

    public ProjectBoard(Project project) {
        this.project = project;
    }
}
