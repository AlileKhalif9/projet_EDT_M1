package projet.M1.BDD.dao;

import jakarta.persistence.EntityManager;
import projet.M1.BDD.JPAUtil;
import projet.M1.BDD.entity.Role;
import projet.M1.BDD.entity.UserEntity;

import java.util.List;
import java.util.Optional;

/**
 * DAO pour les utilisateurs.
 */
public class UserDAO {

    /**
     * Authentification : cherche un utilisateur par login + mot de passe.
     */
    public Optional<UserEntity> findByLoginAndMotDePasse(String login, String motDePasse) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            List<UserEntity> results = em.createQuery(
                            "SELECT u FROM UserEntity u WHERE u.login = :login AND u.motDePasse = :mdp",
                            UserEntity.class)
                    .setParameter("login", login)
                    .setParameter("mdp", motDePasse)
                    .getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    /** Récupère tous les utilisateurs ayant un rôle donné. */
    public List<UserEntity> findByRole(Role role) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT u FROM UserEntity u WHERE u.role = :role ORDER BY u.nom",
                            UserEntity.class)
                    .setParameter("role", role)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Charge un utilisateur par son id. */
    public Optional<UserEntity> findById(Long id) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return Optional.ofNullable(em.find(UserEntity.class, id));
        } finally {
            em.close();
        }
    }

    /** Vérifie si un login existe déjà en base. */
    public boolean loginExiste(String login) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(u) FROM UserEntity u WHERE u.login = :login", Long.class)
                    .setParameter("login", login)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    /** Persiste un nouvel utilisateur en base. */
    public void save(UserEntity user) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        em.getTransaction().begin();
        try {
            em.persist(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
