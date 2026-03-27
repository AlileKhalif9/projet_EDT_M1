package projet.M1.BDD.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "groupes_etudiants")
public class GroupeEtudiantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    @OneToMany(mappedBy = "groupe")
    private List<UserEntity> list_etudiant;

    // Getters et Setters
    public Long getId() { return id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public List<UserEntity> getList_etudiant() { return list_etudiant; }
    public void setList_etudiant(List<UserEntity> list_etudiant) { this.list_etudiant = list_etudiant; }
}