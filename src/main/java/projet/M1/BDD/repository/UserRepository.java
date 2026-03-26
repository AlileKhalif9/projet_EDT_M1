package projet.M1.BDD.repository;

import com.timetable.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository pour gérer les utilisateurs (User).
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Recherche un utilisateur par email.
     * @param email email de l'utilisateur
     * @return Optional<User>
     */
    Optional<User> findByEmail(String email);
}