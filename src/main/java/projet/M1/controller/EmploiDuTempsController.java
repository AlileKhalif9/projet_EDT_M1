package projet.M1.controller;

import projet.M1.controller.dao.CoursDAO;
import projet.M1.model.academique.Promotion;
import projet.M1.model.planning.Cours;
import projet.M1.model.planning.Salle;
import projet.M1.model.utilisateur_systeme.Etudiant;
import projet.M1.model.utilisateur_systeme.Professeur;
import projet.M1.session.SessionManager;
import projet.M1.model.utilisateur_systeme.Utilisateur;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class EmploiDuTempsController {

    private final CoursDAO coursDAO;

    public EmploiDuTempsController(CoursDAO coursDAO) {
        this.coursDAO = coursDAO;
    }

    // EDT de l'utilisateur connecté selon son rôle
    public List<Cours> getEmploiDuTempsConnecte(LocalDate semaine) {
        Utilisateur u = SessionManager.getInstance().getUtilisateurConnecte();
        LocalDate lundi = semaine.with(DayOfWeek.MONDAY);
        LocalDate vendredi = semaine.with(DayOfWeek.FRIDAY);

        // Utilisation de la classe sealed
        return switch (u) {
            case Etudiant e -> coursDAO.findByEtudiantAndSemaine(e, lundi, vendredi);
            case Professeur p  -> coursDAO.findByProfesseurAndSemaine(p, lundi, vendredi);
            default -> List.of(); // Gestionnaire et Invite n'ont pas d'EDT personnel
        };
    }

    // EDT d'une promotion pour tous les rôles
    public List<Cours> getEmploiDuTempsPromotion(Promotion promotion, LocalDate semaine) {
        LocalDate lundi    = semaine.with(DayOfWeek.MONDAY);
        LocalDate vendredi = semaine.with(DayOfWeek.FRIDAY);
        return coursDAO.findByPromotionAndSemaine(promotion, lundi, vendredi);
    }

    // EDT d'une salle
    public List<Cours> getEmploiDuTempsSalle(Salle salle, LocalDate semaine) {
        LocalDate lundi = semaine.with(DayOfWeek.MONDAY);
        LocalDate vendredi = semaine.with(DayOfWeek.FRIDAY);
        return coursDAO.findBySalleAndSemaine(salle, lundi, vendredi);
    }
}