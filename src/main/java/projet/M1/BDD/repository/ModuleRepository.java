package projet.M1.BDD.repository;

import projet.M1.BDD.entity.ModuleEntity;
import projet.M1.BDD.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModuleRepository extends JpaRepository<ModuleEntity, Long> {

    // Recherche par nom
    Optional<ModuleEntity> findByNom(String nom);

    // Modules d'un étudiant
    List<ModuleEntity> findByList_etudiantContaining(UserEntity etudiant);

    // Modules d'un professeur
    List<ModuleEntity> findByList_professeurContaining(UserEntity professeur);
}