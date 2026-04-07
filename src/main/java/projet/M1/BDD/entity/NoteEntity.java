package projet.M1.BDD.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "notes")
public class NoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Intitulé du contrôle : ex "DS1", "TP noté", "Examen final" */
    private String intitule;

    private float valeur;
    private float coefficient;

    @ManyToOne
    @JoinColumn(name = "module_id")
    private ModuleEntity module;

    @ManyToOne
    @JoinColumn(name = "etudiant_id")
    private UserEntity etudiant;

    // Getters et Setters
    public Long getId() { return id; }

    public String getIntitule() { return intitule; }
    public void setIntitule(String intitule) { this.intitule = intitule; }

    public float getValeur() { return valeur; }
    public void setValeur(float valeur) { this.valeur = valeur; }

    public float getCoefficient() { return coefficient; }
    public void setCoefficient(float coefficient) { this.coefficient = coefficient; }

    public ModuleEntity getModule() { return module; }
    public void setModule(ModuleEntity module) { this.module = module; }

    public UserEntity getEtudiant() { return etudiant; }
    public void setEtudiant(UserEntity etudiant) { this.etudiant = etudiant; }
}