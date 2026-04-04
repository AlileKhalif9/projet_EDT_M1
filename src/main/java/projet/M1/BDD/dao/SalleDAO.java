package projet.M1.BDD.dao;

import jakarta.persistence.EntityManager;
import projet.M1.BDD.JPAUtil;
import projet.M1.BDD.entity.SalleEntity;

import java.util.List;
import java.util.Optional;

/**
 * DAO pour les salles.
 * Utilise JOIN FETCH pour charger liste_materiel en une seule requête,
 * évitant les LazyInitializationException après fermeture de l'EntityManager.
 */
public class SalleDAO {

    /**
     * Toutes les salles avec leur matériel chargé, triées par nom.
     * Le DISTINCT évite les doublons causés par le JOIN FETCH.
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
