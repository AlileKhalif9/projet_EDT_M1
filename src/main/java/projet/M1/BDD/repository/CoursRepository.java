package projet.M1.BDD.repository;

import projet.M1.BDD.entity.CoursEntity;
import projet.M1.BDD.entity.PromotionEntity;
import projet.M1.BDD.entity.SalleEntity;
import projet.M1.BDD.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CoursRepository extends JpaRepository<CoursEntity, Long> {

    // Cours d'un étudiant
    List<CoursEntity> findByList_etudiantContaining(UserEntity etudiant);

    // Cours d'un professeur
    List<CoursEntity> findByList_professeurContaining(UserEntity professeur);

    // Cours d'un étudiant sur une semaine
    @Query("SELECT c FROM CoursEntity c JOIN c.list_etudiant e " +
            "WHERE e = :etudiant AND c.horaire.jour BETWEEN :lundi AND :vendredi")
    List<CoursEntity> findByEtudiantAndSemaine(
            @Param("etudiant") UserEntity etudiant,
            @Param("lundi")    LocalDate lundi,
            @Param("vendredi") LocalDate vendredi);

    // Cours d'un professeur sur une semaine
    @Query("SELECT c FROM CoursEntity c JOIN c.list_professeur p " +
            "WHERE p = :professeur AND c.horaire.jour BETWEEN :lundi AND :vendredi")
    List<CoursEntity> findByProfesseurAndSemaine(
            @Param("professeur") UserEntity professeur,
            @Param("lundi")      LocalDate lundi,
            @Param("vendredi")   LocalDate vendredi);

    // EDT d'une promotion sur une semaine
    @Query("SELECT DISTINCT c FROM CoursEntity c JOIN c.list_etudiant e " +
            "WHERE e.promotion = :promotion AND c.horaire.jour BETWEEN :lundi AND :vendredi")
    List<CoursEntity> findByPromotionAndSemaine(
            @Param("promotion") PromotionEntity promotion,
            @Param("lundi")     LocalDate lundi,
            @Param("vendredi")  LocalDate vendredi);

    // EDT d'une salle sur une semaine
    @Query("SELECT c FROM CoursEntity c " +
            "WHERE c.salle = :salle AND c.horaire.jour BETWEEN :lundi AND :vendredi")
    List<CoursEntity> findBySalleAndSemaine(
            @Param("salle")    SalleEntity salle,
            @Param("lundi")    LocalDate lundi,
            @Param("vendredi") LocalDate vendredi);

    // EDT d'une salle sur un jour précis (utile pour vérifier disponibilité)
    @Query("SELECT c FROM CoursEntity c " +
            "WHERE c.salle = :salle AND c.horaire.jour = :jour")
    List<CoursEntity> findBySalleAndJour(
            @Param("salle") SalleEntity salle,
            @Param("jour")  LocalDate jour);
}