package projet.M1.academique;

import projet.M1.utilisateur_systeme.Etudiant;

import java.util.List;

public class Groupe_Etudiant {
    private String nom;

    private List<Etudiant> list_etudiant;

    public Groupe_Etudiant(String nom, List<Etudiant> list_etudiant) {
        this.nom = nom;
        this.list_etudiant = list_etudiant;
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
}
