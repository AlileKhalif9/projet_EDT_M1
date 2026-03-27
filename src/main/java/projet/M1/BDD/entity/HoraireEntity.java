package projet.M1.BDD.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "horaires")
public class HoraireEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate jour;
    private LocalTime heureDebut;
    private LocalTime heureFin;

    // Getters et Setters
    public Long getId() { return id; }

    public LocalDate getJour() { return jour; }
    public void setJour(LocalDate jour) { this.jour = jour; }

    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }

    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }
}