package projet.M1.BDD.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cours_modification_requests")
public class CoursModificationRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cours_id")
    private CoursEntity cours;

    @ManyToOne
    @JoinColumn(name = "demandeur_id")
    private UserEntity demandeur;

    @ManyToOne
    @JoinColumn(name = "nouveau_creneau_id")
    private HoraireEntity nouveauCreneau;

    @ManyToOne
    @JoinColumn(name = "nouvelle_salle_id")
    private SalleEntity nouvelleSalle;

    private String raison;

    @Enumerated(EnumType.STRING)
    private StatutDemande statut;

    // Getters et Setters
    public Long getId() { return id; }

    public CoursEntity getCours() { return cours; }
    public void setCours(CoursEntity cours) { this.cours = cours; }

    public UserEntity getDemandeur() { return demandeur; }
    public void setDemandeur(UserEntity demandeur) { this.demandeur = demandeur; }

    public HoraireEntity getNouveauCreneau() { return nouveauCreneau; }
    public void setNouveauCreneau(HoraireEntity nouveauCreneau) { this.nouveauCreneau = nouveauCreneau; }

    public SalleEntity getNouvelleSalle() { return nouvelleSalle; }
    public void setNouvelleSalle(SalleEntity nouvelleSalle) { this.nouvelleSalle = nouvelleSalle; }

    public String getRaison() { return raison; }
    public void setRaison(String raison) { this.raison = raison; }

    public StatutDemande getStatut() { return statut; }
    public void setStatut(StatutDemande statut) { this.statut = statut; }
}