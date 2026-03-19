package projet.M1;

import javafx.application.Application;
import javafx.stage.Stage;
import projet.M1.ui.SceneManager;

/**
 * Point d'entrée JavaFX — c'est ce fichier qui lance l'appli.
 * Pour lancer : mvn javafx:run (ou run directement depuis l'IDE).
 * J'ai pas touché à Main.java, il est toujours là.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        SceneManager.getInstance().init(primaryStage);
        // On commence toujours par la page de connexion
        SceneManager.getInstance().showLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
