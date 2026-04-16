package projet.M1.controller;

import projet.M1.BDD.DataCache;
import projet.M1.BDD.dao.SalleDAO;
import projet.M1.BDD.entity.SalleEntity;

import java.util.List;

/**
 * Back-end : gestion des salles.
 */
public class SalleController {

    private final SalleDAO salleDAO;

    public SalleController(SalleDAO salleDAO) {
        this.salleDAO = salleDAO;
    }

    public List<SalleEntity> getAllSalles() {
        List<SalleEntity> cached = DataCache.getInstance().getSalles();
        if (!cached.isEmpty()) return cached;
        return salleDAO.findAll();
    }

    /**
     * Modifie les équipements d'une salle en BDD.
     * Met aussi à jour l'objet en mémoire pour refléter le changement immédiatement.
     */
    public void modifierEquipements(Long salleId, List<String> equipements) {
        salleDAO.modifierEquipements(salleId, equipements);
    }
}