package hexlet.code.repository;

import hexlet.code.model.Task;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    @EntityGraph(attributePaths = {"labels", "status", "assignee"})
    Optional<Task> findByName(String name);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Task t
        SET t.name = CASE WHEN :nameSet = true THEN :name ELSE t.name END,
            t.index = CASE WHEN :indexSet = true THEN :index ELSE t.index END,
            t.description = CASE WHEN :descriptionSet = true THEN :description ELSE t.description END,
            t.status.id = CASE WHEN :statusSet = true THEN :statusId ELSE t.status.id END,
            t.assignee.id = CASE WHEN :assigneeSet = true THEN :assigneeId ELSE t.assignee.id END
        WHERE t.id = :id
    """)
    int updateAtomic(
        @Param("id") Long id,
        @Param("name") String name, @Param("nameSet") boolean nameSet,
        @Param("index") Integer index, @Param("indexSet") boolean indexSet,
        @Param("description") String description, @Param("descriptionSet") boolean descriptionSet,
        @Param("statusId") Long statusId, @Param("statusSet") boolean statusSet,
        @Param("assigneeId") Long assigneeId, @Param("assigneeSet") boolean assigneeSet
    );
}
