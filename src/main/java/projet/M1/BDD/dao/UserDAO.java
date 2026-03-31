package projet.M1.BDD.dao;

import jakarta.persistence.EntityManager;
import projet.M1.BDD.JPAUtil;
import projet.M1.BDD.entity.Role;
import projet.M1.BDD.entity.UserEntity;

import java.util.List;
import java.util.Optional;

/**
 * DAO pour les utilisateurs.
 * Remplace MockUtilisateurDAO — toutes les requêtes vont désormais en base PostgreSQL.
 */
public class UserDAO {

    /**
     * Authentification : cherche un utilisateur par login + mot de passe.
     * Retourne Optional.empty() si les identifiants sont incorrects.
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

    /** Récupère tous les utilisateurs ayant un rôle donné (ex: PROFESSEUR). */
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

    /** Charge un utilisateur par son id (utile après login pour refresh les lazy collections). */
    public Optional<UserEntity> findById(Long id) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return Optional.ofNullable(em.find(UserEntity.class, id));
        } finally {
            em.close();
        }
    }
}
