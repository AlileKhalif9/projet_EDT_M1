package projet.M1.BDD.repository;

import projet.M1.BDD.entity.SalleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalleRepository extends JpaRepository<SalleEntity, Long> {

    // Recherche par nom
    Optional<SalleEntity> findByNom(String nom);

    // Salles avec une capacité minimum
    List<SalleEntity> findByPlaceGreaterThanEqual(int capaciteMin);
}