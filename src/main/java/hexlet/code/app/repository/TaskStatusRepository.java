package hexlet.code.app.repository;

import hexlet.code.app.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskStatusRepository extends JpaRepository<TaskStatus, Long> {
    Optional<TaskStatus> findBySlug(String slug);

    @Query("SELECT COUNT(t) > 0 FROM Task t WHERE t.taskStatus = :status")
    boolean existsByTasks(@Param("status") TaskStatus status);
}
