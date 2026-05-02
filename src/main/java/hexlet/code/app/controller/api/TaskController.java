package hexlet.code.app.controller.api;

import hexlet.code.app.dto.IndexDTO;
import hexlet.code.app.dto.TaskCreateDTO;
import hexlet.code.app.dto.TaskDTO;
import hexlet.code.app.dto.TaskUpdateDTO;
import hexlet.code.app.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@AllArgsConstructor
@Tag(name = "Tasks", description = "Operations with tasks")
public class TaskController {
    private final TaskService service;

    /**
     * Get a list of all tasks.
     * @param dto index data
     * @return list of tasks
     */
    @Operation(summary = "Get list of all tasks")
    @ApiResponse(responseCode = "200", description = "List of tasks",
            headers = {@io.swagger.v3.oas.annotations.headers.Header(
                    name = "X-Total-Count", description = "Total number of tasks")})
    @GetMapping
    public ResponseEntity<List<TaskDTO>> index(IndexDTO dto) {
        Page<TaskDTO> tasksPage = this.service.findAll(dto);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(tasksPage.getTotalElements()))
                .body(tasksPage.getContent());
    }

    /**
     * Get task details by id.
     * @param id task id
     * @return task data
     */
    @Operation(summary = "Get task by ID", responses = {
        @ApiResponse(responseCode = "200", description = "Task found"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> show(@PathVariable @Parameter(description = "Task ID") Long id) {
        TaskDTO taskDTO = this.service.findById(id);
        return ResponseEntity.ok(taskDTO);
    }

    /**
     * Create a new task.
     * @param dto task creation data
     * @return created task data
     */
    @Operation(summary = "Create a new task")
    @ApiResponse(responseCode = "201", description = "Task created")
    @PostMapping
    public ResponseEntity<TaskDTO> create(@RequestBody TaskCreateDTO dto) {
        TaskDTO taskDTO = this.service.create(dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(taskDTO.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(taskDTO);
    }

    /**
     * Update an existing task.
     * @param id task id
     * @param dto task update data
     * @return updated task data
     */
    @Operation(summary = "Update an existing task", responses = {
        @ApiResponse(responseCode = "200", description = "Task updated"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> update(@PathVariable @Parameter(description = "Task ID") Long id,
                                          @RequestBody TaskUpdateDTO dto) {
        TaskDTO taskDTO = this.service.update(id, dto);
        return ResponseEntity.ok(taskDTO);
    }

    /**
     * Delete task by id.
     * @param id task id
     * @return empty response
     */
    @Operation(summary = "Delete task by ID")
    @ApiResponse(responseCode = "204", description = "Task deleted")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Parameter(description = "Task ID") Long id) {
        this.service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
