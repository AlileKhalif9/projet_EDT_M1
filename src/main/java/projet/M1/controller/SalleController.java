package projet.M1.controller;

import projet.M1.BDD.DataCache;
import projet.M1.BDD.dao.SalleDAO;
import projet.M1.BDD.entity.SalleEntity;

import java.util.List;

/**
 * Back-end : gestion des salles.
 * Utilise le DataCache — pas de requête réseau si les données sont déjà chargées.
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
}
