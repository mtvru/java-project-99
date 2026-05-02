package hexlet.code.app.mapper;

import hexlet.code.app.dto.IndexDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING
)
public abstract class QueryMapper {
    private static final int DEFAULT_PAGE_SIZE = 10;

    public final Pageable toPageable(IndexDTO dto) {
        Integer startValue = dto.getStart() != null ? dto.getStart() : 0;
        Integer endValue = dto.getEnd() != null ? dto.getEnd() : DEFAULT_PAGE_SIZE;
        String sortValue = dto.getSort() != null ? dto.getSort() : "id";
        String orderValue = dto.getOrder() != null ? dto.getOrder() : "ASC";

        Sort sortOrder = Sort.by(sortValue);
        sortOrder = orderValue.equalsIgnoreCase("asc") ? sortOrder.ascending() : sortOrder.descending();

        int size = endValue - startValue;
        int page = startValue / size;
        return PageRequest.of(page, size, sortOrder);
    }
}
