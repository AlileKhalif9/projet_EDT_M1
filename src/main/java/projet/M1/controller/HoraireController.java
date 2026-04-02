package projet.M1.controller;

import projet.M1.BDD.dao.HoraireDAO;
import projet.M1.BDD.entity.HoraireEntity;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Back-end : gestion des horaires.
 * Le front appelle findOrCreate() — jamais HoraireDAO directement.
 */
public class HoraireController {

    private final HoraireDAO horaireDAO;

    public HoraireController(HoraireDAO horaireDAO) {
        this.horaireDAO = horaireDAO;
    }

    public HoraireEntity findOrCreate(LocalDate jour, LocalTime heureDebut, LocalTime heureFin) {
        return horaireDAO.findOrCreate(jour, heureDebut, heureFin);
    }
}
