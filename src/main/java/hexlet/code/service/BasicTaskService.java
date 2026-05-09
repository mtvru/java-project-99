package hexlet.code.service;

import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.dto.TaskParamsDTO;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.repository.TaskRepository;
import hexlet.code.specification.TaskSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class BasicTaskService extends AbstractBasicService<Task, TaskDTO, TaskCreateDTO, TaskUpdateDTO>
        implements TaskService {
    private final TaskMapper mapper;
    private final TaskSpecification specification;

    public BasicTaskService(TaskMapper mapper, TaskSpecification specification, TaskRepository repository) {
        super(repository);
        this.mapper = mapper;
        this.specification = specification;
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
