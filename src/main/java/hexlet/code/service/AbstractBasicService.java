package hexlet.code.service;

import hexlet.code.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

@AllArgsConstructor
@Getter
public abstract class AbstractBasicService<Model, ModelDTO, CreateDTO, UpdateDTO>
        implements CRUDService<ModelDTO, CreateDTO, UpdateDTO> {
    private JpaRepository<Model, Long> repository;

    /**
     * Convert entity to DTO.
     * @param model entity
     * @return DTO
     */
    protected abstract ModelDTO toDTO(Model model);

    /**
     * Convert DTO to an entity.
     * @param dto create DTO
     * @return entity
     */
    protected abstract Model toEntity(CreateDTO dto);

    /**
     * Update entity with DTO data.
     * @param dto update DTO
     * @param model entity to update
     */
    protected abstract void toUpdate(UpdateDTO dto, Model model);

    /**
     * Get error message for non-existent entity.
     * @param id entity id
     * @return error message
     */
    protected abstract String getEntityNotFoundMessage(Long id);

    /**
     * Get all entities.
     * @param pageable pagination and sorting information
     * @return page of entities
     */
    @Override
    public Page<ModelDTO> findAll(Pageable pageable) {
        Page<Model> entities = this.repository.findAll(pageable);
        return entities.map(this::toDTO);
    }

    /**
     * Get entity by id.
     * @param id entity id
     * @return entity data
     */
    @Override
    public ModelDTO findById(Long id) {
        Model entity = this.repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(getEntityNotFoundMessage(id)));
        return toDTO(entity);
    }

    /**
     * Create a new entity.
     * @param dto data for creation
     * @return created entity data
     */
    @Override
    public ModelDTO create(@Valid CreateDTO dto) {
        Model entity = toEntity(dto);
        this.repository.save(entity);
        return toDTO(entity);
    }

    /**
     * Update existing entity.
     * @param id entity id
     * @param dto new data
     * @return updated entity data
     */
    @Override
    public ModelDTO update(Long id, @Valid UpdateDTO dto) {
        Model entity = this.repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(getEntityNotFoundMessage(id)));
        this.toUpdate(dto, entity);
        this.repository.save(entity);
        return toDTO(entity);
    }

    /**
     * Delete entity by id.
     * @param id entity id
     */
    @Override
    public void delete(Long id) {
        this.repository.deleteById(id);
    }
}
