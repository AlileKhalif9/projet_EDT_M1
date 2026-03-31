package projet.M1.BDD.dao;

import jakarta.persistence.EntityManager;
import projet.M1.BDD.JPAUtil;
import projet.M1.BDD.entity.GroupeEtudiantEntity;

import java.util.List;

/**
 * DAO pour les groupes d'étudiants.
 * Utilisé pour remplir la ComboBox "EDT classe" dans TimetableController.
 */
public class GroupeDAO {

    /** Tous les groupes triés par nom. */
    public List<GroupeEtudiantEntity> findAll() {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT g FROM GroupeEtudiantEntity g ORDER BY g.nom",
                            GroupeEtudiantEntity.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
