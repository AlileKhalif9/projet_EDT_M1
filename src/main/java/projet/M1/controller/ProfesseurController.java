package projet.M1.controller;

import projet.M1.controller.dao.CoursDAO;
import projet.M1.model.planning.Cours;
import projet.M1.model.utilisateur_systeme.Professeur;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class ProfesseurController {

    private final Professeur professeur;
    private final CoursDAO coursDAO;

    public ProfesseurController(Professeur professeur, CoursDAO coursDAO) {
        this.professeur = professeur;
        this.coursDAO = coursDAO;
    }

    // EDT du professeur connecté sur une semaine
    public List<Cours> getEmploiDuTempsSemaine(LocalDate semaine) {
        LocalDate lundi = semaine.with(DayOfWeek.MONDAY);
        LocalDate vendredi = semaine.with(DayOfWeek.FRIDAY);
        return coursDAO.findByProfesseurAndSemaine(professeur, lundi, vendredi);
    }

    // EDT de la semaine courante
    public List<Cours> getEmploiDuTempsSemaineActuelle() {
        return getEmploiDuTempsSemaine(LocalDate.now());
    }
}