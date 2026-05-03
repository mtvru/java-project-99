package hexlet.code.service;

import hexlet.code.dto.IndexDTO;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@AllArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository repository;
    private final UserMapper mapper;

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
     * Create a user.
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
    public Page<UserDTO> findAll(IndexDTO dto) {
        Pageable pageable = mapper.map(dto);
        Page<User> users = this.repository.findAll(pageable);
        return users.map(this.mapper::map);
    }

    /**
     * Find a user by id.
     * @param id user id
     * @return user
     * @throws ResourceNotFoundException if the user is not found
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
     * @throws ResourceNotFoundException if the user is not found
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
     * @throws ResourceNotFoundException if the user is not found
     * @throws RuntimeException if the user is linked to tasks
     */
    public void delete(Long id) {
        User user = this.repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        if (this.repository.existsByTasks(user)) {
            throw new RuntimeException("Cannot delete user linked to tasks");
        }
        this.repository.deleteById(id);
    }
}
