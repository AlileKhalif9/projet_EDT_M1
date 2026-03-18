package com.timetable.entity;

import jakarta.persistence.*;
import java.time.LocalTime;

/**
 * Représente l'horaire d'un cours (jour et heure).
 */
@Entity
@Table(name = "horaires")
public class Horaire {

    /** Identifiant unique de l'horaire */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Jour du cours (ex: Lundi, Mardi) */
    private String day;

    /** Heure de début */
    private LocalTime startTime;

    /** Heure de fin */
    private LocalTime endTime;

    /** Cours associé à cet horaire */
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    // Getters et Setters
    public Long getId() { return id; }
    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
}