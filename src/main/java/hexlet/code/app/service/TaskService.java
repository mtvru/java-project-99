package hexlet.code.app.service;

import hexlet.code.app.dto.IndexDTO;
import hexlet.code.app.dto.TaskCreateDTO;
import hexlet.code.app.dto.TaskDTO;
import hexlet.code.app.dto.TaskUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.model.Task;
import hexlet.code.app.repository.TaskRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@AllArgsConstructor
public class TaskService {
    private final TaskRepository repository;
    private final TaskMapper mapper;

    /**
     * Find all tasks.
     * @param dto index data
     * @return page of tasks
     */
    public Page<TaskDTO> findAll(IndexDTO dto) {
        Pageable pageable = mapper.map(dto);
        Page<Task> tasks = this.repository.findAll(pageable);
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
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
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
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
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
