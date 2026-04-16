package projet.M1.ui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Gère la navigation entre les pages. C'est le seul endroit où on change de scène.
 */
public class SceneManager {

    private static SceneManager instance;
    private Stage primaryStage;
    private MainLayoutUI mainLayoutController;

    private SceneManager() {}

    public static SceneManager getInstance() {
        if (instance == null) instance = new SceneManager();
        return instance;
    }

    // Appelé une seule fois au démarrage dans MainApp
    public void init(Stage stage) {
        this.primaryStage = stage;
    }

    // Affiche la page de connexion (au démarrage ou après déconnexion)
    public void showLogin() {
        loadRootScene("login");
    }

    // Affiche le layout principal après une connexion réussie
    public void showMainLayout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/projet/M1/fxml/main-layout.fxml"));
            Parent root = loader.load();
            mainLayoutController = loader.getController();

            Scene scene = buildScene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Hyperplanning");
            primaryStage.setMinWidth(1100);
            primaryStage.setMinHeight(700);
            primaryStage.setResizable(true);
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            primaryStage.setX(bounds.getMinX());
            primaryStage.setY(bounds.getMinY());
            primaryStage.setWidth(bounds.getWidth());
            primaryStage.setHeight(bounds.getHeight());
            primaryStage.show();

            mainLayoutController.navigateTo(View.DASHBOARD);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger le layout principal.", e);
        }
    }

    public MainLayoutUI getMainLayoutController() { return mainLayoutController; }

    void setMainLayoutController(MainLayoutUI c) { this.mainLayoutController = c; }

    public Stage getPrimaryStage() { return primaryStage; }

    private void loadRootScene(String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/projet/M1/fxml/" + fxmlName + ".fxml"));
            Parent root = loader.load();
            Scene scene = buildScene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Hyperplanning – Connexion");
            primaryStage.setResizable(false);
            primaryStage.show();
            // Fix rendu blanc macOS : force un repaint après affichage
            Platform.runLater(() -> {
                primaryStage.setOpacity(0.99);
                primaryStage.setOpacity(1.0);
            });
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger la scène : " + fxmlName, e);
        }
    }

    private Scene buildScene(Parent root) {
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                getClass().getResource("/projet/M1/css/main.css").toExternalForm());
        return scene;
    }
}
