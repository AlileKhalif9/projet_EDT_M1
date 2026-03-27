package projet.M1.BDD.repository;

import projet.M1.BDD.entity.GroupeEtudiantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupeEtudiantRepository extends JpaRepository<GroupeEtudiantEntity, Long> {

    // Recherche par nom
    Optional<GroupeEtudiantEntity> findByNom(String nom);
}