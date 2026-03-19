package projet.M1.controller.dao;

import projet.M1.model.utilisateur_systeme.Etudiant;
import projet.M1.model.utilisateur_systeme.Gestionnaire_Planning;
import projet.M1.model.utilisateur_systeme.Professeur;
import projet.M1.model.utilisateur_systeme.Utilisateur;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * TEMPORAIRE — remplace la vraie BDD le temps qu'elle soit prête.
 *
 * J'implémente votre interface UtilisateurDAO sans la modifier.
 * Les utilisateurs sont stockés en mémoire avec 3 comptes de test :
 *   etudiant / 1234
 *   professeur / 1234
 *   gestionnaire / 1234
 *
 * Quand votre vrai DAO sera prêt, dans LoginController.java remplacez juste :
 *   new AuthController(new MockUtilisateurDAO())
 * par :
 *   new AuthController(new VotreVraiDAO())
 * Le reste du front n'a pas besoin de changer.
 */
public class MockUtilisateurDAO implements UtilisateurDAO {

    private final List<Utilisateur> utilisateurs = new ArrayList<>();

    public MockUtilisateurDAO() {
        utilisateurs.add(new Etudiant(
                "Dupont", "Alice", 20,
                "etudiant", "1234",
                new ArrayList<>(), null, null, new ArrayList<>()
        ));
        utilisateurs.add(new Professeur(
                "Martin", "Jean", 45,
                "professeur", "1234",
                new ArrayList<>()
        ));
        utilisateurs.add(new Gestionnaire_Planning(
                "Admin", "Système", 35,
                "gestionnaire", "1234"
        ));
    }

    @Override
    public Optional<Utilisateur> findByLogin(String login, String motDePasse) {
        return utilisateurs.stream()
                .filter(u -> u.getLogin().equals(login)
                          && u.getMotDePasse().equals(motDePasse))
                .findFirst();
    }

    @Override
    public boolean loginExiste(String login) {
        return utilisateurs.stream().anyMatch(u -> u.getLogin().equals(login));
    }

    @Override
    public void sauvegarderUtilisateur(Utilisateur utilisateur) {
        utilisateurs.add(utilisateur);
    }
}
