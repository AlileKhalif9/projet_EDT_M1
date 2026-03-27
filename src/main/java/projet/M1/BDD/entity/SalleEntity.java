package projet.M1.BDD.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "salles")
public class SalleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private int place;

    @ElementCollection
    @CollectionTable(
            name = "salle_materiel",
            joinColumns = @JoinColumn(name = "salle_id")
    )
    @Column(name = "materiel")
    private List<String> liste_materiel;

    // Getters et Setters
    public Long getId() { return id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public int getPlace() { return place; }
    public void setPlace(int place) { this.place = place; }

    public List<String> getListe_materiel() { return liste_materiel; }
    public void setListe_materiel(List<String> liste_materiel) { this.liste_materiel = liste_materiel; }
}