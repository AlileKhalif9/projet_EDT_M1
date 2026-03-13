package projet.M1.controller.dao;

import projet.M1.model.utilisateur_systeme.Utilisateur;
import java.util.Optional;

public interface UtilisateurDAO {

    Optional<Utilisateur> findByLogin(String login, String motDePasse);

    boolean loginExiste(String login);                                  // pas de corps
    void sauvegarderUtilisateur(Utilisateur utilisateur);
}