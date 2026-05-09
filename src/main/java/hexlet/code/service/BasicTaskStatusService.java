package hexlet.code.service;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class BasicTaskStatusService extends AbstractBasicService<TaskStatus, TaskStatusDTO,
        TaskStatusCreateDTO, TaskStatusUpdateDTO> {
    private final TaskStatusMapper mapper;

    public BasicTaskStatusService(TaskStatusMapper mapper, TaskStatusRepository repository) {
        super(repository);
        this.mapper = mapper;
    }

    /**
     * Update existing entity.
     * @param id entity id
     * @param dto new data
     * @return updated entity data
     */
    @Override
    @Transactional
    public TaskStatusDTO update(Long id, @Valid TaskStatusUpdateDTO dto) {
        int updated = ((TaskStatusRepository) getRepository()).updateAtomic(
            id,
            dto.getName() == null ? null : dto.getName().orElse(null),
            dto.getName() != null && dto.getName().isPresent(),
            dto.getSlug() == null ? null : dto.getSlug().orElse(null),
            dto.getSlug() != null && dto.getSlug().isPresent()
        );

        if (updated == 0) {
            throw new ResourceNotFoundException(getEntityNotFoundMessage(id));
        }

        return findById(id);
    }

    /**
     * Convert TaskStatus to DTO.
     * @param model entity
     * @return DTO
     */
    @Override
    protected TaskStatusDTO toDTO(TaskStatus model) {
        return mapper.map(model);
    }
    /**
     * Convert DTO to TaskStatus.
     * @param dto create DTO
     * @return entity
     */
    @Override
    protected TaskStatus toEntity(TaskStatusCreateDTO dto) {
        return mapper.map(dto);
    }
    /**
     * Update TaskStatus with DTO data.
     * @param dto update DTO
     * @param model entity to update
     */
    @Override
    protected void toUpdate(TaskStatusUpdateDTO dto, TaskStatus model) {
        mapper.update(dto, model);
    }
    /**
     * Get error message for non-existent TaskStatus.
     * @param id entity id
     * @return error message
     */
    @Override
    protected String getEntityNotFoundMessage(Long id) {
        return String.format("TaskStatus with id %d not found", id);
    }
}
