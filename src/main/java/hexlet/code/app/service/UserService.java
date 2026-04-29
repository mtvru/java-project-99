package hexlet.code.app.service;

import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.dto.UserDTO;
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
    private final UserRepository repository;
    private final UserMapper mapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.repository = userRepository;
        this.mapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return this.repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public UserDTO create(@Valid UserCreateDTO dto) {
        User user = this.mapper.map(dto);
        user = this.repository.save(user);
        return this.mapper.map(user);
    }

    public Page<UserDTO> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = this.repository.findAll(pageable);
        return users.map(this.mapper::map);
    }

    public UserDTO findById(Long id) {
        return this.repository.findById(id)
                .map(this.mapper::map)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
    }

    public UserDTO update(Long id, @Valid UserUpdateDTO dto) {
        User user = this.repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        this.mapper.update(dto, user);
        this.repository.save(user);
        return this.mapper.map(user);
    }

    public boolean delete(Long id) {
        if (!this.repository.existsById(id)) {
            return false;
        }
        this.repository.deleteById(id);
        return true;
    }
}
