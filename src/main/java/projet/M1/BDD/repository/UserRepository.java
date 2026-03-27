package projet.M1.BDD.repository;

import projet.M1.BDD.entity.Role;
import projet.M1.BDD.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // Authentification — AuthController
    Optional<UserEntity> findByLoginAndMotDePasse(String login, String motDePasse);

    // Vérification login unique — Service_Inscription
    boolean existsByLogin(String login);

    // Récupérer tous les utilisateurs d'un rôle
    List<UserEntity> findByRole(Role role);
}