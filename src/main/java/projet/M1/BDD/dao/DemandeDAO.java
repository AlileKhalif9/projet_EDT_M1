package projet.M1.BDD.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import projet.M1.BDD.JPAUtil;
import projet.M1.BDD.entity.CoursModificationRequestEntity;
import projet.M1.BDD.entity.StatutDemande;
import projet.M1.BDD.entity.UserEntity;

import java.util.List;
import java.util.Optional;

/**
 * DAO pour les demandes de modification de cours.
 */
public class DemandeDAO {

    /** Toutes les demandes soumises par un professeur, triées du plus récent au plus ancien. */
    public List<CoursModificationRequestEntity> findByDemandeur(UserEntity demandeur) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT d FROM CoursModificationRequestEntity d " +
                            "WHERE d.demandeur.id = :id ORDER BY d.id DESC",
                            CoursModificationRequestEntity.class)
                    .setParameter("id", demandeur.getId())
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Toutes les demandes ayant un statut donné */
    public List<CoursModificationRequestEntity> findByStatut(StatutDemande statut) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT d FROM CoursModificationRequestEntity d " +
                            "WHERE d.statut = :statut ORDER BY d.id DESC",
                            CoursModificationRequestEntity.class)
                    .setParameter("statut", statut)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Cherche une demande par son id. */
    public Optional<CoursModificationRequestEntity> findById(Long id) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return Optional.ofNullable(em.find(CoursModificationRequestEntity.class, id));
        } finally {
            em.close();
        }
    }

    /**
     * Persiste une nouvelle demande
     */
    public CoursModificationRequestEntity save(CoursModificationRequestEntity demande) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CoursModificationRequestEntity saved = em.merge(demande);
            tx.commit();
            return saved;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Met à jour le statut d'une demande
     */
    public void updateStatut(Long id, StatutDemande statut) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CoursModificationRequestEntity d = em.find(CoursModificationRequestEntity.class, id);
            if (d != null) d.setStatut(statut);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Supprime une demande
     */
    public void delete(Long id) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CoursModificationRequestEntity d = em.find(CoursModificationRequestEntity.class, id);
            if (d != null) em.remove(d);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
