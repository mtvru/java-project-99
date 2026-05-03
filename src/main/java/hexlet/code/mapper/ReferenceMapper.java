package hexlet.code.mapper;

import hexlet.code.model.BaseEntity;
import jakarta.persistence.EntityManager;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.TargetType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING
)
public abstract class ReferenceMapper {
    @Autowired
    private EntityManager entityManager;

    public final <T extends BaseEntity> T toEntity(Long id, @TargetType Class<T> entityClass) {
        return id != null ? this.entityManager.find(entityClass, id) : null;
    }

    public final <T extends BaseEntity> Set<T> toEntitySet(Set<Long> ids, @TargetType Class<T> entityClass) {
        if (ids == null) {
            return null;
        }
        return ids.stream()
            .map(id -> this.toEntity(id, entityClass))
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
