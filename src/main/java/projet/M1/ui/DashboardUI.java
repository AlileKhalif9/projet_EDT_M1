package projet.M1.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import projet.M1.BDD.dao.CoursDAO;
import projet.M1.BDD.entity.CoursEntity;
import projet.M1.BDD.entity.Role;
import projet.M1.BDD.entity.UserEntity;
import projet.M1.controller.EmploiDuTempsController;
import projet.M1.session.SessionManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Controller du tableau de bord.
 * Passe par EmploiDuTempsController (back-end) : jamais CoursDAO directement.
 */
public class DashboardUI {

    @FXML private Label labelWelcome;
    @FXML private Label labelDate;
    @FXML private Label labelCoursAujourdHui;
    @FXML private Label labelCoursSemaine;
    @FXML private VBox cardDemandeModif;
    @FXML private VBox cardNotes;
    @FXML private VBox todayCoursContainer;
    @FXML private ProgressIndicator loadingIndicator;

    // Passe par le back-end, pas directement par le DAO
    private final EmploiDuTempsController edtController =
            new EmploiDuTempsController(new CoursDAO());

    @FXML
    public void initialize() {
        UserEntity u = SessionManager.getInstance().getUtilisateurConnecte();
        if (u == null) return;

        labelWelcome.setText("Bonjour, " + u.getPrenom() + " !");
        labelDate.setText(LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH)));

        applyRoleVisibility(u);

        // Requête BDD en arrière-plan
        LocalDate lundi = LocalDate.now().with(DayOfWeek.MONDAY);
        Thread t = new Thread(() -> {
            List<CoursEntity> semaine;
            try {
                semaine = edtController.getEmploiDuTempsConnecte(u, lundi);
            } catch (Exception e) {
                semaine = List.of();
            }
            final List<CoursEntity> result = semaine;
            Platform.runLater(() -> {
                loadStats(result);
                loadTodayCours(result);
                // Cache le spinner, affiche le contenu
                loadingIndicator.setVisible(false);
                loadingIndicator.setManaged(false);
                todayCoursContainer.setVisible(true);
                todayCoursContainer.setManaged(true);
            });
        });
        t.setDaemon(true);
        t.start();
    }

    private void loadStats(List<CoursEntity> semaine) {
        LocalDate today = LocalDate.now();
        long todayCount = semaine.stream()
                .filter(c -> c.getHoraire() != null && today.equals(c.getHoraire().getJour()))
                .count();
        labelCoursAujourdHui.setText(String.valueOf(todayCount));
        labelCoursSemaine.setText(String.valueOf(semaine.size()));
    }

    private void loadTodayCours(List<CoursEntity> semaine) {
        LocalDate today = LocalDate.now();
        List<CoursDisplay> todayCours = semaine.stream()
                .filter(c -> c.getHoraire() != null && today.equals(c.getHoraire().getJour()))
                .map(CoursDisplay::fromEntity)
                .filter(Objects::nonNull)
                .toList();

        todayCoursContainer.getChildren().clear();

        if (todayCours.isEmpty()) {
            Label empty = new Label("Aucun cours aujourd'hui.");
            empty.getStyleClass().add("text-muted");
            todayCoursContainer.getChildren().add(empty);
            return;
        }

        for (CoursDisplay c : todayCours) {
            todayCoursContainer.getChildren().add(buildCoursMini(c));
        }
    }

    private VBox buildCoursMini(CoursDisplay c) {
        VBox card = new VBox(4);
        String type = c.typeCours() != null ? c.typeCours().getLibelle() : "?";
        String heure = c.heureDebut() + " – " + c.heureFin();

        Label titre = new Label(c.nom() != null ? c.nom() : "Cours");
        titre.getStyleClass().add("cours-mini-title");

        Label details = new Label("[" + type + "]  " + heure
                + "  •  " + (c.nomSalle() != null ? c.nomSalle() : "?"));
        details.getStyleClass().add("cours-mini-details");

        card.getChildren().addAll(titre, details);

        if (c.typeCours() != null) {
            card.setStyle(
                    "-fx-border-color: " + c.typeCours().getCouleurBordure()
                            + "; -fx-border-width: 0 0 0 4; -fx-border-radius: 0;"
                            + "-fx-background-color: " + c.typeCours().getCouleurFond()
                            + "; -fx-background-radius: 6; -fx-padding: 10 12 10 12;");
        }
        return card;
    }

    private void applyRoleVisibility(UserEntity u) {
        boolean canRequest = u.getRole() == Role.PROFESSEUR
                || u.getRole() == Role.GESTIONNAIRE_PLANNING;
        cardDemandeModif.setVisible(canRequest);
        cardDemandeModif.setManaged(canRequest);

        // Notes : professeur et étudiant uniquement
        boolean canSeeNotes = u.getRole() == Role.PROFESSEUR
                || u.getRole() == Role.ETUDIANT;
        cardNotes.setVisible(canSeeNotes);
        cardNotes.setManaged(canSeeNotes);
    }

    @FXML private void goToTimetable() {
        SceneManager.getInstance().getMainLayoutController().navigateTo(View.TIMETABLE);
    }

    @FXML private void goToRoomSelection() {
        SceneManager.getInstance().getMainLayoutController().navigateTo(View.MODIFICATION_REQUEST);
    }

    @FXML private void goToNotes() {
        SceneManager.getInstance().getMainLayoutController().navigateTo(View.NOTES);
    }
}