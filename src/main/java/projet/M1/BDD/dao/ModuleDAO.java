package projet.M1.BDD.dao;

import jakarta.persistence.EntityManager;
import projet.M1.BDD.JPAUtil;
import projet.M1.BDD.entity.ModuleEntity;
import projet.M1.BDD.entity.UserEntity;

import java.util.List;

/**
 * DAO pour les modules.
 */
public class ModuleDAO {

    /** Modules enseignés par un professeur (via table professeur_module). */
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

    /** Modules suivis par un étudiant (via table etudiant_module). */
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

    /** Vérifie si un professeur enseigne un module donné (par nom exact). */
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

    /** Retourne tous les professeurs rattachés à un module (par nom exact). */
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