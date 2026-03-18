package com.timetable.entity;

import jakarta.persistence.*;
import java.util.Set;

/**
 * Représente un cours dans le système.
 * Un cours est associé à une matière, une salle, un professeur et plusieurs étudiants.
 */
@Entity
@Table(name = "courses")
public class Course {

    /** Identifiant unique du cours */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nom du cours */
    private String name;

    /** Matière associée */
    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    /** Salle où le cours se déroule */
    @ManyToOne
    @JoinColumn(name = "classroom_id")
    private ClassRoom classRoom;

    /** Professeur responsable du cours */
    @ManyToOne
    @JoinColumn(name = "professor_id")
    private User professor;

    /** Étudiants inscrits au cours */
    @ManyToMany
    @JoinTable(name = "user_course",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> students;

    /** Horaires associés à ce cours */
    @OneToMany(mappedBy = "course")
    private Set<Horaire> horaires;

    /** Demandes de modification associées au cours */
    @OneToMany(mappedBy = "course")
    private Set<CourseModificationRequest> modificationRequests;

    // Getters et Setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public ClassRoom getClassRoom() { return classRoom; }
    public void setClassRoom(ClassRoom classRoom) { this.classRoom = classRoom; }
    public User getProfessor() { return professor; }
    public void setProfessor(User professor) { this.professor = professor; }
    public Set<User> getStudents() { return students; }
    public void setStudents(Set<User> students) { this.students = students; }
    public Set<Horaire> getHoraires() { return horaires; }
    public void setHoraires(Set<Horaire> horaires) { this.horaires = horaires; }
    public Set<CourseModificationRequest> getModificationRequests() { return modificationRequests; }
    public void setModificationRequests(Set<CourseModificationRequest> modificationRequests) { this.modificationRequests = modificationRequests; }
}