package hexlet.code.controller.api;

import hexlet.code.dto.IndexDTO;
import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.service.TaskStatusService;
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
@RequestMapping("/api/task_statuses")
@AllArgsConstructor
@Tag(name = "Task Statuses", description = "Operations with task statuses")
public class TaskStatusController {
    private final TaskStatusService service;

    /**
     * Get a list of all task statuses.
     * @param dto index data
     * @return response with a list of statuses
     */
    @Operation(summary = "Get list of all task statuses")
    @ApiResponse(responseCode = "200", description = "List of task statuses",
            headers = {@io.swagger.v3.oas.annotations.headers.Header(
                    name = "X-Total-Count", description = "Total number of task statuses")})
    @GetMapping
    public ResponseEntity<List<TaskStatusDTO>> index(IndexDTO dto) {
        Page<TaskStatusDTO> statusesPage = this.service.findAll(dto);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(statusesPage.getTotalElements()))
                .body(statusesPage.getContent());
    }

    /**
     * Get task status by id.
     * @param id status id
     * @return task status
     */
    @Operation(summary = "Get task status by ID", responses = {
        @ApiResponse(responseCode = "200", description = "Task status found"),
        @ApiResponse(responseCode = "404", description = "Task status not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskStatusDTO> show(@PathVariable @Parameter(description = "Task Status ID") Long id) {
        TaskStatusDTO taskStatusDTO = this.service.findById(id);
        return ResponseEntity.ok(taskStatusDTO);
    }

    /**
     * Create a new task status.
     * @param dto status data
     * @return created task status
     */
    @Operation(summary = "Create a new task status")
    @ApiResponse(responseCode = "201", description = "Task status created")
    @PostMapping
    public ResponseEntity<TaskStatusDTO> create(@RequestBody TaskStatusCreateDTO dto) {
        TaskStatusDTO taskStatusDTO = this.service.create(dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(taskStatusDTO.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(taskStatusDTO);
    }

    /**
     * Update task status.
     * @param id status id
     * @param dto new status data
     * @return updated task status
     */
    @Operation(summary = "Update task status", responses = {
        @ApiResponse(responseCode = "200", description = "Task status updated"),
        @ApiResponse(responseCode = "404", description = "Task status not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TaskStatusDTO> update(@PathVariable @Parameter(description = "Task Status ID") Long id,
                                                @RequestBody TaskStatusUpdateDTO dto) {
        TaskStatusDTO taskStatusDTO = this.service.update(id, dto);
        return ResponseEntity.ok(taskStatusDTO);
    }

    /**
     * Delete task status.
     * @param id status id
     * @return empty response
     */
    @Operation(summary = "Delete task status")
    @ApiResponse(responseCode = "204", description = "Task status deleted")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> destroy(@PathVariable @Parameter(description = "Task Status ID") Long id) {
        this.service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
