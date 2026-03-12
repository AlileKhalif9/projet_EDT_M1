package projet.M1.model.planning;

import java.util.List;

public class Salle {
    private String nom;
    private int place;
    private List<String> liste_materiel;

    public Salle(String nom, int place, List<String> liste_materiel) {
        this.nom = nom;
        this.place = place;
        this.liste_materiel = liste_materiel;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public List<String> getListe_materiel() {
        return liste_materiel;
    }

    public void setListe_materiel(List<String> liste_materiel) {
        this.liste_materiel = liste_materiel;
    }
}
