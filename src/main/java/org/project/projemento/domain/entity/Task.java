package org.project.projemento.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.project.projemento.domain.enums.TaskPriority;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "tasks")
public class Task extends BaseEntity {
    @Setter
    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Setter
    @Column(name = "description", length = 4000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "column_id", nullable = false)
    private BoardColumn column;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Setter
    @Column(name = "deadline")
    private LocalDate deadline;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private TaskPriority priority = TaskPriority.MEDIUM;

    protected Task() {
    }

    public Task(String title, String description, Project project, BoardColumn column, User assignee,
                LocalDate deadline, TaskPriority priority) {
        this.title = title;
        this.description = description;
        this.project = project;
        this.column = column;
        this.assignee = assignee;
        this.deadline = deadline;
        this.priority = priority == null ? TaskPriority.MEDIUM : priority;
    }
}
