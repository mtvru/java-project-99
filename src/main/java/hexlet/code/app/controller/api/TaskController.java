package hexlet.code.app.controller.api;

import hexlet.code.app.dto.TaskCreateDTO;
import hexlet.code.app.dto.TaskDTO;
import hexlet.code.app.dto.TaskUpdateDTO;
import hexlet.code.app.service.TaskService;
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
@RequestMapping("/api/tasks")
@AllArgsConstructor
public class TaskController {
    private final TaskService service;

    /**
     * Get a list of all tasks.
     * @return list of tasks
     */
    @GetMapping
    public ResponseEntity<List<TaskDTO>> index() {
        List<TaskDTO> tasks = this.service.findAll();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(tasks.size()))
                .body(tasks);
    }

    /**
     * Get task details by id.
     * @param id task id
     * @return task data
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> show(@PathVariable Long id) {
        TaskDTO taskDTO = this.service.findById(id);
        return ResponseEntity.ok(taskDTO);
    }

    /**
     * Create a new task.
     * @param dto task creation data
     * @return created task data
     */
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
    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> update(@PathVariable Long id, @RequestBody TaskUpdateDTO dto) {
        TaskDTO taskDTO = this.service.update(id, dto);
        return ResponseEntity.ok(taskDTO);
    }

    /**
     * Delete task by id.
     * @param id task id
     * @return empty response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        this.service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
