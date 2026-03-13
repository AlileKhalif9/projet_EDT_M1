package projet.M1.controller;

import projet.M1.controller.dao.UtilisateurDAO;
import projet.M1.model.utilisateur_systeme.Utilisateur;

import java.util.Random;

public class Service_Inscription {

    private final UtilisateurDAO utilisateurDAO;
    private final Random random = new Random();

    public Service_Inscription(UtilisateurDAO utilisateurDAO) {
        this.utilisateurDAO = utilisateurDAO;
    }

    public void inscrireUtilisateur(Utilisateur utilisateur) {
        boolean aLogin = utilisateur.getLogin() != null && !utilisateur.getLogin().isEmpty();
        boolean aMotDePasse = utilisateur.getMotDePasse() != null && !utilisateur.getMotDePasse().isEmpty();

        if (aLogin && aMotDePasse) {
            throw new IllegalStateException("L'utilisateur a déjà un login et un mot de passe.");
        }

        if (aLogin || aMotDePasse) {
            throw new IllegalStateException("État incohérent : l'utilisateur a un login sans mot de passe ou inversement.");
        }

        String login = genererLoginUnique(utilisateur);
        utilisateur.setLogin(login);

        String motDePasse = genererMotDePasse();
        utilisateur.setMotDePasse(motDePasse);

        utilisateurDAO.sauvegarderUtilisateur(utilisateur);
    }

    private String genererLoginUnique(Utilisateur utilisateur) {
        String prenom = utilisateur.getPrenom().toLowerCase();
        String nom = utilisateur.getNom().toLowerCase();
        String base = String.valueOf(prenom.charAt(0)) + nom;

        String login;
        do {
            int deuxChiffres = random.nextInt(90) + 10;
            login = base + deuxChiffres;
        } while (utilisateurDAO.loginExiste(login));

        return login;
    }

    private String genererMotDePasse() {
        // à définir avec ton groupe
        return null;
    }
}