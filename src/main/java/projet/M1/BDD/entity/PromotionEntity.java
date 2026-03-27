package projet.M1.BDD.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "promotions")
public class PromotionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    @OneToMany(mappedBy = "promotion")
    private List<UserEntity> list_etudiant;

    @ManyToMany(mappedBy = "list_promotion")
    private List<UserEntity> list_professeur;

    @ManyToMany
    @JoinTable(
            name = "promotion_module",
            joinColumns = @JoinColumn(name = "promotion_id"),
            inverseJoinColumns = @JoinColumn(name = "module_id")
    )
    private List<ModuleEntity> list_module;

    // Getters et Setters
    public Long getId() { return id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public List<UserEntity> getList_etudiant() { return list_etudiant; }
    public void setList_etudiant(List<UserEntity> list_etudiant) { this.list_etudiant = list_etudiant; }

    public List<UserEntity> getList_professeur() { return list_professeur; }
    public void setList_professeur(List<UserEntity> list_professeur) { this.list_professeur = list_professeur; }

    public List<ModuleEntity> getList_module() { return list_module; }
    public void setList_module(List<ModuleEntity> list_module) { this.list_module = list_module; }
}