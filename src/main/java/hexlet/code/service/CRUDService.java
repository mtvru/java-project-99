package hexlet.code.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

public interface CRUDService<ModelDTO, IndexDTO, CreateDTO, UpdateDTO> {
    Page<ModelDTO> findAll(IndexDTO params);
    ModelDTO findById(Long id);
    ModelDTO create(@Valid CreateDTO dto);
    ModelDTO update(Long id, @Valid UpdateDTO dto);
    void delete(Long id);
}
