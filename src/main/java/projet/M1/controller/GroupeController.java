package projet.M1.controller;

import projet.M1.BDD.dao.GroupeDAO;
import projet.M1.BDD.entity.GroupeEtudiantEntity;

import java.util.List;

/**
 * Back-end : gestion des groupes d'étudiants.
 * Le front appelle getAllGroupes() — jamais GroupeDAO directement.
 */
public class GroupeController {

    private final GroupeDAO groupeDAO;

    public GroupeController(GroupeDAO groupeDAO) {
        this.groupeDAO = groupeDAO;
    }

    public List<GroupeEtudiantEntity> getAllGroupes() {
        return groupeDAO.findAll();
    }
}
