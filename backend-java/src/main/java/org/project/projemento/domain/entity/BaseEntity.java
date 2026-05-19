package org.project.projemento.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@MappedSuperclass
public class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @JsonIgnore
    public boolean isNew(){
        return this.id == null;
    }

    @PrePersist
    protected void onCreate(){
        if (this.createdAt == null){
            this.createdAt = Instant.now();
        }
    }
}