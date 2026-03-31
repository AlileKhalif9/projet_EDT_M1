package projet.M1.BDD.dao;

import jakarta.persistence.EntityManager;
import projet.M1.BDD.JPAUtil;
import projet.M1.BDD.entity.SalleEntity;

import java.util.List;
import java.util.Optional;

/**
 * DAO pour les salles.
 * Utilisé pour remplir les ComboBox de salles dans ModificationRequestController
 * et TimetableController (onglet "Salle").
 */
public class SalleDAO {

    /** Toutes les salles triées par nom. */
    public List<SalleEntity> findAll() {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT s FROM SalleEntity s ORDER BY s.nom", SalleEntity.class)
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
                            "SELECT s FROM SalleEntity s WHERE s.nom = :nom", SalleEntity.class)
                    .setParameter("nom", nom)
                    .getResultList();
            return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
        } finally {
            em.close();
        }
    }
}
