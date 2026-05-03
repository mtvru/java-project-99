package hexlet.code.service;

import hexlet.code.dto.IndexDTO;
import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@AllArgsConstructor
@Service
@Validated
public class TaskStatusService {
    private final TaskStatusRepository repository;
    private final TaskStatusMapper mapper;

    /**
     * Get all task statuses.
     * @param dto index data
     * @return page of task statuses
     */
    public Page<TaskStatusDTO> findAll(IndexDTO dto) {
        Pageable pageable = mapper.map(dto);
        Page<TaskStatus> statuses = this.repository.findAll(pageable);
        return statuses.map(this.mapper::map);
    }

    /**
     * Find task status by id.
     * @param id status id
     * @return task status
     * @throws ResourceNotFoundException if the status is not found
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
     * @throws ResourceNotFoundException if the status is not found
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
     * @throws ResourceNotFoundException if the status is not found
     * @throws RuntimeException if the status is linked to tasks
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
