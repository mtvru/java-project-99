package hexlet.code.service;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskParamsDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.repository.TaskRepository;
import hexlet.code.specification.TaskSpecification;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@AllArgsConstructor
public class TaskService {
    private static final String TASK_NOT_FOUND_MESSAGE = "Task with id %d not found";

    private final TaskRepository repository;
    private final TaskMapper mapper;
    private final TaskSpecification specification;

    /**
     * Find all tasks.
     * @param params filter and pagination data
     * @return page of tasks
     */
    public Page<TaskDTO> findAll(TaskParamsDTO params) {
        Pageable pageable = mapper.map(params);
        Specification<Task> spec = specification.build(params);
        Page<Task> tasks = this.repository.findAll(spec, pageable);
        return tasks.map(this.mapper::map);
    }

    /**
     * Find a task by id.
     * @param id task id
     * @return task data
     * @throws ResourceNotFoundException if the task is not found
     */
    public TaskDTO findById(Long id) {
        Task task = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(TASK_NOT_FOUND_MESSAGE, id)));
        return mapper.map(task);
    }

    /**
     * Create a new task.
     * @param dto task creation data
     * @return created task data
     */
    public TaskDTO create(@Valid TaskCreateDTO dto) {
        Task task = mapper.map(dto);
        repository.save(task);
        return mapper.map(task);
    }

    /**
     * Update an existing task.
     * @param id task id
     * @param dto task update data
     * @return updated task data
     * @throws ResourceNotFoundException if the task is not found
     */
    public TaskDTO update(Long id, @Valid TaskUpdateDTO dto) {
        Task task = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(TASK_NOT_FOUND_MESSAGE, id)));
        mapper.update(dto, task);
        repository.save(task);
        return mapper.map(task);
    }

    /**
     * Delete task by id.
     * @param id task id
     */
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
