package projet.M1.academique;

import projet.M1.utilisateur_systeme.Etudiant;

import java.util.List;

public class Promotion {
    private String nom;

    private List<Etudiant> list_etudiant;
    private List<Module> list_module;

    public Promotion(String nom, List<Etudiant> list_etudiant, List<Module> list_module) {
        this.nom = nom;
        this.list_etudiant = list_etudiant;
        this.list_module = list_module;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public List<Etudiant> getList_etudiant() {
        return list_etudiant;
    }

    public void setList_etudiant(List<Etudiant> list_etudiant) {
        this.list_etudiant = list_etudiant;
    }

    public List<Module> getList_module() {
        return list_module;
    }

    public void setList_module(List<Module> list_module) {
        this.list_module = list_module;
    }
}
