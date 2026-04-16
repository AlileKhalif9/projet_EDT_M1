package projet.M1.controller;

import projet.M1.BDD.dao.UserDAO;
import projet.M1.BDD.entity.UserEntity;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Back-end : authentification.
 */
public class AuthController {

    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());

    private final UserDAO userDAO;

    public AuthController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public Optional<UserEntity> connecter(String login, String motDePasse) {
        Optional<UserEntity> result = userDAO.findByLoginAndMotDePasse(login, motDePasse);
        if (result.isPresent()) {
            LOGGER.info("Connexion réussie : " + result.get().getNom());
        } else {
            LOGGER.warning("Échec connexion pour : " + login);
        }
        return result;
    }
}
