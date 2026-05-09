package hexlet.code.repository;

import hexlet.code.model.TaskStatus;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskStatusRepository extends BaseRepository<TaskStatus, Long> {
    Optional<TaskStatus> findBySlug(String slug);
}
