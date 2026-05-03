package hexlet.code.mapper;


import hexlet.code.dto.IndexDTO;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.model.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.BeforeMapping;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(
    uses = {JsonNullableMapper.class},
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class UserMapper {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private QueryMapper queryMapper;

    // Creation: standard behavior (null -> null)
    public abstract User map(UserCreateDTO dto);
    public abstract UserDTO map(User model);

    // Partial update.
    // Explicitly specify IGNORE so that null fields in the DTO don't affect the entity.
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void update(UserUpdateDTO dto, @MappingTarget User model);

    @BeforeMapping
    public final void encryptPassword(UserCreateDTO dto, @MappingTarget User user) {
        String password = dto.getPassword();
        if (password != null) {
            user.setPassword(this.passwordEncoder.encode(password));
        }
    }

    @BeforeMapping
    public final void encryptPassword(UserUpdateDTO dto, @MappingTarget User user) {
        JsonNullable<String> password = dto.getPassword();
        if (password != null && password.isPresent()) {
            user.setPassword(this.passwordEncoder.encode(password.get()));
        }
    }

    public final Pageable map(IndexDTO dto) {
        return queryMapper.toPageable(dto);
    }
}
