package projet.M1.BDD.dao;

import jakarta.persistence.EntityManager;
import projet.M1.BDD.JPAUtil;
import projet.M1.BDD.entity.ModuleEntity;
import projet.M1.BDD.entity.PromotionEntity;
import projet.M1.BDD.entity.UserEntity;

import java.util.List;

/**
 * DAO pour les modules.
 */
public class ModuleDAO {

    /** Modules enseignés par un professeur. */
    public List<ModuleEntity> findByProfesseur(Long profId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT m FROM ModuleEntity m JOIN m.list_professeur p " +
                                    "WHERE p.id = :profId ORDER BY m.nom",
                            ModuleEntity.class)
                    .setParameter("profId", profId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Modules suivis par un étudiant. */
    public List<ModuleEntity> findByEtudiant(Long etudiantId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT m FROM ModuleEntity m JOIN m.list_etudiant e " +
                                    "WHERE e.id = :etudiantId ORDER BY m.nom",
                            ModuleEntity.class)
                    .setParameter("etudiantId", etudiantId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Étudiants inscrits à un module. */
    public List<UserEntity> findEtudiantsByModule(Long moduleId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT e FROM ModuleEntity m JOIN m.list_etudiant e " +
                                    "WHERE m.id = :moduleId ORDER BY e.nom, e.prenom",
                            UserEntity.class)
                    .setParameter("moduleId", moduleId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Promotions auxquelles un professeur est rattaché .
     */
    public List<PromotionEntity> findPromotionsByProfesseur(Long profId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT DISTINCT p FROM PromotionEntity p " +
                                    "JOIN FETCH p.list_etudiant " +
                                    "JOIN p.list_professeur prof " +
                                    "WHERE prof.id = :profId ORDER BY p.nom",
                            PromotionEntity.class)
                    .setParameter("profId", profId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Modules enseignés par un professeur pour une promotion donnée.
     */
    public List<ModuleEntity> findByProfesseurAndPromotion(Long profId, Long promotionId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT m FROM ModuleEntity m " +
                                    "JOIN m.list_professeur p " +
                                    "JOIN m.list_promotion pr " +
                                    "WHERE p.id = :profId AND pr.id = :promotionId " +
                                    "ORDER BY m.nom",
                            ModuleEntity.class)
                    .setParameter("profId",      profId)
                    .setParameter("promotionId", promotionId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Étudiants d'une promotion inscrits à un module donné.
     */
    public List<UserEntity> findEtudiantsByModuleAndPromotion(Long moduleId, Long promotionId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT e FROM UserEntity e " +
                                    "JOIN e.list_module m " +
                                    "WHERE m.id = :moduleId AND e.promotion.id = :promotionId " +
                                    "ORDER BY e.nom, e.prenom",
                            UserEntity.class)
                    .setParameter("moduleId",    moduleId)
                    .setParameter("promotionId", promotionId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Vérifie si un professeur enseigne un module donné . */
    public boolean profEnseigneModule(Long profId, String nomModule) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(m) FROM ModuleEntity m JOIN m.list_professeur p " +
                                    "WHERE p.id = :profId AND m.nom = :nom",
                            Long.class)
                    .setParameter("profId", profId)
                    .setParameter("nom",    nomModule)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    /** Retourne tous les professeurs rattachés à un module . */
    public List<UserEntity> getProfsForModule(String nomModule) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT p FROM ModuleEntity m JOIN m.list_professeur p " +
                                    "WHERE m.nom = :nom ORDER BY p.nom",
                            UserEntity.class)
                    .setParameter("nom", nomModule)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}