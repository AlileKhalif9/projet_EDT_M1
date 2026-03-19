package projet.M1.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import projet.M1.model.utilisateur_systeme.Etudiant;
import projet.M1.model.utilisateur_systeme.Gestionnaire_Planning;
import projet.M1.model.utilisateur_systeme.Professeur;
import projet.M1.model.utilisateur_systeme.Utilisateur;
import projet.M1.session.SessionManager;

import java.io.IOException;

/**
 * Controller du layout principal — fichier FXML : main-layout.fxml
 *
 * C'est lui qui gère la sidebar (navigation + profil) et la zone centrale
 * où les pages s'affichent. La sidebar reste fixe, seul le centre change.
 *
 * Pour naviguer depuis un autre controller :
 *   SceneManager.getInstance().getMainLayoutController().navigateTo(View.TIMETABLE)
 *
 * Le bouton "Demande de modification" est masqué automatiquement pour les Étudiants.
 */
public class MainLayoutController {

    @FXML private VBox      sidebarNav;
    @FXML private Button    btnDashboard;
    @FXML private Button    btnTimetable;
    @FXML private Button    btnRoomSelection;
    @FXML private Circle    avatarCircle;
    @FXML private Label     labelAvatarInitials;
    @FXML private Label     labelUserName;
    @FXML private Label     labelUserRole;
    @FXML private Button    btnLogout;
    @FXML private StackPane contentArea; // zone centrale où les pages se chargent

    private View activeView;

    @FXML
    public void initialize() {
        SceneManager.getInstance().setMainLayoutController(this);
        populateUserInfo();
        applyRoleVisibility();
    }

    private void populateUserInfo() {
        Utilisateur u = SessionManager.getInstance().getUtilisateurConnecte();
        if (u == null) return;
        labelAvatarInitials.setText(initiales(u));
        labelUserName.setText(u.getPrenom() + " " + u.getNom());
        labelUserRole.setText(roleLabel(u));
        labelUserRole.getStyleClass().add(roleStyleClass(u));
    }

    private void applyRoleVisibility() {
        Utilisateur u = SessionManager.getInstance().getUtilisateurConnecte();
        // Demande de modif accessible seulement pour les profs et gestionnaires
        boolean canRequest = (u instanceof Professeur) || (u instanceof Gestionnaire_Planning);
        btnRoomSelection.setVisible(canRequest);
        btnRoomSelection.setManaged(canRequest);
    }

    // Charge une page dans la zone centrale (remplace le contenu précédent)
    public void navigateTo(View view) {
        if (view == activeView) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/projet/M1/fxml/" + view.getFxmlName() + ".fxml"));
            Node content = loader.load();
            contentArea.getChildren().setAll(content);
            activeView = view;
            updateNavHighlight(view);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger la vue : " + view, e);
        }
    }

    @FXML private void onDashboard()     { navigateTo(View.DASHBOARD); }
    @FXML private void onTimetable()     { navigateTo(View.TIMETABLE); }
    @FXML private void onRoomSelection() { navigateTo(View.ROOM_SELECTION); }

    @FXML
    private void onLogout() {
        SessionManager.getInstance().deconnecter();
        SceneManager.getInstance().showLogin();
    }

    private void updateNavHighlight(View view) {
        btnDashboard.getStyleClass().remove("sidebar-nav-item-active");
        btnTimetable.getStyleClass().remove("sidebar-nav-item-active");
        btnRoomSelection.getStyleClass().remove("sidebar-nav-item-active");
        switch (view) {
            case DASHBOARD      -> btnDashboard.getStyleClass().add("sidebar-nav-item-active");
            case TIMETABLE      -> btnTimetable.getStyleClass().add("sidebar-nav-item-active");
            case ROOM_SELECTION -> btnRoomSelection.getStyleClass().add("sidebar-nav-item-active");
        }
    }

    // "Jean Martin" → "JM"
    private String initiales(Utilisateur u) {
        return (u.getPrenom().isEmpty() ? "" : String.valueOf(u.getPrenom().charAt(0)).toUpperCase())
             + (u.getNom().isEmpty()    ? "" : String.valueOf(u.getNom().charAt(0)).toUpperCase());
    }

    private String roleLabel(Utilisateur u) {
        if (u instanceof Etudiant)              return "Étudiant";
        if (u instanceof Professeur)            return "Professeur";
        if (u instanceof Gestionnaire_Planning) return "Gestionnaire";
        return "Invité";
    }

    private String roleStyleClass(Utilisateur u) {
        if (u instanceof Etudiant)              return "badge-role-etudiant";
        if (u instanceof Professeur)            return "badge-role-prof";
        if (u instanceof Gestionnaire_Planning) return "badge-role-gest";
        return "badge-role-invite";
    }
}
