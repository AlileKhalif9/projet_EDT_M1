package projet.M1.controller;

import projet.M1.BDD.dao.CoursDAO;
import projet.M1.BDD.entity.CoursEntity;
import projet.M1.BDD.entity.SalleEntity;
import projet.M1.BDD.entity.UserEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Back-end : emploi du temps.
 * Le front (TimetableController, DashboardController) appelle ces méthodes —
 * jamais CoursDAO directement.
 *
 * Rôles et leur EDT :
 *   ETUDIANT              → ses cours uniquement (findByEtudiantAndSemaine)
 *   PROFESSEUR            → ses cours uniquement (findByProfesseurAndSemaine)
 *   INVITE                → ses cours uniquement, comme un professeur (findByProfesseurAndSemaine)
 *   GESTIONNAIRE_PLANNING → tous les cours de la semaine (findAllBySemaine)
 */
public class EmploiDuTempsController {

    private final CoursDAO coursDAO;

    public EmploiDuTempsController(CoursDAO coursDAO) {
        this.coursDAO = coursDAO;
    }

    public List<CoursEntity> getEmploiDuTempsConnecte(UserEntity u, LocalDate semaine) {
        return switch (u.getRole()) {
            case ETUDIANT              -> coursDAO.findByEtudiantAndSemaine(u, semaine);
            case PROFESSEUR, INVITE    -> coursDAO.findByProfesseurAndSemaine(u, semaine);
            case GESTIONNAIRE_PLANNING -> coursDAO.findAllBySemaine(semaine);
        };
    }

    public List<CoursEntity> getEmploiDuTempsGroupe(String nomGroupe, LocalDate semaine) {
        return coursDAO.findByGroupeAndSemaine(nomGroupe, semaine);
    }

    public List<CoursEntity> getEmploiDuTempsSalle(SalleEntity salle, LocalDate semaine) {
        return coursDAO.findBySalleAndSemaine(salle, semaine);
    }

    /**
     * US13 — Annule un cours (rôle PROFESSEUR).
     * Retourne le typeCours d'origine pour permettre la réactivation.
     */
    public String annulerCours(Long coursId) {
        if (coursId == null) throw new IllegalArgumentException("Ce cours n'a pas d'identifiant BDD.");
        return coursDAO.annulerCours(coursId);
    }

    /**
     * Réactive un cours annulé en restaurant son typeCours d'origine.
     */
    public void reactiverCours(Long coursId, String typeCoursOrigine) {
        if (coursId == null) throw new IllegalArgumentException("Ce cours n'a pas d'identifiant BDD.");
        coursDAO.reactiverCours(coursId, typeCoursOrigine);
    }

    /**
     * US14 — Crée un nouveau cours en BDD (rôle GESTIONNAIRE).
     */
    public CoursEntity ajouterCours(String nom, String typeCours,
                                    LocalDate jour, LocalTime heureDebut, LocalTime heureFin,
                                    String nomSalle, String nomGroupe) {
        return coursDAO.ajouterCours(nom, typeCours, jour, heureDebut, heureFin, nomSalle, nomGroupe);
    }

    /**
     * US15 — Modifie un cours existant en BDD (rôle GESTIONNAIRE).
     */
    public CoursEntity modifierCours(Long coursId, String nom, String typeCours,
                                     LocalDate jour, LocalTime heureDebut, LocalTime heureFin,
                                     String nomSalle, String nomGroupe) {
        if (coursId == null) throw new IllegalArgumentException("Ce cours n'a pas d'identifiant BDD.");
        return coursDAO.modifierCours(coursId, nom, typeCours, jour, heureDebut, heureFin, nomSalle, nomGroupe);
    }
}
