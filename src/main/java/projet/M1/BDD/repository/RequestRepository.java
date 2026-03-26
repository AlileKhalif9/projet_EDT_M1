package projet.M1.BDD.repository;

import com.timetable.entity.CourseModificationRequest;
import com.timetable.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository pour gérer les demandes de modification de cours.
 */
public interface RequestRepository extends JpaRepository<CourseModificationRequest, Long> {

    /**
     * Récupère toutes les demandes faites par un professeur.
     * @param requester professeur qui a fait la demande
     * @return liste des demandes
     */
    List<CourseModificationRequest> findByRequester(User requester);
}