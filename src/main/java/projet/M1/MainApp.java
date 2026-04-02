package projet.M1;

import javafx.application.Application;
import javafx.stage.Stage;
import projet.M1.BDD.DataCache;
import projet.M1.BDD.JPAUtil;
import projet.M1.ui.SceneManager;

/**
 * Point d'entrée JavaFX — c'est ce fichier qui lance l'appli.
 * Pour lancer : mvn javafx:run (ou run directement depuis l'IDE).
 * J'ai pas touché à Main.java, il est toujours là.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Préchargement du pool JPA + données statiques (salles, groupes)
            // en arrière-plan pendant que la fenêtre s'affiche
            Thread preload = new Thread(() -> {
                JPAUtil.getEntityManagerFactory();
                DataCache.getInstance().preload();
            });
            preload.setDaemon(true);
            preload.start();

            SceneManager.getInstance().init(primaryStage);
            SceneManager.getInstance().showLogin();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void stop() {
        JPAUtil.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
