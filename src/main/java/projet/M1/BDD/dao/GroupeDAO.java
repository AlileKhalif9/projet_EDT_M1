package projet.M1.BDD.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import projet.M1.BDD.JPAUtil;
import projet.M1.BDD.entity.GroupeEtudiantEntity;
import projet.M1.BDD.entity.UserEntity;

import java.util.List;

/**
 * DAO pour les groupes d'étudiants.
 */
public class GroupeDAO {

    /**
     * Tous les groupes avec leurs étudiants chargés, triés par nom.
     */
    public List<GroupeEtudiantEntity> findAll() {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT DISTINCT g FROM GroupeEtudiantEntity g " +
                                    "LEFT JOIN FETCH g.list_etudiant " +
                                    "ORDER BY g.nom",
                            GroupeEtudiantEntity.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Crée et persiste un nouveau groupe vide. */
    public GroupeEtudiantEntity save(String nom) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            GroupeEtudiantEntity g = new GroupeEtudiantEntity();
            g.setNom(nom);
            em.persist(g);
            tx.commit();
            return g;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /** Renomme un groupe existant. */
    public void updateNom(Long groupeId, String nom) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            GroupeEtudiantEntity g = em.find(GroupeEtudiantEntity.class, groupeId);
            if (g != null) g.setNom(nom);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     *  Affecte un étudiant à un groupe
     */
    public void addMembre(Long groupeId, Long etudiantId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            UserEntity u = em.find(UserEntity.class, etudiantId);
            GroupeEtudiantEntity g = em.find(GroupeEtudiantEntity.class, groupeId);
            if (u != null && g != null) u.setGroupe(g);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /** Retire un étudiant de son groupe. */
    public void removeMembre(Long etudiantId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            UserEntity u = em.find(UserEntity.class, etudiantId);
            if (u != null) u.setGroupe(null);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
