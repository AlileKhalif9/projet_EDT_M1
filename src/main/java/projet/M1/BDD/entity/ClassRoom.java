package projet.M1.BDD.entity;

import jakarta.persistence.*;
import java.util.Set;

/**
 * Représente une salle où les cours ont lieu.
 * Une salle peut accueillir plusieurs cours.
 */
@Entity
@Table(name = "classrooms")
public class ClassRoom {

    /** Identifiant unique de la salle */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nom de la salle */
    private String name;

    /** Capacité de la salle */
    private int capacity;

    /** Les cours qui se déroulent dans cette salle */
    @OneToMany(mappedBy = "classRoom")
    private Set<Course> courses;

    // Getters et Setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public Set<Course> getCourses() { return courses; }
    public void setCourses(Set<Course> courses) { this.courses = courses; }
}