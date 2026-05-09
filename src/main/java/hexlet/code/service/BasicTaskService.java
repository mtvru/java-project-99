package hexlet.code.service;

import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.dto.TaskParamsDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.ReferenceMapper;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.specification.TaskSpecification;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Service
@Validated
public class BasicTaskService extends AbstractBasicService<Task, TaskDTO, TaskCreateDTO, TaskUpdateDTO>
        implements TaskService {
    private final TaskMapper mapper;
    private final TaskSpecification specification;
    private final TaskStatusRepository taskStatusRepository;
    private final UserRepository userRepository;
    private final ReferenceMapper referenceMapper;

    public BasicTaskService(
        TaskMapper mapper,
        TaskSpecification specification,
        TaskRepository repository,
        TaskStatusRepository taskStatusRepository,
        UserRepository userRepository,
        ReferenceMapper referenceMapper
    ) {
        super(repository);
        this.mapper = mapper;
        this.specification = specification;
        this.taskStatusRepository = taskStatusRepository;
        this.userRepository = userRepository;
        this.referenceMapper = referenceMapper;
    }

    /**
     * Get all tasks filtered by parameters.
     * @param params filter and pagination data
     * @param pageable pagination and sorting information
     * @return page of tasks
     */
    public Page<TaskDTO> findAll(TaskParamsDTO params, Pageable pageable) {
        Specification<Task> spec = specification.build(params);
        Page<Task> tasks = ((TaskRepository) this.getRepository()).findAll(spec, pageable);
        return tasks.map(this.mapper::map);
    }

    /**
     * Update existing entity.
     * @param id entity id
     * @param dto new data
     * @return updated entity data
     */
    @Override
    @Transactional
    public TaskDTO update(Long id, @Valid TaskUpdateDTO dto) {
        TaskStatus status = null;
        boolean statusSet = false;
        if (dto.getTaskStatus() != null && dto.getTaskStatus().isPresent()) {
            String slug = dto.getTaskStatus().get();
            status = taskStatusRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Status not found"));
            statusSet = true;
        }

        User assignee = null;
        boolean assigneeSet = false;
        if (dto.getAssigneeId() != null && dto.getAssigneeId().isPresent()) {
            Long assigneeId = dto.getAssigneeId().get();
            if (assigneeId != null) {
                assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            }
            assigneeSet = true;
        }

        int updated = ((TaskRepository) getRepository()).updateAtomic(
            id,
            dto.getName() == null ? null : dto.getName().orElse(null),
            dto.getName() != null && dto.getName().isPresent(),
            dto.getIndex() == null ? null : dto.getIndex().orElse(null),
            dto.getIndex() != null && dto.getIndex().isPresent(),
            dto.getDescription() == null ? null : dto.getDescription().orElse(null),
            dto.getDescription() != null && dto.getDescription().isPresent(),
            status == null ? null : status.getId(),
            statusSet,
            assignee == null ? null : assignee.getId(),
            assigneeSet
        );

        if (updated == 0) {
            throw new ResourceNotFoundException(getEntityNotFoundMessage(id));
        }

        if (dto.getTaskLabelIds() != null && dto.getTaskLabelIds().isPresent()) {
            Task task = getRepository().findById(id).get();
            Set<Long> labelIds = dto.getTaskLabelIds().get();
            Set<Label> labels = referenceMapper.toEntitySet(labelIds, Label.class);
            task.getLabels().clear();
            if (labels != null) {
                task.getLabels().addAll(labels);
            }
            getRepository().save(task);
        }

        return findById(id);
    }

    /**
     * Convert Task to DTO.
     * @param model entity
     * @return DTO
     */
    @Override
    protected TaskDTO toDTO(Task model) {
        return this.mapper.map(model);
    }
    /**
     * Convert DTO to Task.
     * @param dto create DTO
     * @return entity
     */
    @Override
    protected Task toEntity(TaskCreateDTO dto) {
        return this.mapper.map(dto);
    }
    /**
     * Update Task with DTO data.
     * @param dto update DTO
     * @param model entity to update
     */
    @Override
    protected void toUpdate(TaskUpdateDTO dto, Task model) {
        this.mapper.update(dto, model);
    }
    /**
     * Get error message for non-existent Task.
     * @param id entity id
     * @return error message
     */
    @Override
    protected String getEntityNotFoundMessage(Long id) {
        return String.format("Task with id %d not found", id);
    }
}
