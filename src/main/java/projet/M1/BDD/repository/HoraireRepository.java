package projet.M1.BDD.repository;

import projet.M1.BDD.entity.HoraireEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface HoraireRepository extends JpaRepository<HoraireEntity, Long> {

    // Horaires d'un jour précis
    List<HoraireEntity> findByJour(LocalDate jour);

    // Horaires entre deux dates (pour filtrer une semaine)
    List<HoraireEntity> findByJourBetween(LocalDate debut, LocalDate fin);
}