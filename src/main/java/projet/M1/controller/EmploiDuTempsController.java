package projet.M1.controller;

import projet.M1.BDD.dao.CoursDAO;
import projet.M1.BDD.dao.UserDAO;
import projet.M1.BDD.entity.CoursEntity;
import projet.M1.BDD.entity.Role;
import projet.M1.BDD.entity.SalleEntity;
import projet.M1.BDD.entity.UserEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Back-end : emploi du temps.
 */
public class EmploiDuTempsController {

    private final CoursDAO coursDAO;

    public EmploiDuTempsController(CoursDAO coursDAO) {
        this.coursDAO = coursDAO;
    }

    public List<CoursEntity> getEmploiDuTempsConnecte(UserEntity u, LocalDate semaine) {
        return switch (u.getRole()) {
            case ETUDIANT -> coursDAO.findByEtudiantAndSemaine(u, semaine);
            case PROFESSEUR, INVITE -> coursDAO.findByProfesseurAndSemaine(u, semaine);
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
     * Annule un cours (rôle PROFESSEUR).
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
     * Crée un nouveau cours en BDD (rôle GESTIONNAIRE).
     */
    public CoursEntity ajouterCours(String nom, String typeCours,
                                    LocalDate jour, LocalTime heureDebut, LocalTime heureFin,
                                    String nomSalle, String nomGroupe, Long profId) {
        return coursDAO.ajouterCours(nom, typeCours, jour, heureDebut, heureFin, nomSalle, nomGroupe, profId);
    }

    /**
     * Modifie un cours existant en BDD (rôle GESTIONNAIRE).
     */
    public CoursEntity modifierCours(Long coursId, String nom, String typeCours,
                                     LocalDate jour, LocalTime heureDebut, LocalTime heureFin,
                                     String nomSalle, String nomGroupe) {
        if (coursId == null) throw new IllegalArgumentException("Ce cours n'a pas d'identifiant BDD.");
        return coursDAO.modifierCours(coursId, nom, typeCours, jour, heureDebut, heureFin, nomSalle, nomGroupe);
    }

    /** Retourne tous les professeurs (pour le sélecteur de tiers dans TimetableUI). */
    public List<UserEntity> getProfesseurs() {
        return new UserDAO().findByRole(Role.PROFESSEUR);
    }
}