package projet.M1.model.academique;

import projet.M1.model.planning.Cours;
import projet.M1.model.utilisateur_systeme.Etudiant;
import projet.M1.model.utilisateur_systeme.Professeur;

import java.util.List;

public class Module {
    private String nom;
    private float moyenne;
    private float coefficient;

    private List<Note> list_Note;
    private List<Etudiant> list_etudiant;
    private List<Professeur> list_professeur;
    private Cours cours;

    public Module(String nom, float moyenne, float coefficient, List<Note> list_Note, List<Etudiant> list_etudiant, List<Professeur> list_professeur, Cours cours) {
        this.nom = nom;
        this.moyenne = moyenne;
        this.coefficient = coefficient;
        this.list_Note = list_Note;
        this.list_etudiant = list_etudiant;
        this.list_professeur = list_professeur;
        this.cours = cours;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public float getMoyenne() {
        return moyenne;
    }

    public void setMoyenne(float moyenne) {
        this.moyenne = moyenne;
    }

    public float getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(float coefficient) {
        this.coefficient = coefficient;
    }

    public List<Note> getList_Note() {
        return list_Note;
    }

    public void setList_Note(List<Note> list_Note) {
        this.list_Note = list_Note;
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

    public Cours getCours() {
        return cours;
    }

    public void setCours(Cours cours) {
        this.cours = cours;
    }
}
