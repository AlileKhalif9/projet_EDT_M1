package projet.M1.BDD.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import projet.M1.BDD.JPAUtil;
import projet.M1.BDD.entity.CoursEntity;
import projet.M1.BDD.entity.HoraireEntity;
import projet.M1.BDD.entity.SalleEntity;
import projet.M1.BDD.entity.UserEntity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * DAO pour les cours.
 * Remplace MockDataService pour la récupération des emplois du temps.
 *
 * Toutes les méthodes créent leur propre EntityManager et le ferment après usage
 * pour éviter les fuites de connexions (pattern "EntityManager par opération").
 */
public class CoursDAO {

    /**
     * Cours d'un étudiant pour la semaine contenant la date donnée.
     * Utilisé dans TimetableController (onglet "Mon EDT") quand l'utilisateur est ETUDIANT.
     */
    public List<CoursEntity> findByEtudiantAndSemaine(UserEntity etudiant, LocalDate semaine) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            LocalDate lundi    = semaine.with(DayOfWeek.MONDAY);
            LocalDate vendredi = semaine.with(DayOfWeek.FRIDAY);
            return em.createQuery(
                            "SELECT c FROM CoursEntity c JOIN c.list_etudiant e " +
                            "WHERE e = :etudiant AND c.horaire.jour BETWEEN :lundi AND :vendredi " +
                            "ORDER BY c.horaire.jour, c.horaire.heureDebut",
                            CoursEntity.class)
                    .setParameter("etudiant", em.merge(etudiant))
                    .setParameter("lundi",    lundi)
                    .setParameter("vendredi", vendredi)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Cours d'un professeur pour la semaine contenant la date donnée.
     * Utilisé dans TimetableController (onglet "Mon EDT") quand l'utilisateur est PROFESSEUR.
     */
    public List<CoursEntity> findByProfesseurAndSemaine(UserEntity professeur, LocalDate semaine) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            LocalDate lundi    = semaine.with(DayOfWeek.MONDAY);
            LocalDate vendredi = semaine.with(DayOfWeek.FRIDAY);
            return em.createQuery(
                            "SELECT c FROM CoursEntity c JOIN c.list_professeur p " +
                            "WHERE p = :professeur AND c.horaire.jour BETWEEN :lundi AND :vendredi " +
                            "ORDER BY c.horaire.jour, c.horaire.heureDebut",
                            CoursEntity.class)
                    .setParameter("professeur", em.merge(professeur))
                    .setParameter("lundi",      lundi)
                    .setParameter("vendredi",   vendredi)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * EDT d'un groupe d'étudiants pour une semaine.
     * Utilisé dans TimetableController (onglet "EDT classe" / "Tiers").
     */
    public List<CoursEntity> findByGroupeAndSemaine(String nomGroupe, LocalDate semaine) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            LocalDate lundi    = semaine.with(DayOfWeek.MONDAY);
            LocalDate vendredi = semaine.with(DayOfWeek.FRIDAY);
            return em.createQuery(
                            "SELECT c FROM CoursEntity c JOIN c.list_etudiant e " +
                            "WHERE e.groupe.nom = :groupe AND c.horaire.jour BETWEEN :lundi AND :vendredi " +
                            "ORDER BY c.horaire.jour, c.horaire.heureDebut",
                            CoursEntity.class)
                    .setParameter("groupe",  nomGroupe)
                    .setParameter("lundi",   lundi)
                    .setParameter("vendredi",vendredi)
                    .getResultList().stream().distinct().toList();
        } finally {
            em.close();
        }
    }

    /**
     * Applique une demande approuvée : déplace le cours vers le nouvel horaire
     * et/ou la nouvelle salle. Appelé par ModificationRequestController.onApprouver().
     */
    public void applyModification(Long coursId, HoraireEntity newHoraire, SalleEntity newSalle) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CoursEntity cours = em.find(CoursEntity.class, coursId);
            if (cours != null) {
                if (newHoraire != null)
                    cours.setHoraire(em.find(HoraireEntity.class, newHoraire.getId()));
                if (newSalle != null)
                    cours.setSalle(em.find(SalleEntity.class, newSalle.getId()));
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * EDT d'une salle pour une semaine.
     * Utilisé dans TimetableController (onglet "Salle").
     */
    public List<CoursEntity> findBySalleAndSemaine(SalleEntity salle, LocalDate semaine) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            LocalDate lundi    = semaine.with(DayOfWeek.MONDAY);
            LocalDate vendredi = semaine.with(DayOfWeek.FRIDAY);
            return em.createQuery(
                            "SELECT c FROM CoursEntity c " +
                                    "WHERE c.salle.id = :salleId AND c.horaire.jour BETWEEN :lundi AND :vendredi " +
                                    "ORDER BY c.horaire.jour, c.horaire.heureDebut",
                            CoursEntity.class)
                    .setParameter("salleId",  salle.getId())
                    .setParameter("lundi",    lundi)
                    .setParameter("vendredi", vendredi)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
