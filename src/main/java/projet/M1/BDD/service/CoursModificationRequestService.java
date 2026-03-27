package projet.M1.BDD.service;

import projet.M1.BDD.entity.StatutDemande;
import projet.M1.BDD.entity.UserEntity;
import projet.M1.BDD.repository.CoursModificationRequestRepository;
import projet.M1.BDD.entity.CoursModificationRequestEntity;

import java.util.List;

public class CoursModificationRequestService {

    private final CoursModificationRequestRepository requestRepository;

    public CoursModificationRequestService(CoursModificationRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    // Demande du professeur
    public CoursModificationRequestEntity soumettreDemande(CoursModificationRequestEntity demande) {
        demande.setStatut(StatutDemande.PENDING);
        return requestRepository.save(demande);
    }

    // Toutes les demandes d'un professeur
    public List<CoursModificationRequestEntity> getDemandesByProfesseur(UserEntity professeur) {
        return requestRepository.findByDemandeur(professeur);
    }

    // Toutes les demandes en attente
    public List<CoursModificationRequestEntity> getDemandesEnAttente() {
        return requestRepository.findByStatut(StatutDemande.PENDING);
    }

    // Approuver une demande, gestionnaire
    public CoursModificationRequestEntity approuver(Long id) {
        CoursModificationRequestEntity demande = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable : " + id));
        demande.setStatut(StatutDemande.ACCEPTED);
        return requestRepository.save(demande);
    }

    // Rejeté une demande, gestionnaire
    public CoursModificationRequestEntity rejeter(Long id) {
        CoursModificationRequestEntity demande = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable : " + id));
        demande.setStatut(StatutDemande.REFUSED);
        return requestRepository.save(demande);
    }

    // Annuler demande en cours, professeur
    public CoursModificationRequestEntity annuler(Long id) {
        CoursModificationRequestEntity demande = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable : " + id));

        if (demande.getStatut() != StatutDemande.PENDING) {
            throw new IllegalStateException("Impossible d'annuler une demande déjà traitée.");
        }

        requestRepository.delete(demande);
        return demande;
    }
}
