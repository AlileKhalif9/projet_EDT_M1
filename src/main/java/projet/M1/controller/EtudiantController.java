package projet.M1.controller;

import projet.M1.controller.dao.CoursDAO;
import projet.M1.model.academique.Promotion;
import projet.M1.model.planning.Cours;
import projet.M1.model.utilisateur_systeme.Etudiant;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class EtudiantController {

    private final Etudiant etudiant;
    private final CoursDAO coursDAO;

    public EtudiantController(Etudiant etudiant, CoursDAO coursDAO) {
        this.etudiant = etudiant;
        this.coursDAO = coursDAO;
    }

    // EDT de l'étudiant connecté sur une semaine
    public List<Cours> getEmploiDuTempsSemaine(LocalDate semaine) {
        LocalDate lundi = semaine.with(DayOfWeek.MONDAY);
        LocalDate vendredi = semaine.with(DayOfWeek.FRIDAY);
        return coursDAO.findByEtudiantAndSemaine(etudiant, lundi, vendredi);
    }

    // EDT de la semaine courante
    public List<Cours> getEmploiDuTempsSemaineActuelle() {
        return getEmploiDuTempsSemaine(LocalDate.now());
    }

    // Consultation de l'EDT d'une promotion
    public List<Cours> getEmploiDuTempsPromotion(Promotion promotion, LocalDate semaine) {
        LocalDate lundi = semaine.with(DayOfWeek.MONDAY);
        LocalDate vendredi = semaine.with(DayOfWeek.FRIDAY);
        return coursDAO.findByPromotionAndSemaine(promotion, lundi, vendredi);
    }
}