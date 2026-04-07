package projet.M1.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import projet.M1.BDD.dao.UserDAO;
import projet.M1.BDD.entity.UserEntity;
import projet.M1.controller.AuthController;
import projet.M1.session.SessionManager;

import java.util.Optional;

public class LoginController {

    @FXML private TextField     loginField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;
    @FXML private Button        loginButton;

    // Passe par le back-end AuthController, pas directement par UserDAO
    private final AuthController authController = new AuthController(new UserDAO());

    @FXML
    public void initialize() {
        loginField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleLogin());
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
            Optional<UserEntity> result = authController.connecter(login, password);

            if (result.isPresent()) {
                SessionManager.getInstance().setUtilisateurConnecte(result.get());
                SceneManager.getInstance().showMainLayout();
            } else {
                showError("Identifiant ou mot de passe incorrect.");
                passwordField.clear();
                passwordField.requestFocus();
            }

        } catch (Exception e) {
            e.printStackTrace(); // à ajouter temporairement
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
