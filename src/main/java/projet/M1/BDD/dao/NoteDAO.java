package projet.M1.BDD.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import projet.M1.BDD.JPAUtil;
import projet.M1.BDD.entity.ModuleEntity;
import projet.M1.BDD.entity.NoteEntity;
import projet.M1.BDD.entity.UserEntity;

import java.util.List;
import java.util.Optional;

/**
 * DAO pour les notes.
 * Utilisé par NoteController (back-end).
 */
public class NoteDAO {

    /**
     * Toutes les notes d'un module donné, avec étudiant chargé.
     * Utilisé par le professeur pour afficher le tableau de notes.
     */
    public List<NoteEntity> findByModule(Long moduleId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT n FROM NoteEntity n " +
                                    "JOIN FETCH n.etudiant " +
                                    "WHERE n.module.id = :moduleId " +
                                    "ORDER BY n.etudiant.nom, n.etudiant.prenom, n.intitule",
                            NoteEntity.class)
                    .setParameter("moduleId", moduleId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Toutes les notes d'un étudiant pour un module donné.
     * Utilisé par l'étudiant pour afficher ses notes.
     */
    public List<NoteEntity> findByEtudiantAndModule(Long etudiantId, Long moduleId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT n FROM NoteEntity n " +
                                    "WHERE n.etudiant.id = :etudiantId AND n.module.id = :moduleId " +
                                    "ORDER BY n.intitule",
                            NoteEntity.class)
                    .setParameter("etudiantId", etudiantId)
                    .setParameter("moduleId",   moduleId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Toutes les notes d'un étudiant, tous modules confondus.
     */
    public List<NoteEntity> findByEtudiant(Long etudiantId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT n FROM NoteEntity n " +
                                    "JOIN FETCH n.module " +
                                    "WHERE n.etudiant.id = :etudiantId " +
                                    "ORDER BY n.module.nom, n.intitule",
                            NoteEntity.class)
                    .setParameter("etudiantId", etudiantId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Cherche une note existante par étudiant, module et intitulé.
     * Permet de détecter si une note existe déjà avant d'en créer une nouvelle.
     */
    public Optional<NoteEntity> findByEtudiantModuleIntitule(Long etudiantId, Long moduleId, String intitule) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            List<NoteEntity> results = em.createQuery(
                            "SELECT n FROM NoteEntity n " +
                                    "WHERE n.etudiant.id = :etudiantId " +
                                    "AND n.module.id = :moduleId " +
                                    "AND n.intitule = :intitule",
                            NoteEntity.class)
                    .setParameter("etudiantId", etudiantId)
                    .setParameter("moduleId",   moduleId)
                    .setParameter("intitule",   intitule)
                    .getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    /**
     * Crée ou met à jour une note (upsert).
     * Si une note existe déjà pour cet étudiant/module/intitulé, on la met à jour.
     * Sinon on en crée une nouvelle.
     */
    public NoteEntity sauvegarderNote(Long etudiantId, Long moduleId,
                                      String intitule, float valeur, float coefficient) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // Chercher une note existante
            List<NoteEntity> existing = em.createQuery(
                            "SELECT n FROM NoteEntity n " +
                                    "WHERE n.etudiant.id = :etudiantId " +
                                    "AND n.module.id = :moduleId " +
                                    "AND n.intitule = :intitule",
                            NoteEntity.class)
                    .setParameter("etudiantId", etudiantId)
                    .setParameter("moduleId",   moduleId)
                    .setParameter("intitule",   intitule)
                    .getResultList();

            NoteEntity note;
            if (!existing.isEmpty()) {
                // Mise à jour
                note = existing.get(0);
                note.setValeur(valeur);
                note.setCoefficient(coefficient);
            } else {
                // Création
                note = new NoteEntity();
                note.setIntitule(intitule);
                note.setValeur(valeur);
                note.setCoefficient(coefficient);
                note.setModule(em.find(ModuleEntity.class, moduleId));
                note.setEtudiant(em.find(UserEntity.class, etudiantId));
                em.persist(note);
            }

            tx.commit();
            return note;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Supprime toutes les notes d'un intitulé donné pour un module.
     * Utilisé quand le prof supprime un contrôle entier.
     */
    public void supprimerControle(Long moduleId, String intitule) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery(
                            "DELETE FROM NoteEntity n " +
                                    "WHERE n.module.id = :moduleId AND n.intitule = :intitule")
                    .setParameter("moduleId", moduleId)
                    .setParameter("intitule", intitule)
                    .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}