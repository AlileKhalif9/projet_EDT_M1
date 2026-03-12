package projet.M1.controller;

import projet.M1.controller.dao.UtilisateurDAO;
import projet.M1.model.utilisateur_systeme.Utilisateur;
import java.util.Optional;

import java.util.logging.Logger;

public class AuthController {

    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());

    private final UtilisateurDAO utilisateurDAO;

    public AuthController(UtilisateurDAO utilisateurDAO) {
        this.utilisateurDAO = utilisateurDAO;
    }

    public Optional<Utilisateur> connecter(String login, String motDePasse) {
        Optional<Utilisateur> utilisateurOpt = utilisateurDAO.findByLogin(login, motDePasse);

        if(utilisateurOpt.isPresent()) {
            LOGGER.info("Connexion réussie pour : " + utilisateurOpt.get().getNom());
        } else {
            LOGGER.warning("Login ou mot de passe incorrect pour : " + login);
        }

        return utilisateurOpt;
    }
}