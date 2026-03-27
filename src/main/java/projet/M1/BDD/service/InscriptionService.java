package projet.M1.BDD.service;

import projet.M1.BDD.entity.UserEntity;
import projet.M1.BDD.repository.UserRepository;

import java.util.Random;

public class InscriptionService {

    private final UserRepository userRepository;
    private final Random random = new Random();

    public InscriptionService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Generation login mdp, pour un nouvel utilisateur
    public void inscrireUtilisateur(UserEntity utilisateur) {
        boolean aLogin = utilisateur.getLogin() != null && !utilisateur.getLogin().isEmpty();
        boolean aMotDePasse = utilisateur.getMotDePasse() != null && !utilisateur.getMotDePasse().isEmpty();

        if (aLogin && aMotDePasse) {
            throw new IllegalStateException("L'utilisateur a déjà un login et un mot de passe.");
        }

        if (aLogin || aMotDePasse) {
            throw new IllegalStateException("État incohérent : login ou mot de passe manquant.");
        }

        String login = genererLoginUnique(utilisateur);
        utilisateur.setLogin(login);

        String motDePasse = genererMotDePasse();
        utilisateur.setMotDePasse(motDePasse);

        userRepository.save(utilisateur);
    }

    private String genererLoginUnique(UserEntity utilisateur) {
        String prenom = utilisateur.getPrenom().toLowerCase();
        String nom = utilisateur.getNom().toLowerCase();
        String base = String.valueOf(prenom.charAt(0)) + nom;

        String login;
        do {
            int deuxChiffres = random.nextInt(90) + 10;
            login = base + deuxChiffres;
        } while (userRepository.existsByLogin(login));

        return login;
    }

    private String genererMotDePasse() {
        // à définir plus tard
        return null;
    }
}