package hexlet.code.repository;

import hexlet.code.model.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {
    Optional<Label> findByName(String name);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Label l
        SET l.name = CASE WHEN :nameSet = true THEN :name ELSE l.name END
        WHERE l.id = :id
    """)
    int updateAtomic(
        @Param("id") Long id,
        @Param("name") String name, @Param("nameSet") boolean nameSet
    );
}
