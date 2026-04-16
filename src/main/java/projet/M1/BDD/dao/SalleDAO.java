package projet.M1.BDD.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import projet.M1.BDD.JPAUtil;
import projet.M1.BDD.entity.SalleEntity;

import java.util.List;
import java.util.Optional;

import java.util.List;
import java.util.Optional;

/**
 * DAO pour les salles.
 */
public class SalleDAO {

    /**
     * Toutes les salles avec leur matériel chargé, triées par nom.
     */
    public List<SalleEntity> findAll() {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT DISTINCT s FROM SalleEntity s " +
                                    "LEFT JOIN FETCH s.liste_materiel " +
                                    "ORDER BY s.nom",
                            SalleEntity.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Met à jour la liste des équipements d'une salle en BDD.
     */
    public void modifierEquipements(Long salleId, List<String> equipements) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            SalleEntity salle = em.find(SalleEntity.class, salleId);
            if (salle != null) salle.setListe_materiel(equipements);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /** Cherche une salle par son nom exact. */
    public Optional<SalleEntity> findByNom(String nom) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            List<SalleEntity> r = em.createQuery(
                            "SELECT s FROM SalleEntity s " +
                                    "LEFT JOIN FETCH s.liste_materiel " +
                                    "WHERE s.nom = :nom",
                            SalleEntity.class)
                    .setParameter("nom", nom)
                    .getResultList();
            return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
        } finally {
            em.close();
        }
    }
}