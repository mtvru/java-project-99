package hexlet.code.repository;

import hexlet.code.model.Label;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LabelRepository extends BaseRepository<Label, Long> {
    Optional<Label> findByName(String name);
}
