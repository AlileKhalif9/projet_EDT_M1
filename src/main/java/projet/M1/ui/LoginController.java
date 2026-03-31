package projet.M1.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import projet.M1.BDD.dao.UserDAO;
import projet.M1.BDD.entity.UserEntity;
import projet.M1.session.SessionManager;

import java.util.Optional;

/**
 * Controller de la page de connexion.
 *
 * Intégration BDD : utilise UserDAO pour authentifier l'utilisateur
 * contre la base PostgreSQL (plus de MockUtilisateurDAO).
 *
 * En cas d'échec de connexion à la BDD (ex: PostgreSQL non démarré),
 * une alerte explicite s'affiche plutôt qu'un crash silencieux.
 */
public class LoginController {

    @FXML private TextField     loginField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;
    @FXML private Button        loginButton;

    // DAO branché sur la vraie BDD PostgreSQL via JPAUtil
    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        loginField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleLogin()); // Entrée = connexion
        hideError();
    }

    @FXML
    private void handleLogin() {
        String login    = loginField.getText().trim();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        try {
            Optional<UserEntity> result = userDAO.findByLoginAndMotDePasse(login, password);

            if (result.isPresent()) {
                // Connexion réussie : on stocke le UserEntity dans la session
                SessionManager.getInstance().setUtilisateurConnecte(result.get());
                SceneManager.getInstance().showMainLayout();
            } else {
                showError("Identifiant ou mot de passe incorrect.");
                passwordField.clear();
                passwordField.requestFocus();
            }

        } catch (Exception e) {
            // La BDD est inaccessible (PostgreSQL non démarré, mauvaise config…)
            showError("Impossible de joindre la base de données.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
