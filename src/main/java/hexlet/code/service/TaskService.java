package hexlet.code.service;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskParamsDTO;
import hexlet.code.dto.TaskUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService extends CRUDService<TaskDTO, TaskCreateDTO,   TaskUpdateDTO> {
    /**
     * Get all tasks filtered by parameters.
     * @param params search parameters
     * @param pageable pagination and sorting information
     * @return page of tasks
     */
    Page<TaskDTO> findAll(TaskParamsDTO params, Pageable pageable);
}
