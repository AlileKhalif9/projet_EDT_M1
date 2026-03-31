package projet.M1.BDD.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import projet.M1.BDD.JPAUtil;
import projet.M1.BDD.entity.HoraireEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DAO pour les horaires.
 * Utilisé pour remplir la ComboBox "Nouveau créneau" dans ModificationRequestController.
 */
public class HoraireDAO {

    /** Tous les horaires triés par date puis heure de début. */
    public List<HoraireEntity> findAll() {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT h FROM HoraireEntity h ORDER BY h.jour, h.heureDebut",
                            HoraireEntity.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Cherche un horaire existant par date + heures.
     * Si aucun ne correspond, en crée un nouveau et le persiste.
     * Utilisé lors de la soumission d'une demande de modification.
     */
    public HoraireEntity findOrCreate(LocalDate jour, LocalTime heureDebut, LocalTime heureFin) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            List<HoraireEntity> existing = em.createQuery(
                            "SELECT h FROM HoraireEntity h WHERE h.jour = :jour " +
                            "AND h.heureDebut = :debut AND h.heureFin = :fin",
                            HoraireEntity.class)
                    .setParameter("jour",  jour)
                    .setParameter("debut", heureDebut)
                    .setParameter("fin",   heureFin)
                    .getResultList();

            if (!existing.isEmpty()) return existing.get(0);

            // Crée un nouvel horaire s'il n'existe pas encore
            HoraireEntity h = new HoraireEntity();
            h.setJour(jour);
            h.setHeureDebut(heureDebut);
            h.setHeureFin(heureFin);
            tx.begin();
            em.persist(h);
            tx.commit();
            return h;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /** Horaires disponibles à une date donnée (utile pour la sélection de nouveau créneau). */
    public List<HoraireEntity> findByJour(LocalDate jour) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT h FROM HoraireEntity h WHERE h.jour = :jour ORDER BY h.heureDebut",
                            HoraireEntity.class)
                    .setParameter("jour", jour)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
