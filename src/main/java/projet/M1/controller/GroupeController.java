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

    /** UC8/US17 — Crée et persiste un nouveau groupe vide. */
    public GroupeEtudiantEntity creerGroupe(String nom) {
        if (nom == null || nom.isBlank())
            throw new IllegalArgumentException("Le nom du groupe ne peut pas être vide.");
        return groupeDAO.save(nom);
    }

    /** UC8/US17 — Renomme un groupe existant en BDD. */
    public void renommerGroupe(Long groupeId, String nom) {
        if (nom == null || nom.isBlank())
            throw new IllegalArgumentException("Le nom du groupe ne peut pas être vide.");
        groupeDAO.updateNom(groupeId, nom);
    }

    /** UC8/US17 — Affecte un étudiant à un groupe. */
    public void ajouterMembre(Long groupeId, Long etudiantId) {
        groupeDAO.addMembre(groupeId, etudiantId);
    }

    /** UC8/US17 — Retire un étudiant de son groupe. */
    public void retirerMembre(Long etudiantId) {
        groupeDAO.removeMembre(etudiantId);
    }
}
