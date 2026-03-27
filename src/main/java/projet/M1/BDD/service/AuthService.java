package projet.M1.BDD.service;

import projet.M1.BDD.entity.UserEntity;
import projet.M1.BDD.repository.UserRepository;

import java.util.Optional;

public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Retourner l'utilisateur connecté
    public Optional<UserEntity> connecter(String login, String motDePasse) {
        return userRepository.findByLoginAndMotDePasse(login, motDePasse);
    }
}