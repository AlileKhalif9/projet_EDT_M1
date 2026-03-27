package projet.M1.controller.dao;

import projet.M1.model.planning.Cours;
import projet.M1.model.utilisateur_systeme.Etudiant;
import projet.M1.model.utilisateur_systeme.Professeur;
import projet.M1.model.academique.Promotion;
import projet.M1.model.planning.Salle;

import java.time.LocalDate;
import java.util.List;

public interface CoursDAO {

    // EDT d'un étudiant sur une semaine
    List<Cours> findByEtudiantAndSemaine(Etudiant etudiant, LocalDate lundi, LocalDate vendredi);

    // EDT d'un professeur sur une semaine
    List<Cours> findByProfesseurAndSemaine(Professeur professeur, LocalDate lundi, LocalDate vendredi);

    // EDT d'une promotion sur une semaine
    List<Cours> findByPromotionAndSemaine(Promotion promotion, LocalDate lundi, LocalDate vendredi);

    // EDT d'une salle sur une semaine
    List<Cours> findBySalleAndSemaine(Salle salle, LocalDate lundi, LocalDate vendredi);
}