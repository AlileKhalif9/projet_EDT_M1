package projet.M1.controller;

import projet.M1.model.academique.Module;
import projet.M1.model.planning.Cours;
import projet.M1.model.utilisateur_systeme.Etudiant;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class EtudiantController {

    private final Etudiant etudiant;

    public EtudiantController(Etudiant etudiant) {
        this.etudiant = etudiant;
    }

    public List<Cours> getEmploiDuTempsSemaine(LocalDate semaine) {
        LocalDate lundi = semaine.with(DayOfWeek.MONDAY);
        LocalDate vendredi = semaine.with(DayOfWeek.FRIDAY);

        return etudiant.getList_module().stream()
                .map(Module::getCours)
                .filter(cours -> {
                    LocalDate jour = cours.getHoraire().getJour();
                    return !jour.isBefore(lundi) && !jour.isAfter(vendredi);
                })
                .collect(Collectors.toList());
    }
}