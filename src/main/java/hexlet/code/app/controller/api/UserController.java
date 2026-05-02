package hexlet.code.app.controller.api;

import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.dto.UserDTO;
import hexlet.code.app.dto.UserIndexDTO;
import hexlet.code.app.dto.UserUpdateDTO;
import hexlet.code.app.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.net.URI;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get all users.
     * @param dto filter and pagination data
     * @return list of users
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> index(UserIndexDTO dto) {
        Page<UserDTO> usersPage = this.userService.findAll(dto);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(usersPage.getTotalElements()))
                .body(usersPage.getContent());
    }

    /**
     * Create user.
     * @param dto user data
     * @return created user
     */
    @PostMapping
    public ResponseEntity<UserDTO> create(@RequestBody UserCreateDTO dto) {
        UserDTO userDTO = this.userService.create(dto);
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(userDTO.getId())
            .toUri();
        return ResponseEntity.created(location)
            .body(userDTO);
    }

    /**
     * Get user by id.
     * @param id user id
     * @return user
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> show(@PathVariable Long id) {
        UserDTO userDTO = this.userService.findById(id);
        return ResponseEntity.ok(userDTO);
    }

    /**
     * Update user.
     * @param id user id
     * @param dto update data
     * @return updated user
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> update(@PathVariable Long id, @RequestBody UserUpdateDTO dto) {
        UserDTO userDTO = this.userService.update(id, dto);
        return ResponseEntity.ok(userDTO);
    }

    /**
     * Delete user.
     * @param id user id
     * @return no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        this.userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
