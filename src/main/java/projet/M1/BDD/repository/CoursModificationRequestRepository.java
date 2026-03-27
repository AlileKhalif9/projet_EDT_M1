package projet.M1.BDD.repository;

import projet.M1.BDD.entity.StatutDemande;
import projet.M1.BDD.entity.UserEntity;
import projet.M1.BDD.entity.CoursModificationRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoursModificationRequestRepository
        extends JpaRepository<CoursModificationRequestEntity, Long> {

    // Demandes d'un professeur
    List<CoursModificationRequestEntity> findByDemandeur(UserEntity demandeur);

    // Demandes par statut, gestionnaire
    List<CoursModificationRequestEntity> findByStatut(StatutDemande statut);

    // Demandes d'un professeur par statut
    List<CoursModificationRequestEntity> findByDemandeurAndStatut(
            UserEntity demandeur, StatutDemande statut);
}
