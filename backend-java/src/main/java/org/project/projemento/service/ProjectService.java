package org.project.projemento.service;

import org.project.projemento.domain.entity.BoardColumn;
import org.project.projemento.domain.entity.Project;
import org.project.projemento.domain.entity.ProjectBoard;
import org.project.projemento.domain.entity.ProjectMember;
import org.project.projemento.domain.entity.Task;
import org.project.projemento.domain.entity.TaskComment;
import org.project.projemento.domain.entity.User;
import org.project.projemento.domain.enums.BoardColumnType;
import org.project.projemento.domain.enums.ProjectMemberRole;
import org.project.projemento.domain.enums.TaskPriority;
import org.project.projemento.dto.BoardColumnResponse;
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
import org.project.projemento.dto.UserResponse;
import org.project.projemento.repository.BoardColumnRepository;
import org.project.projemento.repository.ProjectBoardRepository;
import org.project.projemento.repository.ProjectMemberRepository;
import org.project.projemento.repository.ProjectRepository;
import org.project.projemento.repository.TaskCommentRepository;
import org.project.projemento.repository.TaskRepository;
import org.project.projemento.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectBoardRepository projectBoardRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;

    public ProjectService(UserRepository userRepository,
                          ProjectRepository projectRepository,
                          ProjectMemberRepository projectMemberRepository,
                          ProjectBoardRepository projectBoardRepository,
                          BoardColumnRepository boardColumnRepository,
                          TaskRepository taskRepository,
                          TaskCommentRepository taskCommentRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectBoardRepository = projectBoardRepository;
        this.boardColumnRepository = boardColumnRepository;
        this.taskRepository = taskRepository;
        this.taskCommentRepository = taskCommentRepository;
    }

    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request, String userEmail) {
        User owner = getUserByEmail(userEmail);
        Project project = projectRepository.save(new Project(request.name().trim(), request.description(), owner));
        projectMemberRepository.save(new ProjectMember(project, owner, ProjectMemberRole.OWNER));

        ProjectBoard board = projectBoardRepository.save(new ProjectBoard(project));
        Arrays.stream(BoardColumnType.values())
                .map(type -> new BoardColumn(board, type))
                .forEach(boardColumnRepository::save);

        return toProjectResponse(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects(String userEmail) {
        return projectRepository.findAllAccessibleByUserEmail(userEmail).stream()
                .map(this::toProjectResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long projectId, String userEmail) {
        return toProjectResponse(getAccessibleProject(projectId, userEmail));
    }

    @Transactional
    public ProjectMemberResponse addMember(Long projectId, ProjectMemberAddRequest request, String userEmail) {
        Project project = getAccessibleProject(projectId, userEmail);
        requireOwner(projectId, userEmail);

        User user = getUserByEmail(request.email());
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a project member");
        }

        return toProjectMemberResponse(projectMemberRepository.save(
                new ProjectMember(project, user, ProjectMemberRole.MEMBER)
        ));
    }

    @Transactional(readOnly = true)
    public BoardResponse getBoard(Long projectId, String userEmail) {
        getAccessibleProject(projectId, userEmail);
        ProjectBoard board = projectBoardRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project board not found"));
        List<Task> tasks = taskRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
        Map<Long, List<TaskResponse>> tasksByColumnId = tasks.stream()
                .collect(Collectors.groupingBy(task -> task.getColumn().getId(),
                        Collectors.mapping(this::toTaskResponse, Collectors.toList())));

        List<BoardColumnResponse> columns = boardColumnRepository.findByBoardProjectIdOrderByPositionAsc(projectId)
                .stream()
                .map(column -> new BoardColumnResponse(
                        column.getId(),
                        column.getType(),
                        column.getTitle(),
                        column.getPosition(),
                        tasksByColumnId.getOrDefault(column.getId(), List.of())
                ))
                .toList();

        return new BoardResponse(board.getId(), projectId, columns);
    }

    @Transactional
    public TaskResponse createTask(Long projectId, TaskCreateRequest request, String userEmail) {
        Project project = getAccessibleProject(projectId, userEmail);
        BoardColumnType status = request.status() == null ? BoardColumnType.BACKLOG : request.status();
        BoardColumn column = getColumn(projectId, status);
        User assignee = getAssignee(projectId, request.assigneeId());

        Task task = taskRepository.save(new Task(
                request.title().trim(),
                request.description(),
                project,
                column,
                assignee,
                request.deadline(),
                request.priority() == null ? TaskPriority.MEDIUM : request.priority()
        ));

        return toTaskResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long projectId, Long taskId, TaskUpdateRequest request, String userEmail) {
        getAccessibleProject(projectId, userEmail);
        Task task = getTask(projectId, taskId);

        if (request.title() != null) {
            if (request.title().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task title is required");
            }
            task.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.assigneeId() != null) {
            task.setAssignee(getAssignee(projectId, request.assigneeId()));
        }
        if (request.deadline() != null) {
            task.setDeadline(request.deadline());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }

        return toTaskResponse(task);
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long projectId, Long taskId, TaskStatusUpdateRequest request, String userEmail) {
        getAccessibleProject(projectId, userEmail);
        Task task = getTask(projectId, taskId);
        task.setColumn(getColumn(projectId, request.status()));
        return toTaskResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks(Long projectId, String userEmail) {
        getAccessibleProject(projectId, userEmail);
        return taskRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(this::toTaskResponse)
                .toList();
    }

    @Transactional
    public CommentResponse addComment(Long projectId, Long taskId, CommentCreateRequest request, String userEmail) {
        getAccessibleProject(projectId, userEmail);
        Task task = getTask(projectId, taskId);
        User author = getUserByEmail(userEmail);

        TaskComment comment = taskCommentRepository.save(new TaskComment(task, author, request.content().trim()));
        return toCommentResponse(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> listComments(Long projectId, Long taskId, String userEmail) {
        getAccessibleProject(projectId, userEmail);
        getTask(projectId, taskId);
        return taskCommentRepository.findByTaskIdAndTaskProjectIdOrderByCreatedAtAsc(taskId, projectId).stream()
                .map(this::toCommentResponse)
                .toList();
    }

    private Project getAccessibleProject(Long projectId, String userEmail) {
        return projectRepository.findAccessibleByIdAndUserEmail(projectId, userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private void requireOwner(Long projectId, String userEmail) {
        if (!projectMemberRepository.existsByProjectIdAndUserEmailAndRole(projectId, userEmail, ProjectMemberRole.OWNER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only project owner can perform this action");
        }
    }

    private BoardColumn getColumn(Long projectId, BoardColumnType status) {
        return boardColumnRepository.findByBoardProjectIdAndType(projectId, status)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task status is not available on project board"));
    }

    private Task getTask(Long projectId, Long taskId) {
        return taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    private User getAssignee(Long projectId, Long assigneeId) {
        if (assigneeId == null) {
            return null;
        }
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee not found"));
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, assigneeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee must be a project member");
        }
        return assignee;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private ProjectResponse toProjectResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                toUserResponse(project.getOwner()),
                project.getCreatedAt()
        );
    }

    private ProjectMemberResponse toProjectMemberResponse(ProjectMember projectMember) {
        return new ProjectMemberResponse(
                projectMember.getId(),
                toUserResponse(projectMember.getUser()),
                projectMember.getRole()
        );
    }

    private TaskResponse toTaskResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getProject().getId(),
                task.getColumn().getType(),
                task.getAssignee() == null ? null : toUserResponse(task.getAssignee()),
                task.getDeadline(),
                task.getPriority(),
                task.getCreatedAt()
        );
    }

    private CommentResponse toCommentResponse(TaskComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTask().getId(),
                toUserResponse(comment.getAuthor()),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
