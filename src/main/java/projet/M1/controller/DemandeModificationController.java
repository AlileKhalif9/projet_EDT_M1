package projet.M1.controller;

import projet.M1.BDD.dao.CoursDAO;
import projet.M1.BDD.dao.DemandeDAO;
import projet.M1.BDD.entity.*;

import java.util.List;
import java.util.logging.Logger;

/**
 * Back-end : demandes de modification.
 */
public class DemandeModificationController {

    private static final Logger LOGGER =
            Logger.getLogger(DemandeModificationController.class.getName());

    private final DemandeDAO demandeDAO;
    private final CoursDAO   coursDAO;

    public DemandeModificationController(DemandeDAO demandeDAO, CoursDAO coursDAO) {
        this.demandeDAO = demandeDAO;
        this.coursDAO   = coursDAO;
    }

    /** Soumet une demande de modification. */
    public void soumettreDemande(CoursEntity cours, HoraireEntity nouveauCreneau,
                                 SalleEntity nouvelleSalle, String raison, UserEntity professeur) {
        CoursModificationRequestEntity demande = new CoursModificationRequestEntity();
        demande.setCours(cours);
        demande.setNouveauCreneau(nouveauCreneau);
        demande.setNouvelleSalle(nouvelleSalle);
        demande.setRaison(raison);
        demande.setDemandeur(professeur);
        demande.setStatut(StatutDemande.PENDING);
        demandeDAO.save(demande);
        LOGGER.info("Demande soumise par : " + professeur.getNom());
    }

    /** Annule une demande encore en attente (rôle PROFESSEUR). */
    public void annulerDemande(Long demandeId) {
        demandeDAO.delete(demandeId);
        LOGGER.info("Demande annulée : " + demandeId);
    }

    /** Toutes les demandes PENDING (rôle GESTIONNAIRE). */
    public List<CoursModificationRequestEntity> getDemandesEnAttente() {
        return demandeDAO.findByStatut(StatutDemande.PENDING);
    }

    /** Toutes les demandes d'un professeur (historique). */
    public List<CoursModificationRequestEntity> getDemandesByProfesseur(UserEntity professeur) {
        return demandeDAO.findByDemandeur(professeur);
    }

    /**
     * Approuve une demande (rôle GESTIONNAIRE) :
     * déplace d'abord le cours en BDD, puis passe le statut à ACCEPTED.
     */
    public void approuverDemande(Long demandeId) {
        demandeDAO.findById(demandeId).ifPresent(d -> {
            if (d.getCours() != null
                    && (d.getNouveauCreneau() != null || d.getNouvelleSalle() != null)) {
                coursDAO.applyModification(
                        d.getCours().getId(),
                        d.getNouveauCreneau(),
                        d.getNouvelleSalle());
            }
        });
        demandeDAO.updateStatut(demandeId, StatutDemande.ACCEPTED);
        LOGGER.info("Demande approuvée : " + demandeId);
    }

    /** Rejette une demande (rôle GESTIONNAIRE). */
    public void rejeterDemande(Long demandeId) {
        demandeDAO.updateStatut(demandeId, StatutDemande.REFUSED);
        LOGGER.info("Demande rejetée : " + demandeId);
    }
}
