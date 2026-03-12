package projet.M1.model.planning;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Horaire {
    private LocalDate jour;
    private LocalDateTime heureDebut;
    private LocalDateTime heureFin;

    public Horaire(LocalDate jour, LocalDateTime heureDebut, LocalDateTime heureFin) {
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }

    public LocalDate getJour() {
        return jour;
    }

    public void setJour(LocalDate jour) {
        this.jour = jour;
    }

    public LocalDateTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalDateTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalDateTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalDateTime heureFin) {
        this.heureFin = heureFin;
    }
}
