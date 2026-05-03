package hexlet.code.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import hexlet.code.dto.IndexDTO;
import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.LabelUpdateDTO;
import hexlet.code.model.Label;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
    uses = {JsonNullableMapper.class},
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class LabelMapper {
    @Autowired
    private QueryMapper queryMapper;

    public abstract Label map(LabelCreateDTO dto);

    public abstract LabelDTO map(Label model);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void update(LabelUpdateDTO dto, @MappingTarget Label model);

    public final Pageable map(IndexDTO dto) {
        return queryMapper.toPageable(dto);
    }
}
