package hexlet.code.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CRUDService<ModelDTO, CreateDTO, UpdateDTO> {
    /**
     * Get all entities.
     * @param pageable pagination and sorting information
     * @return page of entities
     */
    Page<ModelDTO> findAll(Pageable pageable);

    /**
     * Get entity by id.
     * @param id entity id
     * @return entity data
     */
    ModelDTO findById(Long id);

    /**
     * Create a new entity.
     * @param dto data for creation
     * @return created entity data
     */
    ModelDTO create(@Valid CreateDTO dto);

    /**
     * Update existing entity.
     * @param id entity id
     * @param dto new data
     * @return updated entity data
     */
    ModelDTO update(Long id, @Valid UpdateDTO dto);

    /**
     * Delete entity by id.
     * @param id entity id
     */
    void delete(Long id);
}
