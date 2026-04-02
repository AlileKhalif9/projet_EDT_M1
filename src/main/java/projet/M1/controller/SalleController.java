package projet.M1.controller;

import projet.M1.BDD.dao.SalleDAO;
import projet.M1.BDD.entity.SalleEntity;

import java.util.List;

/**
 * Back-end : gestion des salles.
 * Le front appelle getAllSalles() — jamais SalleDAO directement.
 */
public class SalleController {

    private final SalleDAO salleDAO;

    public SalleController(SalleDAO salleDAO) {
        this.salleDAO = salleDAO;
    }

    public List<SalleEntity> getAllSalles() {
        return salleDAO.findAll();
    }
}
