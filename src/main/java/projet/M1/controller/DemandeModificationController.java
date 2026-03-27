package projet.M1.controller;

import projet.M1.controller.dao.DemandeModificationDAO;
import projet.M1.model.planning.Cours;
import projet.M1.model.planning.Horaire;
import projet.M1.model.planning.Salle;
import projet.M1.model.utilisateur_systeme.Professeur;
import projet.M1.model.utilisateur_systeme.Utilisateur;
import projet.M1.session.SessionManager;

import java.util.List;
import java.util.logging.Logger;

public class DemandeModificationController {

    private static final Logger LOGGER =
            Logger.getLogger(DemandeModificationController.class.getName());

    private final DemandeModificationDAO demandeDAO;

    public DemandeModificationController(DemandeModificationDAO demandeDAO) {
        this.demandeDAO = demandeDAO;
    }

    // Soumet une demande de modification, professeur
    public void soumettreDemande(Cours cours, Horaire nouveauCreneau,
                                 Salle nouvelleSalle, String raison) {
        Utilisateur u = SessionManager.getInstance().getUtilisateurConnecte();

        if (!(u instanceof Professeur)) {
            throw new IllegalStateException("Seul un professeur peut soumettre une demande.");
        }

        demandeDAO.sauvegarderDemande(cours, nouveauCreneau, nouvelleSalle, raison, (Professeur) u);
        LOGGER.info("Demande soumise par : " + u.getNom());
    }

    // Annule une demande en attente, professeur
    public void annulerDemande(Long demandeId) {
        demandeDAO.annulerDemande(demandeId);
        LOGGER.info("Demande annulée : " + demandeId);
    }

    // Récupère les demandes en attente, gestionnaire
    public List<?> getDemandesEnAttente() {
        return demandeDAO.findDemandesEnAttente();
    }

    // Approuve une demande, gestionnaire
    public void approuverDemande(Long demandeId) {
        demandeDAO.approuverDemande(demandeId);
        LOGGER.info("Demande approuvée : " + demandeId);
    }

    // Rejette une demande, gestionnaire
    public void rejeterDemande(Long demandeId) {
        demandeDAO.rejeterDemande(demandeId);
        LOGGER.info("Demande rejetée : " + demandeId);
    }
}