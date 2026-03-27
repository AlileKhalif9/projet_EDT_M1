package projet.M1.BDD.repository;

import projet.M1.BDD.entity.PromotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PromotionRepository extends JpaRepository<PromotionEntity, Long> {

    // Recherche par nom
    Optional<PromotionEntity> findByNom(String nom);
}