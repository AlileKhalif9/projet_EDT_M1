package com.timetable.entity;

import jakarta.persistence.*;
import java.util.Set;

/**
 * Représente un utilisateur du système.
 * Peut être un étudiant, un professeur ou un administrateur.
 * <p>
 * Les utilisateurs peuvent être inscrits à plusieurs cours (en tant qu'étudiants)
 * et peuvent enseigner plusieurs cours (en tant que professeurs).
 * </p>
 */
@Entity
@Table(name = "users")
public class User {

    /** Identifiant unique de l'utilisateur, généré automatiquement */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Prénom de l'utilisateur */
    private String firstname;

    /** Nom de l'utilisateur */
    private String lastname;

    /** Email de l'utilisateur, utilisé pour l'authentification */
    private String email;

    /** Rôle de l'utilisateur : "student", "prof", "admin" */
    private String role;

    /**
     * Les cours auxquels l'utilisateur est inscrit en tant qu'étudiant.
     * Relation ManyToMany avec Course.
     */
    @ManyToMany(mappedBy = "students")
    private Set<Course> enrolledCourses;

    /**
     * Les cours que l'utilisateur enseigne en tant que professeur.
     * Relation OneToMany avec Course.
     */
    @OneToMany(mappedBy = "professor")
    private Set<Course> taughtCourses;

    // --------------------
    // Getters et Setters
    // --------------------

    /** @return l'identifiant unique de l'utilisateur */
    public Long getId() { return id; }

    /** @return le prénom de l'utilisateur */
    public String getFirstname() { return firstname; }

    /** @param firstname définit le prénom de l'utilisateur */
    public void setFirstname(String firstname) { this.firstname = firstname; }

    /** @return le nom de l'utilisateur */
    public String getLastname() { return lastname; }

    /** @param lastname définit le nom de l'utilisateur */
    public void setLastname(String lastname) { this.lastname = lastname; }

    /** @return l'email de l'utilisateur */
    public String getEmail() { return email; }

    /** @param email définit l'email de l'utilisateur */
    public void setEmail(String email) { this.email = email; }

    /** @return le rôle de l'utilisateur */
    public String getRole() { return role; }

    /** @param role définit le rôle de l'utilisateur */
    public void setRole(String role) { this.role = role; }

    /** @return l'ensemble des cours où l'utilisateur est inscrit */
    public Set<Course> getEnrolledCourses() { return enrolledCourses; }

    /** @param enrolledCourses définit l'ensemble des cours où l'utilisateur est inscrit */
    public void setEnrolledCourses(Set<Course> enrolledCourses) { this.enrolledCourses = enrolledCourses; }

    /** @return l'ensemble des cours que l'utilisateur enseigne */
    public Set<Course> getTaughtCourses() { return taughtCourses; }

    /** @param taughtCourses définit l'ensemble des cours que l'utilisateur enseigne */
    public void setTaughtCourses(Set<Course> taughtCourses) { this.taughtCourses = taughtCourses; }
}