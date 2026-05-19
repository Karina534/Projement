package org.project.projemento.repository;

import org.project.projemento.domain.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("""
            select p from Project p
            join ProjectMember m on m.project = p
            where m.user.email = :email
            order by p.createdAt desc
            """)
    List<Project> findAllAccessibleByUserEmail(@Param("email") String email);

    @Query("""
            select p from Project p
            join ProjectMember m on m.project = p
            where p.id = :projectId and m.user.email = :email
            """)
    Optional<Project> findAccessibleByIdAndUserEmail(@Param("projectId") Long projectId, @Param("email") String email);
}
