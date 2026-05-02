package hexlet.code.app.service;

import hexlet.code.app.dto.IndexDTO;
import hexlet.code.app.dto.LabelCreateDTO;
import hexlet.code.app.dto.LabelDTO;
import hexlet.code.app.dto.LabelUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.LabelMapper;
import hexlet.code.app.model.Label;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@AllArgsConstructor
@Service
@Validated
public class LabelService {
    private final LabelRepository repository;
    private final TaskRepository taskRepository;
    private final LabelMapper mapper;

    /**
     * Get a list of all labels.
     * @param dto index data
     * @return page of labels
     */
    public Page<LabelDTO> findAll(IndexDTO dto) {
        Pageable pageable = mapper.map(dto);
        Page<Label> labels = this.repository.findAll(pageable);
        return labels.map(this.mapper::map);
    }

    /**
     * Find a label by identifier.
     * @param id label identifier
     * @return found label DTO
     * @throws ResourceNotFoundException if the label is not found
     */
    public LabelDTO findById(Long id) {
        Label label = this.repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label with id " + id + " not found"));
        return this.mapper.map(label);
    }

    /**
     * Create a new label.
     * @param dto label creation data
     * @return created label DTO
     */
    public LabelDTO create(@Valid LabelCreateDTO dto) {
        Label label = this.mapper.map(dto);
        this.repository.save(label);
        return this.mapper.map(label);
    }

    /**
     * Update an existing label.
     * @param id label identifier
     * @param dto update data
     * @return updated label DTO
     * @throws ResourceNotFoundException if the label is not found
     */
    public LabelDTO update(Long id, @Valid LabelUpdateDTO dto) {
        Label label = this.repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label with id " + id + " not found"));
        this.mapper.update(dto, label);
        this.repository.save(label);
        return this.mapper.map(label);
    }

    /**
     * Delete a label by identifier.
     * Checks if the label is linked to tasks before deletion.
     * @param id label identifier
     * @throws ResourceNotFoundException if the label is not found
     * @throws RuntimeException if the label is linked to tasks
     */
    public void delete(Long id) {
        Label label = this.repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label with id " + id + " not found"));
        if (this.taskRepository.existsByTaskLabelsLabel(label)) {
            throw new RuntimeException("Cannot delete label linked to tasks");
        }
        this.repository.deleteById(id);
    }
}
