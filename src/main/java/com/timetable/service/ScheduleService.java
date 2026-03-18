package com.timetable.service;

import com.timetable.entity.Course;
import com.timetable.entity.User;
import com.timetable.repository.CourseRepository;

import java.util.List;

/**
 * Service pour gérer les cours et les inscriptions.
 */
public class ScheduleService {

    private final CourseRepository courseRepository;

    public ScheduleService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    /**
     * Récupère tous les cours d'un utilisateur (étudiant ou professeur)
     * @param user l'utilisateur
     * @return liste de cours
     */
    public List<Course> getUserCourses(User user) {
        if ("prof".equals(user.getRole())) {
            return courseRepository.findByProfessor(user);
        } else {
            return courseRepository.findByStudentsContaining(user);
        }
    }

    /**
     * Inscrit un étudiant à un cours
     * @param user étudiant
     * @param course cours
     */
    public void enrollStudent(User user, Course course) {
        course.getStudents().add(user);
        courseRepository.save(course);
    }

    /**
     * Supprime un étudiant d'un cours
     * @param user étudiant
     * @param course cours
     */
    public void removeStudent(User user, Course course) {
        course.getStudents().remove(user);
        courseRepository.save(course);
    }
}