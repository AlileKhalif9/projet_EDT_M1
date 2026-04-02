package projet.M1.controller;

import projet.M1.BDD.dao.CoursDAO;
import projet.M1.BDD.entity.CoursEntity;
import projet.M1.BDD.entity.SalleEntity;
import projet.M1.BDD.entity.UserEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * Back-end : emploi du temps.
 * Le front (TimetableController, DashboardController) appelle ces méthodes —
 * jamais CoursDAO directement.
 */
public class EmploiDuTempsController {

    private final CoursDAO coursDAO;

    public EmploiDuTempsController(CoursDAO coursDAO) {
        this.coursDAO = coursDAO;
    }

    /** Cours de l'utilisateur connecté pour la semaine, selon son rôle. */
    public List<CoursEntity> getEmploiDuTempsConnecte(UserEntity u, LocalDate semaine) {
        return switch (u.getRole()) {
            case ETUDIANT   -> coursDAO.findByEtudiantAndSemaine(u, semaine);
            case PROFESSEUR -> coursDAO.findByProfesseurAndSemaine(u, semaine);
            default         -> List.of();
        };
    }

    /** Cours d'un groupe pour la semaine (onglet EDT classe). */
    public List<CoursEntity> getEmploiDuTempsGroupe(String nomGroupe, LocalDate semaine) {
        return coursDAO.findByGroupeAndSemaine(nomGroupe, semaine);
    }

    /** Cours d'une salle pour la semaine (onglet Salle). */
    public List<CoursEntity> getEmploiDuTempsSalle(SalleEntity salle, LocalDate semaine) {
        return coursDAO.findBySalleAndSemaine(salle, semaine);
    }
}
