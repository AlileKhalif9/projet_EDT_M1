package projet.M1.BDD.service;

import projet.M1.BDD.entity.CoursEntity;
import projet.M1.BDD.entity.PromotionEntity;
import projet.M1.BDD.entity.SalleEntity;
import projet.M1.BDD.repository.CoursRepository;
import projet.M1.BDD.repository.PromotionRepository;
import projet.M1.BDD.repository.SalleRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class ScheduleTiersService {

    private final CoursRepository coursRepository;
    private final PromotionRepository promotionRepository;
    private final SalleRepository salleRepository;

    public ScheduleTiersService(CoursRepository coursRepository,
                                PromotionRepository promotionRepository,
                                SalleRepository salleRepository) {
        this.coursRepository  = coursRepository;
        this.promotionRepository = promotionRepository;
        this.salleRepository = salleRepository;
    }

    // Retourne les cours de la semaine d'une promotion
    public List<CoursEntity> getEmploiDuTempsPromotion(String nomPromotion, LocalDate semaine) {
        LocalDate lundi = semaine.with(DayOfWeek.MONDAY);
        LocalDate vendredi = semaine.with(DayOfWeek.FRIDAY);

        PromotionEntity promotion = promotionRepository.findByNom(nomPromotion)
                .orElseThrow(() -> new IllegalArgumentException("Promotion introuvable : " + nomPromotion));

        return coursRepository.findByPromotionAndSemaine(promotion, lundi, vendredi);
    }

    // Retourne les cours de la semaine d'une salle
    public List<CoursEntity> getEmploiDuTempsSalle(String nomSalle, LocalDate semaine) {
        LocalDate lundi    = semaine.with(DayOfWeek.MONDAY);
        LocalDate vendredi = semaine.with(DayOfWeek.FRIDAY);

        SalleEntity salle = salleRepository.findByNom(nomSalle)
                .orElseThrow(() -> new IllegalArgumentException("Salle introuvable : " + nomSalle));

        return coursRepository.findBySalleAndSemaine(salle, lundi, vendredi);
    }

    // Retourne les cours d'une salle pour un jour précis
    public List<CoursEntity> getDisponibiliteSalle(String nomSalle, LocalDate jour) {
        SalleEntity salle = salleRepository.findByNom(nomSalle)
                .orElseThrow(() -> new IllegalArgumentException("Salle introuvable : " + nomSalle));

        return coursRepository.findBySalleAndJour(salle, jour);
    }
}