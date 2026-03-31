package projet.M1.session;

import projet.M1.BDD.entity.UserEntity;

/**
 * Garde en mémoire l'utilisateur connecté pendant toute la session.
 *
 * Après le login réussi, on stocke ici le UserEntity retourné par la BDD.
 * N'importe quel controller peut récupérer l'utilisateur connecté via :
 *   SessionManager.getInstance().getUtilisateurConnecte()
 *
 * À la déconnexion, on remet à null (méthode deconnecter()).
 *
 * Migration : on stocke maintenant UserEntity (BDD) au lieu du modèle Utilisateur.
 */
public class SessionManager {

    private static SessionManager instance;
    private UserEntity utilisateurConnecte;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public UserEntity getUtilisateurConnecte() { return utilisateurConnecte; }

    public void setUtilisateurConnecte(UserEntity utilisateur) {
        this.utilisateurConnecte = utilisateur;
    }

    public void deconnecter() { this.utilisateurConnecte = null; }

    public boolean estConnecte() { return utilisateurConnecte != null; }
}
