package projet.M1.controller;

import projet.M1.controller.dao.CoursDAO;
import projet.M1.model.academique.Promotion;
import projet.M1.model.planning.Cours;
import projet.M1.model.planning.Salle;
import projet.M1.model.utilisateur_systeme.Gestionnaire_Planning;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class GestionnaireController {

    private final Gestionnaire_Planning gestionnaire;
    private final CoursDAO coursDAO;

    public GestionnaireController(Gestionnaire_Planning gestionnaire, CoursDAO coursDAO) {
        this.gestionnaire = gestionnaire;
        this.coursDAO = coursDAO;
    }

    // EDT d'une promotion sur une semaine
    public List<Cours> getEmploiDuTempsPromotion(Promotion promotion, LocalDate semaine) {
        LocalDate lundi = semaine.with(DayOfWeek.MONDAY);
        LocalDate vendredi = semaine.with(DayOfWeek.FRIDAY);
        return coursDAO.findByPromotionAndSemaine(promotion, lundi, vendredi);
    }

    // EDT d'une salle sur une semaine
    public List<Cours> getEmploiDuTempsSalle(Salle salle, LocalDate semaine) {
        LocalDate lundi = semaine.with(DayOfWeek.MONDAY);
        LocalDate vendredi = semaine.with(DayOfWeek.FRIDAY);
        return coursDAO.findBySalleAndSemaine(salle, lundi, vendredi);
    }
}