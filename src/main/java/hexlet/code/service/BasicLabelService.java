package hexlet.code.service;

import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.LabelUpdateDTO;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class BasicLabelService extends AbstractBasicService<Label, LabelDTO, LabelCreateDTO, LabelUpdateDTO> {
    private final LabelMapper mapper;

    public BasicLabelService(LabelMapper mapper, LabelRepository repository) {
        super(repository);
        this.mapper = mapper;
    }

    /**
     * Convert Label to DTO.
     * @param model entity
     * @return DTO
     */
    @Override
    protected LabelDTO toDTO(Label model) {
        return mapper.map(model);
    }
    /**
     * Convert DTO to Label.
     * @param dto create DTO
     * @return entity
     */
    @Override
    protected Label toEntity(LabelCreateDTO dto) {
        return mapper.map(dto);
    }
    /**
     * Update Label with DTO data.
     * @param dto update DTO
     * @param model entity to update
     */
    @Override
    protected void toUpdate(LabelUpdateDTO dto, Label model) {
        mapper.update(dto, model);
    }
    /**
     * Get error message for non-existent Label.
     * @param id entity id
     * @return error message
     */
    @Override
    protected String getEntityNotFoundMessage(Long id) {
        return String.format("Label with id %d not found", id);
    }

}
