package hexlet.code.controller.api;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.service.CRUDService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@AllArgsConstructor
@Tag(name = "Users", description = "Operations with users")
public class UserController {
    private final CRUDService<UserDTO, UserCreateDTO, UserUpdateDTO> service;

    /**
     * Get all users.
     * @return list of users
     */
    @Operation(summary = "Get list of all users")
    @ApiResponse(responseCode = "200", description = "List of users",
            headers = {@io.swagger.v3.oas.annotations.headers.Header(
                    name = "X-Total-Count", description = "Total number of users")})
    @GetMapping
    public ResponseEntity<List<UserDTO>> index() {
        Page<UserDTO> usersPage = this.service.findAll(Pageable.unpaged());
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(usersPage.getTotalElements()))
                .body(usersPage.getContent());
    }

    /**
     * Create a user.
     * @param dto user data
     * @return created user
     */
    @Operation(summary = "Create a new user")
    @ApiResponse(responseCode = "201", description = "User created")
    @PostMapping
    public ResponseEntity<UserDTO> create(@RequestBody UserCreateDTO dto) {
        UserDTO userDTO = this.service.create(dto);
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
    @Operation(summary = "Get user by ID", responses = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> show(@PathVariable @Parameter(description = "User ID") Long id) {
        UserDTO userDTO = this.service.findById(id);
        return ResponseEntity.ok(userDTO);
    }

    /**
     * Update user.
     * @param id user id
     * @param dto update data
     * @return updated user
     */
    @Operation(summary = "Update user", responses = {
        @ApiResponse(responseCode = "200", description = "User updated"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@userUtils.isOwner(#id)")
    public ResponseEntity<UserDTO> update(@PathVariable @Parameter(description = "User ID") Long id,
                                          @RequestBody UserUpdateDTO dto) {
        UserDTO userDTO = this.service.update(id, dto);
        return ResponseEntity.ok(userDTO);
    }

    /**
     * Delete user.
     * @param id user id
     * @return no content
     */
    @Operation(summary = "Delete user")
    @ApiResponse(responseCode = "204", description = "User deleted")
    @DeleteMapping("/{id}")
    @PreAuthorize("@userUtils.isOwner(#id)")
    public ResponseEntity<Void> destroy(@PathVariable @Parameter(description = "User ID") Long id) {
        this.service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
