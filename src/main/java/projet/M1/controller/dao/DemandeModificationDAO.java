package projet.M1.controller.dao;

import projet.M1.model.planning.Cours;
import projet.M1.model.planning.Horaire;
import projet.M1.model.planning.Salle;
import projet.M1.model.utilisateur_systeme.Professeur;

import java.util.List;

public interface DemandeModificationDAO {

    // Sauvegarde une nouvelle demande
    void sauvegarderDemande(Cours cours, Horaire nouveauCreneau,
                            Salle nouvelleSalle, String raison, Professeur professeur);

    // Annule une demande en attente
    void annulerDemande(Long demandeId);

    // Récupère toutes les demandes en attente
    List<?> findDemandesEnAttente();

    // Approuve une demande
    void approuverDemande(Long demandeId);

    // Rejette une demande
    void rejeterDemande(Long demandeId);
}
