package projet.M1.session;

import projet.M1.model.utilisateur_systeme.Utilisateur;

/**
 * Garde en mémoire qui est connecté pendant toute la session.
 *
 * Après le login, on stocke l'utilisateur ici.
 * N'importe quel contrôleur peut ensuite récupérer l'utilisateur connecté :
 *   SessionManager.getInstance().getUtilisateurConnecte()
 *
 * À la déconnexion, on remet à null.
 */
public class SessionManager {

    private static SessionManager instance;
    private Utilisateur utilisateurConnecte;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public Utilisateur getUtilisateurConnecte() { return utilisateurConnecte; }

    public void setUtilisateurConnecte(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
    }

    public void deconnecter() { this.utilisateurConnecte = null; }

    public boolean estConnecte() { return utilisateurConnecte != null; }
}
