package projet.M1.controller;

import projet.M1.BDD.dao.UserDAO;
import projet.M1.BDD.entity.Role;
import projet.M1.BDD.entity.UserEntity;

import java.util.logging.Logger;

/**
 * Back-end : inscription d'un nouvel utilisateur.
 *
 * Vérifie que le login n'existe pas déjà, crée le UserEntity
 * et le persiste via UserDAO.
 *
 * Le front appellera inscrireUtilisateur() — jamais UserDAO directement.
 */
public class Service_Inscription {

    private static final Logger LOGGER = Logger.getLogger(Service_Inscription.class.getName());

    private final UserDAO userDAO;

    public Service_Inscription(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Inscrit un nouvel utilisateur en base.
     * Le login doit être unique — lève une IllegalArgumentException sinon.
     */
    public void inscrireUtilisateur(String nom, String prenom, String login,
                                    String motDePasse, Role role) {
        if (userDAO.loginExiste(login)) {
            throw new IllegalArgumentException("Le login \"" + login + "\" est déjà utilisé.");
        }

        UserEntity user = new UserEntity();
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setLogin(login);
        user.setMotDePasse(motDePasse);
        user.setRole(role);

        userDAO.save(user);
        LOGGER.info("Utilisateur inscrit : " + login + " (" + role + ")");
    }
}
