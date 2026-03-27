package projet.M1.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import projet.M1.controller.AuthController;
import projet.M1.controller.dao.MockUtilisateurDAO;
import projet.M1.model.utilisateur_systeme.Utilisateur;
import projet.M1.session.SessionManager;

import java.util.Optional;

/**
 * Quand la BDD sera prête, dans ce fichier on remplace juste :
 *   new AuthController(new MockUtilisateurDAO())
 * par :
 *   new AuthController(new VotreVraiDAO())
 */
public class LoginController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final AuthController authController =
            new AuthController(new MockUtilisateurDAO());

    @FXML
    public void initialize() {
        loginField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleLogin()); // Entrée = connexion
        hideError();
    }

    @FXML
    private void handleLogin() {
        String login = loginField.getText().trim();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        Optional<Utilisateur> result = authController.connecter(login, password);

        if (result.isPresent()) {
            // Connexion réussie : on mémorise l'utilisateur et on ouvre l'appli
            SessionManager.getInstance().setUtilisateurConnecte(result.get());
            SceneManager.getInstance().showMainLayout();
        } else {
            showError("Identifiant ou mot de passe incorrect.");
            passwordField.clear();
            passwordField.requestFocus();
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
