package projet.M1.BDD.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "utilisateurs")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Attributs communs : Utilisateur
    private String nom;
    private String prenom;
    private int age;
    private String login;
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    private Role role;

    // Attributs Etudiant
    @ManyToOne
    @JoinColumn(name = "promotion_id", nullable = true)
    private PromotionEntity promotion;

    @ManyToOne
    @JoinColumn(name = "groupe_id", nullable = true)
    private GroupeEtudiantEntity groupe;

    @ManyToMany
    @JoinTable(
            name = "etudiant_module",
            joinColumns = @JoinColumn(name = "etudiant_id"),
            inverseJoinColumns = @JoinColumn(name = "module_id")
    )
    private List<ModuleEntity> list_module;

    @OneToMany(mappedBy = "etudiant", cascade = CascadeType.ALL)
    private List<NoteEntity> list_note;

    // Attributs Professeur
    @ManyToMany
    @JoinTable(
            name = "professeur_promotion",
            joinColumns = @JoinColumn(name = "professeur_id"),
            inverseJoinColumns = @JoinColumn(name = "promotion_id")
    )
    private List<PromotionEntity> list_promotion;

    // Getters et Setters
    public Long getId() { return id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public PromotionEntity getPromotion() { return promotion; }
    public void setPromotion(PromotionEntity promotion) { this.promotion = promotion; }

    public GroupeEtudiantEntity getGroupe() { return groupe; }
    public void setGroupe(GroupeEtudiantEntity groupe) { this.groupe = groupe; }

    public List<ModuleEntity> getList_module() { return list_module; }
    public void setList_module(List<ModuleEntity> list_module) { this.list_module = list_module; }

    public List<NoteEntity> getList_note() { return list_note; }
    public void setList_note(List<NoteEntity> list_note) { this.list_note = list_note; }

    public List<PromotionEntity> getList_promotion() { return list_promotion; }
    public void setList_promotion(List<PromotionEntity> list_promotion) { this.list_promotion = list_promotion; }
}