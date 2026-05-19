package org.project.projemento.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.project.projemento.config.OpenApiConfig;
import org.project.projemento.dto.BoardResponse;
import org.project.projemento.dto.CommentCreateRequest;
import org.project.projemento.dto.CommentResponse;
import org.project.projemento.dto.ProjectCreateRequest;
import org.project.projemento.dto.ProjectMemberAddRequest;
import org.project.projemento.dto.ProjectMemberResponse;
import org.project.projemento.dto.ProjectResponse;
import org.project.projemento.dto.TaskCreateRequest;
import org.project.projemento.dto.TaskResponse;
import org.project.projemento.dto.TaskStatusUpdateRequest;
import org.project.projemento.dto.TaskUpdateRequest;
import org.project.projemento.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Projects", description = "Projects, boards, tasks and comments")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a project with default kanban board")
    public ProjectResponse createProject(@Valid @RequestBody ProjectCreateRequest request, Authentication authentication) {
        return projectService.createProject(request, authentication.getName());
    }

    @GetMapping
    @Operation(summary = "List projects available to current user")
    public List<ProjectResponse> listProjects(Authentication authentication) {
        return projectService.listProjects(authentication.getName());
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "Get project by id")
    public ProjectResponse getProject(@PathVariable Long projectId, Authentication authentication) {
        return projectService.getProject(projectId, authentication.getName());
    }

    @PostMapping("/{projectId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add project member")
    public ProjectMemberResponse addMember(@PathVariable Long projectId,
                                           @Valid @RequestBody ProjectMemberAddRequest request,
                                           Authentication authentication) {
        return projectService.addMember(projectId, request, authentication.getName());
    }

    @GetMapping("/{projectId}/board")
    @Operation(summary = "Get kanban board with tasks grouped by columns")
    public BoardResponse getBoard(@PathVariable Long projectId, Authentication authentication) {
        return projectService.getBoard(projectId, authentication.getName());
    }

    @PostMapping("/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create task in project")
    public TaskResponse createTask(@PathVariable Long projectId,
                                   @Valid @RequestBody TaskCreateRequest request,
                                   Authentication authentication) {
        return projectService.createTask(projectId, request, authentication.getName());
    }

    @GetMapping("/{projectId}/tasks")
    @Operation(summary = "List project tasks")
    public List<TaskResponse> listTasks(@PathVariable Long projectId, Authentication authentication) {
        return projectService.listTasks(projectId, authentication.getName());
    }

    @PatchMapping("/{projectId}/tasks/{taskId}")
    @Operation(summary = "Update task fields")
    public TaskResponse updateTask(@PathVariable Long projectId,
                                   @PathVariable Long taskId,
                                   @Valid @RequestBody TaskUpdateRequest request,
                                   Authentication authentication) {
        return projectService.updateTask(projectId, taskId, request, authentication.getName());
    }

    @PatchMapping("/{projectId}/tasks/{taskId}/status")
    @Operation(summary = "Move task to another board column")
    public TaskResponse updateTaskStatus(@PathVariable Long projectId,
                                         @PathVariable Long taskId,
                                         @Valid @RequestBody TaskStatusUpdateRequest request,
                                         Authentication authentication) {
        return projectService.updateTaskStatus(projectId, taskId, request, authentication.getName());
    }

    @PostMapping("/{projectId}/tasks/{taskId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add task comment")
    public CommentResponse addComment(@PathVariable Long projectId,
                                      @PathVariable Long taskId,
                                      @Valid @RequestBody CommentCreateRequest request,
                                      Authentication authentication) {
        return projectService.addComment(projectId, taskId, request, authentication.getName());
    }

    @GetMapping("/{projectId}/tasks/{taskId}/comments")
    @Operation(summary = "List task comments")
    public List<CommentResponse> listComments(@PathVariable Long projectId,
                                              @PathVariable Long taskId,
                                              Authentication authentication) {
        return projectService.listComments(projectId, taskId, authentication.getName());
    }
}
