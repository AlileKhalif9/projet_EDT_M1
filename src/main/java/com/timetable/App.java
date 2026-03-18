package com.timetable;

import com.timetable.entity.*;
import com.timetable.repository.*;
import com.timetable.service.*;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.HashSet;

public class App {

    public static void main(String[] args) {

        // Crée le contexte JPA
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("timetablePU");
        EntityManager em = emf.createEntityManager();

        System.out.println("Connexion BDD OK !");

        // Initialisation transaction
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // ---------------------
        // Création d'un utilisateur (étudiant et prof)
        // ---------------------
        User student = new User();
        student.setFirstname("Alice");
        student.setLastname("Martin");
        student.setEmail("alice.martin@example.com");
        student.setRole("student");

        User professor = new User();
        professor.setFirstname("Dr");
        professor.setLastname("Dupont");
        professor.setEmail("dupont@example.com");
        professor.setRole("prof");

        em.persist(student);
        em.persist(professor);

        // ---------------------
        // Création d'une matière
        // ---------------------
        Subject math = new Subject();
        math.setName("Mathématiques");
        em.persist(math);

        // ---------------------
        // Création d'une salle
        // ---------------------
        ClassRoom room101 = new ClassRoom();
        room101.setName("Salle 101");
        room101.setCapacity(30);
        em.persist(room101);

        // ---------------------
        // Création d'un cours
        // ---------------------
        Course course = new Course();
        course.setName("Algèbre 1");
        course.setSubject(math);
        course.setClassRoom(room101);
        course.setProfessor(professor);
        course.setStudents(new HashSet<>()); // initialiser set vide
        em.persist(course);

        // ---------------------
        // Inscription de l'étudiant au cours
        // ---------------------
        course.getStudents().add(student);
        em.persist(course);

        // ---------------------
        // Création d'un horaire
        // ---------------------
        Horaire horaire = new Horaire();
        horaire.setCourse(course);
        horaire.setDay("Lundi");
        horaire.setStartTime(LocalTime.of(9, 0));
        horaire.setEndTime(LocalTime.of(10, 30));
        em.persist(horaire);

        // ---------------------
        // Création d'une demande de modification
        // ---------------------
        CourseModificationRequest request = new CourseModificationRequest();
        request.setCourse(course);
        request.setRequester(professor);
        request.setStatus("PENDING");
        em.persist(request);

        // Commit transaction
        tx.commit();

        // Fermeture EntityManager
        em.close();
        emf.close();

        System.out.println("Données insérées avec succès !");
    }
}