package hexlet.code.app.controller.api;

import hexlet.code.app.dto.TaskStatusCreateDTO;
import hexlet.code.app.dto.TaskStatusDTO;
import hexlet.code.app.dto.TaskStatusUpdateDTO;
import hexlet.code.app.service.TaskStatusService;
import lombok.AllArgsConstructor;
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
public class TaskStatusController {
    private final TaskStatusService service;

    /**
     * Get a list of all task statuses.
     * @return response with a list of statuses
     */
    @GetMapping
    public ResponseEntity<List<TaskStatusDTO>> index() {
        List<TaskStatusDTO> statuses = this.service.findAll();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(statuses.size()))
                .body(statuses);
    }

    /**
     * Get task status by id.
     * @param id status id
     * @return task status
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskStatusDTO> show(@PathVariable Long id) {
        TaskStatusDTO taskStatusDTO = this.service.findById(id);
        return ResponseEntity.ok(taskStatusDTO);
    }

    /**
     * Create a new task status.
     * @param dto status data
     * @return created task status
     */
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
    @PutMapping("/{id}")
    public ResponseEntity<TaskStatusDTO> update(@PathVariable Long id, @RequestBody TaskStatusUpdateDTO dto) {
        TaskStatusDTO taskStatusDTO = this.service.update(id, dto);
        return ResponseEntity.ok(taskStatusDTO);
    }

    /**
     * Delete task status.
     * @param id status id
     * @return empty response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        this.service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
