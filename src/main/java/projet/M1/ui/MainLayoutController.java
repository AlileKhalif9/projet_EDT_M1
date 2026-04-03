package projet.M1.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import projet.M1.BDD.entity.Role;
import projet.M1.BDD.entity.UserEntity;
import projet.M1.session.SessionManager;

import java.io.IOException;

/**
 * Controller du layout principal (sidebar + zone centrale).
 *
 * Visibilité des boutons selon le rôle :
 *   ETUDIANT              → Dashboard, EDT
 *   PROFESSEUR            → Dashboard, EDT, Demande modification, Salles
 *   INVITE                → Dashboard, EDT, Salles
 *   GESTIONNAIRE_PLANNING → Dashboard, EDT, Demande modification, Groupes, Salles
 */
public class MainLayoutController {

    @FXML private VBox      sidebarNav;
    @FXML private Button    btnDashboard;
    @FXML private Button    btnTimetable;
    @FXML private Button    btnRoomSelection;
    @FXML private Button    btnGroupes;
    @FXML private Button    btnSalles;
    @FXML private Circle    avatarCircle;
    @FXML private Label     labelAvatarInitials;
    @FXML private Label     labelUserName;
    @FXML private Label     labelUserRole;
    @FXML private Button    btnLogout;
    @FXML private StackPane contentArea;

    private View activeView;

    @FXML
    public void initialize() {
        SceneManager.getInstance().setMainLayoutController(this);
        populateUserInfo();
        applyRoleVisibility();
    }

    private void populateUserInfo() {
        UserEntity u = SessionManager.getInstance().getUtilisateurConnecte();
        if (u == null) return;

        labelAvatarInitials.setText(initiales(u));
        labelUserName.setText(u.getPrenom() + " " + u.getNom());
        labelUserRole.setText(roleLabel(u));
        labelUserRole.getStyleClass().add(roleStyleClass(u));
    }

    private void applyRoleVisibility() {
        UserEntity u = SessionManager.getInstance().getUtilisateurConnecte();
        if (u == null) return;

        // Demande de modification : professeur et gestionnaire uniquement (pas l'invité)
        boolean canRequest = u.getRole() == Role.PROFESSEUR
                || u.getRole() == Role.GESTIONNAIRE_PLANNING;
        btnRoomSelection.setVisible(canRequest);
        btnRoomSelection.setManaged(canRequest);

        // Groupes : gestionnaire uniquement
        boolean isGestionnaire = u.getRole() == Role.GESTIONNAIRE_PLANNING;
        btnGroupes.setVisible(isGestionnaire);
        btnGroupes.setManaged(isGestionnaire);

        // Salles : gestionnaire, professeur et invité
        boolean canSeeSalles = u.getRole() == Role.GESTIONNAIRE_PLANNING
                || u.getRole() == Role.PROFESSEUR
                || u.getRole() == Role.INVITE;
        btnSalles.setVisible(canSeeSalles);
        btnSalles.setManaged(canSeeSalles);
    }

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
    @FXML private void onRoomSelection() { navigateTo(View.MODIFICATION_REQUEST); }
    @FXML private void onGroupes()       { navigateTo(View.GROUPES); }
    @FXML private void onSalles()        { navigateTo(View.SALLES); }

    @FXML
    private void onLogout() {
        SessionManager.getInstance().deconnecter();
        SceneManager.getInstance().showLogin();
    }

    private void updateNavHighlight(View view) {
        btnDashboard.getStyleClass().remove("sidebar-nav-item-active");
        btnTimetable.getStyleClass().remove("sidebar-nav-item-active");
        btnRoomSelection.getStyleClass().remove("sidebar-nav-item-active");
        btnGroupes.getStyleClass().remove("sidebar-nav-item-active");
        btnSalles.getStyleClass().remove("sidebar-nav-item-active");
        switch (view) {
            case DASHBOARD            -> btnDashboard.getStyleClass().add("sidebar-nav-item-active");
            case TIMETABLE            -> btnTimetable.getStyleClass().add("sidebar-nav-item-active");
            case MODIFICATION_REQUEST -> btnRoomSelection.getStyleClass().add("sidebar-nav-item-active");
            case GROUPES              -> btnGroupes.getStyleClass().add("sidebar-nav-item-active");
            case SALLES               -> btnSalles.getStyleClass().add("sidebar-nav-item-active");
        }
    }

    private String initiales(UserEntity u) {
        String p = u.getPrenom() != null ? u.getPrenom() : "";
        String n = u.getNom()    != null ? u.getNom()    : "";
        return (p.isEmpty() ? "" : String.valueOf(p.charAt(0)).toUpperCase())
                + (n.isEmpty() ? "" : String.valueOf(n.charAt(0)).toUpperCase());
    }

    private String roleLabel(UserEntity u) {
        return switch (u.getRole()) {
            case ETUDIANT              -> "Étudiant";
            case PROFESSEUR            -> "Professeur";
            case GESTIONNAIRE_PLANNING -> "Gestionnaire";
            case INVITE                -> "Invité";
        };
    }

    private String roleStyleClass(UserEntity u) {
        return switch (u.getRole()) {
            case ETUDIANT              -> "badge-role-etudiant";
            case PROFESSEUR            -> "badge-role-prof";
            case GESTIONNAIRE_PLANNING -> "badge-role-gest";
            case INVITE                -> "badge-role-invite";
        };
    }
}