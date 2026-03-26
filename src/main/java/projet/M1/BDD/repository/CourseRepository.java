package projet.M1.BDD.repository;

import com.timetable.entity.Course;
import com.timetable.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository pour gérer les cours.
 */
public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * Récupère tous les cours où un utilisateur est inscrit (étudiant).
     * @param student l'utilisateur
     * @return liste de cours
     */
    List<Course> findByStudentsContaining(User student);

    /**
     * Récupère tous les cours enseignés par un professeur.
     * @param professor l'utilisateur
     * @return liste de cours
     */
    List<Course> findByProfessor(User professor);
}