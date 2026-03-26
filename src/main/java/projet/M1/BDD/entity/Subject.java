package projet.M1.BDD.entity;

import jakarta.persistence.*;
import java.util.Set;

/**
 * Représente une matière (Subject) enseignée dans le système.
 * Une matière peut être associée à plusieurs cours.
 */
@Entity
@Table(name = "subjects")
public class Subject {

    /** Identifiant unique de la matière */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nom de la matière */
    private String name;

    /** Les cours associés à cette matière */
    @OneToMany(mappedBy = "subject")
    private Set<Course> courses;

    // Getters et Setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Set<Course> getCourses() { return courses; }
    public void setCourses(Set<Course> courses) { this.courses = courses; }
}