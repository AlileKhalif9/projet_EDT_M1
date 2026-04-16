package projet.M1.controller;

import projet.M1.BDD.DataCache;
import projet.M1.BDD.dao.GroupeDAO;
import projet.M1.BDD.dao.UserDAO;
import projet.M1.BDD.entity.GroupeEtudiantEntity;
import projet.M1.BDD.entity.Role;
import projet.M1.BDD.entity.UserEntity;

import java.util.List;

/**
 * Back-end : gestion des groupes d'étudiants.
 */
public class GroupeController {

    private final GroupeDAO groupeDAO;

    public GroupeController(GroupeDAO groupeDAO) {
        this.groupeDAO = groupeDAO;
    }

    public List<GroupeEtudiantEntity> getAllGroupes() {
        List<GroupeEtudiantEntity> cached = DataCache.getInstance().getGroupes();
        if (!cached.isEmpty()) return cached;
        return groupeDAO.findAll();
    }

    /** Crée et persiste un nouveau groupe vide. */
    public GroupeEtudiantEntity creerGroupe(String nom) {
        if (nom == null || nom.isBlank())
            throw new IllegalArgumentException("Le nom du groupe ne peut pas être vide.");
        GroupeEtudiantEntity g = groupeDAO.save(nom);
        DataCache.getInstance().invalidateGroupes();
        return g;
    }

    /** Renomme un groupe existant en BDD. */
    public void renommerGroupe(Long groupeId, String nom) {
        if (nom == null || nom.isBlank())
            throw new IllegalArgumentException("Le nom du groupe ne peut pas être vide.");
        groupeDAO.updateNom(groupeId, nom);
        DataCache.getInstance().invalidateGroupes();
    }

    /** Affecte un étudiant à un groupe. */
    public void ajouterMembre(Long groupeId, Long etudiantId) {
        groupeDAO.addMembre(groupeId, etudiantId);
        DataCache.getInstance().invalidateGroupes();
    }

    /** Retire un étudiant de son groupe. */
    public void retirerMembre(Long etudiantId) {
        groupeDAO.removeMembre(etudiantId);
        DataCache.getInstance().invalidateGroupes();
    }

    /** Retourne tous les étudiants . */
    public List<UserEntity> getEtudiants() {
        return new UserDAO().findByRole(Role.ETUDIANT);
    }
}
