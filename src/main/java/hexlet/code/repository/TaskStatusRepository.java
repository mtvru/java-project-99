package hexlet.code.repository;

import hexlet.code.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface TaskStatusRepository extends JpaRepository<TaskStatus, Long> {
    Optional<TaskStatus> findBySlug(String slug);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE TaskStatus ts
        SET ts.name = CASE WHEN :nameSet = true THEN :name ELSE ts.name END,
            ts.slug = CASE WHEN :slugSet = true THEN :slug ELSE ts.slug END
        WHERE ts.id = :id
    """)
    int updateAtomic(
        @Param("id") Long id,
        @Param("name") String name, @Param("nameSet") boolean nameSet,
        @Param("slug") String slug, @Param("slugSet") boolean slugSet
    );
}
