package projet.M1.BDD.service;

import projet.M1.BDD.entity.CoursEntity;
import projet.M1.BDD.entity.UserEntity;
import projet.M1.BDD.repository.CoursRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class ScheduleService {

    private final CoursRepository coursRepository;

    public ScheduleService(CoursRepository coursRepository) {
        this.coursRepository = coursRepository;
    }

    // Retourne les cours d'un etudiant
    public List<CoursEntity> getEmploiDuTempsEtudiant(UserEntity etudiant, LocalDate semaine) {
        LocalDate lundi   = semaine.with(DayOfWeek.MONDAY);
        LocalDate vendredi = semaine.with(DayOfWeek.FRIDAY);
        return coursRepository.findByEtudiantAndSemaine(etudiant, lundi, vendredi);
    }

    // Retourne les cours d'un prof
    public List<CoursEntity> getEmploiDuTempsProfesseur(UserEntity professeur, LocalDate semaine) {
        LocalDate lundi    = semaine.with(DayOfWeek.MONDAY);
        LocalDate vendredi = semaine.with(DayOfWeek.FRIDAY);
        return coursRepository.findByProfesseurAndSemaine(professeur, lundi, vendredi);
    }
}