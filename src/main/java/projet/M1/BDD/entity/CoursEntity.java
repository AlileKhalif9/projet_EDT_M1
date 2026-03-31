package projet.M1.BDD.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "cours")
public class CoursEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nom du cours (ex: "Algorithmique", "Base de données")
    // Ajouté lors de l'intégration front-back pour affichage dans la grille EDT
    private String nom;

    // Type du cours : "CM", "TD", "TP", "EXAMEN", "ANNULE"
    // Ajouté lors de l'intégration pour la coloration dans la grille EDT
    private String typeCours;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "horaire_id")
    private HoraireEntity horaire;

    @ManyToOne
    @JoinColumn(name = "salle_id")
    private SalleEntity salle;

    @ManyToMany
    @JoinTable(
            name = "etudiant_cours",
            joinColumns = @JoinColumn(name = "cours_id"),
            inverseJoinColumns = @JoinColumn(name = "etudiant_id")
    )
    private List<UserEntity> list_etudiant;

    @ManyToMany
    @JoinTable(
            name = "professeur_cours",
            joinColumns = @JoinColumn(name = "cours_id"),
            inverseJoinColumns = @JoinColumn(name = "professeur_id")
    )
    private List<UserEntity> list_professeur;

    // Getters et Setters
    public Long getId() { return id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getTypeCours() { return typeCours; }
    public void setTypeCours(String typeCours) { this.typeCours = typeCours; }

    public HoraireEntity getHoraire() { return horaire; }
    public void setHoraire(HoraireEntity horaire) { this.horaire = horaire; }

    public SalleEntity getSalle() { return salle; }
    public void setSalle(SalleEntity salle) { this.salle = salle; }

    public List<UserEntity> getList_etudiant() { return list_etudiant; }
    public void setList_etudiant(List<UserEntity> list_etudiant) { this.list_etudiant = list_etudiant; }

    public List<UserEntity> getList_professeur() { return list_professeur; }
    public void setList_professeur(List<UserEntity> list_professeur) { this.list_professeur = list_professeur; }
}