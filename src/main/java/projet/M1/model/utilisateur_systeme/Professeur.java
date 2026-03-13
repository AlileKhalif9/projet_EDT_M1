package projet.M1.model.utilisateur_systeme;

import projet.M1.model.academique.Promotion;

import java.util.List;

public final class Professeur extends Utilisateur {

    private List<Promotion> list_promotion;

    public Professeur(String nom, String prenom, int age, String login, String motDePasse,
                      List<Promotion> list_promotion) {
        super(nom, prenom, age, login, motDePasse);
        this.list_promotion = list_promotion;
    }

    public List<Promotion> getList_promotion() { return list_promotion; }
    public void setList_promotion(List<Promotion> list_promotion) { this.list_promotion = list_promotion; }
}