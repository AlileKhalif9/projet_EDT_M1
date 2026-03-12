package projet.M1.model.utilisateur_systeme;

import projet.M1.model.academique.Groupe_Etudiant;
import projet.M1.model.academique.Module;
import projet.M1.model.academique.Note;
import projet.M1.model.academique.Promotion;

import java.util.List;

public class Etudiant extends Utilisateur{
    private List<Note> list_note;
    private Promotion promotion;
    private Groupe_Etudiant groupe_etudiant;
    private Module list_module;

    public Etudiant(String nom, String prenom, int age, List<Note> list_note, Promotion promotion, Groupe_Etudiant groupe_etudiant, Module list_module) {
        super(nom, prenom, age);
        this.list_note = list_note;
        this.promotion = promotion;
        this.groupe_etudiant = groupe_etudiant;
        this.list_module = list_module;
    }

    public List<Note> getList_note() {
        return list_note;
    }

    public void setList_note(List<Note> list_note) {
        this.list_note = list_note;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public void setPromotion(Promotion promotion) {
        this.promotion = promotion;
    }

    public Groupe_Etudiant getGroupe_etudiant() {
        return groupe_etudiant;
    }

    public void setGroupe_etudiant(Groupe_Etudiant groupe_etudiant) {
        this.groupe_etudiant = groupe_etudiant;
    }

    public Module getList_module() {
        return list_module;
    }

    public void setList_module(Module list_module) {
        this.list_module = list_module;
    }
}
