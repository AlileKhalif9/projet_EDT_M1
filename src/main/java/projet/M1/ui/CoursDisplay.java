package projet.M1.ui;

import projet.M1.model.planning.TypeCours;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * C'est créé par MockDataService (données fictives) et sera remplacé
 * par un vrai service quand la BDD sera branchée.
 */
public record CoursDisplay(
        String nom,
        TypeCours typeCours,
        String nomGroupe,
        String nomProf,
        String nomSalle,
        LocalDate jour,
        LocalTime heureDebut,
        LocalTime heureFin
) {}
