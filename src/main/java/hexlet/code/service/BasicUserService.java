package hexlet.code.service;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.model.enums.UserRole;
import hexlet.code.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class BasicUserService extends AbstractBasicService<User, UserDTO, UserCreateDTO, UserUpdateDTO>
        implements UserDetailsService {
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public BasicUserService(UserMapper mapper, UserRepository repository, PasswordEncoder passwordEncoder) {
        super(repository);
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Load user by username.
     * @param email user email
     * @return UserDetails
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return ((UserRepository) this.getRepository()).findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Create a new User.
     * @param dto data for creation
     * @return created user data
     */
    @Override
    public UserDTO create(@Valid UserCreateDTO dto) {
        User user = this.mapper.map(dto);
        user.setRole(UserRole.USER);
        this.getRepository().save(user);
        return this.mapper.map(user);
    }

    /**
     * Update existing entity.
     * @param id entity id
     * @param dto new data
     * @return updated entity data
     */
    @Override
    @Transactional
    public UserDTO update(Long id, @Valid UserUpdateDTO dto) {
        String password = null;
        boolean passwordSet = false;
        if (dto.getPassword() != null && dto.getPassword().isPresent()) {
            password = passwordEncoder.encode(dto.getPassword().get());
            passwordSet = true;
        }

        int updated = ((UserRepository) getRepository()).updateAtomic(
            id,
            dto.getFirstName() == null ? null : dto.getFirstName().orElse(null),
            dto.getFirstName() != null && dto.getFirstName().isPresent(),
            dto.getLastName() == null ? null : dto.getLastName().orElse(null),
            dto.getLastName() != null && dto.getLastName().isPresent(),
            dto.getEmail() == null ? null : dto.getEmail().orElse(null),
            dto.getEmail() != null && dto.getEmail().isPresent(),
            password,
            passwordSet
        );

        if (updated == 0) {
            throw new ResourceNotFoundException(getEntityNotFoundMessage(id));
        }

        return findById(id);
    }

    /**
     * Convert User to DTO.
     * @param model entity
     * @return DTO
     */
    @Override
    protected UserDTO toDTO(User model) {
        return mapper.map(model);
    }
    /**
     * Convert DTO to User.
     * @param dto create DTO
     * @return entity
     */
    @Override
    protected User toEntity(UserCreateDTO dto) {
        return mapper.map(dto);
    }
    /**
     * Update User with DTO data.
     * @param dto update DTO
     * @param model entity to update
     */
    @Override
    protected void toUpdate(UserUpdateDTO dto, User model) {
        mapper.update(dto, model);
    }
    /**
     * Get error message for non-existent User.
     * @param id entity id
     * @return error message
     */
    @Override
    protected String getEntityNotFoundMessage(Long id) {
        return String.format("User with id %d not found", id);
    }
}
