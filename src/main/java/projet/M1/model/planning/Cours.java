package projet.M1.model.planning;

import projet.M1.model.utilisateur_systeme.Etudiant;
import projet.M1.model.utilisateur_systeme.Professeur;

import java.util.List;

public class Cours {
    private Horaire horaire;
    private Salle salle;

    private List<Etudiant> list_etudiant;
    private List<Professeur> list_professeur;

    public Cours(Horaire creneau, Salle salle, List<Etudiant> list_etudiant, List<Professeur> list_professeur) {
        this.horaire = creneau;
        this.salle = salle;
        this.list_etudiant = list_etudiant;
        this.list_professeur = list_professeur;
    }

    public Horaire getHoraire() {
        return horaire;
    }

    public void setHoraire(Horaire horaire) {
        this.horaire = horaire;
    }

    public Salle getSalle() {
        return salle;
    }

    public void setSalle(Salle salle) {
        this.salle = salle;
    }

    public List<Etudiant> getList_etudiant() {
        return list_etudiant;
    }

    public void setList_etudiant(List<Etudiant> list_etudiant) {
        this.list_etudiant = list_etudiant;
    }

    public List<Professeur> getList_professeur() {
        return list_professeur;
    }

    public void setList_professeur(List<Professeur> list_professeur) {
        this.list_professeur = list_professeur;
    }
}
