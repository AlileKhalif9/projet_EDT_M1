package projet.M1.BDD.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "modules")
public class ModuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private float moyenne;
    private float coefficient;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL)
    private List<NoteEntity> list_Note;

    @ManyToMany(mappedBy = "list_module")
    private List<UserEntity> list_etudiant;

    @ManyToMany
    @JoinTable(
            name = "professeur_module",
            joinColumns = @JoinColumn(name = "module_id"),
            inverseJoinColumns = @JoinColumn(name = "professeur_id")
    )
    private List<UserEntity> list_professeur;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "cours_id")
    private CoursEntity cours;

    @ManyToMany(mappedBy = "list_module")
    private List<PromotionEntity> list_promotion;

    // Getters et Setters
    public Long getId() { return id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public float getMoyenne() { return moyenne; }
    public void setMoyenne(float moyenne) { this.moyenne = moyenne; }

    public float getCoefficient() { return coefficient; }
    public void setCoefficient(float coefficient) { this.coefficient = coefficient; }

    public List<NoteEntity> getList_Note() { return list_Note; }
    public void setList_Note(List<NoteEntity> list_Note) { this.list_Note = list_Note; }

    public List<UserEntity> getList_etudiant() { return list_etudiant; }
    public void setList_etudiant(List<UserEntity> list_etudiant) { this.list_etudiant = list_etudiant; }

    public List<UserEntity> getList_professeur() { return list_professeur; }
    public void setList_professeur(List<UserEntity> list_professeur) { this.list_professeur = list_professeur; }

    public CoursEntity getCours() { return cours; }
    public void setCours(CoursEntity cours) { this.cours = cours; }

    public List<PromotionEntity> getList_promotion() { return list_promotion; }
    public void setList_promotion(List<PromotionEntity> list_promotion) { this.list_promotion = list_promotion; }
}