package hexlet.code.repository;

import hexlet.code.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE User u
        SET u.firstName = CASE WHEN :firstNameSet = true THEN :firstName ELSE u.firstName END,
            u.lastName = CASE WHEN :lastNameSet = true THEN :lastName ELSE u.lastName END,
            u.email = CASE WHEN :emailSet = true THEN :email ELSE u.email END,
            u.password = CASE WHEN :passwordSet = true THEN :password ELSE u.password END,
            u.updatedAt = CURRENT_TIMESTAMP
        WHERE u.id = :id
    """)
    int updateAtomic(
        @Param("id") Long id,
        @Param("firstName") String firstName, @Param("firstNameSet") boolean firstNameSet,
        @Param("lastName") String lastName, @Param("lastNameSet") boolean lastNameSet,
        @Param("email") String email, @Param("emailSet") boolean emailSet,
        @Param("password") String password, @Param("passwordSet") boolean passwordSet
    );
}
