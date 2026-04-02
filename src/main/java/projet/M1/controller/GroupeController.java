package projet.M1.controller;

import projet.M1.BDD.DataCache;
import projet.M1.BDD.dao.GroupeDAO;
import projet.M1.BDD.entity.GroupeEtudiantEntity;

import java.util.List;

/**
 * Back-end : gestion des groupes d'étudiants.
 * Utilise le DataCache — pas de requête réseau si les données sont déjà chargées.
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
}
