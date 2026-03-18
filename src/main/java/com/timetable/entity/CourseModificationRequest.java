package com.timetable.entity;

import jakarta.persistence.*;

/**
 * Représente une demande de modification d'un cours
 * (déplacement, annulation, etc.) par un professeur.
 */
@Entity
@Table(name = "course_modification_requests")
public class CourseModificationRequest {

    /** Identifiant unique de la demande */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Cours concerné par la demande */
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    /** Utilisateur qui fait la demande (professeur) */
    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;

    /** Statut de la demande : "PENDING", "ACCEPTED", "REFUSED" */
    private String status;

    // Getters et Setters
    public Long getId() { return id; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public User getRequester() { return requester; }
    public void setRequester(User requester) { this.requester = requester; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}