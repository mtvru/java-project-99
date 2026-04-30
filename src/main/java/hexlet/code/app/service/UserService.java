package hexlet.code.app.service;

import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.dto.UserDTO;
import hexlet.code.app.dto.UserIndexDTO;
import hexlet.code.app.dto.UserUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class UserService implements UserDetailsService {
    private static final int DEFAULT_PAGE_SIZE = 10;
    private final UserRepository repository;
    private final UserMapper mapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.repository = userRepository;
        this.mapper = userMapper;
    }

    /**
     * Load user by username.
     * @param email user email
     * @return UserDetails
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return this.repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Create user.
     * @param dto create data
     * @return created user
     */
    public UserDTO create(@Valid UserCreateDTO dto) {
        User user = this.mapper.map(dto);
        user = this.repository.save(user);
        return this.mapper.map(user);
    }

    /**
     * Find all users.
     * @param dto index data
     * @return page of users
     */
    public Page<UserDTO> findAll(UserIndexDTO dto) {
        Integer start = dto.getStart() != null ? dto.getStart() : 0;
        Integer end = dto.getEnd() != null ? dto.getEnd() : DEFAULT_PAGE_SIZE;
        String sort = dto.getSort() != null ? dto.getSort() : "id";
        String order = dto.getOrder() != null ? dto.getOrder() : "ASC";
        Sort sortOrder = Sort.by(sort);
        sortOrder = order.equalsIgnoreCase("asc") ? sortOrder.ascending() : sortOrder.descending();
        int size = end - start;
        int page = start / size;
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<User> users = this.repository.findAll(pageable);
        return users.map(this.mapper::map);
    }

    /**
     * Find user by id.
     * @param id user id
     * @return user
     */
    public UserDTO findById(Long id) {
        return this.repository.findById(id)
                .map(this.mapper::map)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
    }

    /**
     * Update user.
     * @param id user id
     * @param dto update data
     * @return updated user
     */
    public UserDTO update(Long id, @Valid UserUpdateDTO dto) {
        User user = this.repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        this.mapper.update(dto, user);
        this.repository.save(user);
        return this.mapper.map(user);
    }

    /**
     * Delete user.
     * @param id user id
     * @return true if deleted
     */
    public boolean delete(Long id) {
        if (!this.repository.existsById(id)) {
            return false;
        }
        this.repository.deleteById(id);
        return true;
    }
}
