package projet.M1.BDD.dao;

import jakarta.persistence.EntityManager;
import projet.M1.BDD.JPAUtil;
import projet.M1.BDD.entity.GroupeEtudiantEntity;

import java.util.List;

/**
 * DAO pour les groupes d'étudiants.
 * Utilise JOIN FETCH pour charger les étudiants de chaque groupe en une seule requête,
 * évitant les LazyInitializationException après fermeture de l'EntityManager.
 */
public class GroupeDAO {

    /**
     * Tous les groupes avec leurs étudiants chargés, triés par nom.
     * Le DISTINCT évite les doublons causés par le JOIN FETCH.
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
}
