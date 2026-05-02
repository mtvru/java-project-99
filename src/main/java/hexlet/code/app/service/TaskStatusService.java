package hexlet.code.app.service;

import hexlet.code.app.dto.TaskStatusCreateDTO;
import hexlet.code.app.dto.TaskStatusDTO;
import hexlet.code.app.dto.TaskStatusUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.repository.TaskStatusRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@AllArgsConstructor
@Service
@Validated
public class TaskStatusService {
    private final TaskStatusRepository repository;
    private final TaskStatusMapper mapper;

    /**
     * Get all task statuses.
     * @return list of task statuses
     */
    public List<TaskStatusDTO> findAll() {
        return this.repository.findAll().stream()
                .map(this.mapper::map)
                .toList();
    }

    /**
     * Find task status by id.
     * @param id status id
     * @return task status
     */
    public TaskStatusDTO findById(Long id) {
        TaskStatus status = this.repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaskStatus with id " + id + " not found"));
        return this.mapper.map(status);
    }

    /**
     * Create a new task status.
     * @param dto status data
     * @return created task status
     */
    public TaskStatusDTO create(@Valid TaskStatusCreateDTO dto) {
        TaskStatus status = this.mapper.map(dto);
        this.repository.save(status);
        return this.mapper.map(status);
    }

    /**
     * Update task status.
     * @param id status id
     * @param dto new status data
     * @return updated task status
     */
    public TaskStatusDTO update(Long id, @Valid TaskStatusUpdateDTO dto) {
        TaskStatus status = this.repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaskStatus with id " + id + " not found"));
        this.mapper.update(dto, status);
        this.repository.save(status);
        return this.mapper.map(status);
    }

    /**
     * Delete task status.
     * @param id status id
     */
    public void delete(Long id) {
        TaskStatus status = this.repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaskStatus with id " + id + " not found"));
        if (this.repository.existsByTasks(status)) {
            throw new RuntimeException("Cannot delete status linked to tasks");
        }
        this.repository.deleteById(id);
    }
}
