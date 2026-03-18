package com.timetable;

import com.timetable.entity.User;
import jakarta.persistence.*;

public class App {

    public static void main(String[] args) {

        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("timetablePU");

        EntityManager em = emf.createEntityManager();

        System.out.println("Connexion BDD OK");

        // Test : créer un utilisateur
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        User u = new User();
        u.setFirstname("Paul");
        u.setLastname("Dupont");
        u.setEmail("paul.dupont@example.com");

        em.persist(u);
        tx.commit();

        em.close();
        emf.close();

        System.out.println("Utilisateur créé !");
    }
}
