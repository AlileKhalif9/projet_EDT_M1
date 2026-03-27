package projet.M1.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import projet.M1.model.utilisateur_systeme.Gestionnaire_Planning;
import projet.M1.model.utilisateur_systeme.Professeur;
import projet.M1.model.utilisateur_systeme.Utilisateur;
import projet.M1.service.MockDataService;
import projet.M1.session.SessionManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Affiche : message de bienvenue, stats de la semaine, raccourcis rapides,
 * et la liste des cours du jour en bas.
 */
public class DashboardController {

    @FXML private Label labelWelcome;
    @FXML private Label labelDate;
    @FXML private Label labelCoursAujourdHui;
    @FXML private Label labelCoursSemaine;
    @FXML private VBox cardDemandeModif;
    @FXML private VBox todayCoursContainer;

    @FXML
    public void initialize() {
        Utilisateur u = SessionManager.getInstance().getUtilisateurConnecte();
        if (u == null) return;

        labelWelcome.setText("Bonjour, " + u.getPrenom() + " \uD83D\uDC4B");
        labelDate.setText(LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH)));

        loadStats();
        loadTodayCours();
        applyRoleVisibility(u);
    }

    private void loadStats() {
        LocalDate lundi = LocalDate.now().with(DayOfWeek.MONDAY);
        List<CoursDisplay> semaine = MockDataService.getInstance().getCoursEtudiant(lundi);
        LocalDate today = LocalDate.now();

        long aujourd = semaine.stream().filter(c -> c.jour().equals(today)).count();
        labelCoursAujourdHui.setText(String.valueOf(aujourd));
        labelCoursSemaine.setText(String.valueOf(semaine.size()));
    }

    private void loadTodayCours() {
        LocalDate lundi = LocalDate.now().with(DayOfWeek.MONDAY);
        List<CoursDisplay> today = MockDataService.getInstance().getCoursEtudiant(lundi)
                .stream().filter(c -> c.jour().equals(LocalDate.now())).toList();

        todayCoursContainer.getChildren().clear();

        if (today.isEmpty()) {
            Label empty = new Label("Aucun cours aujourd'hui.");
            empty.getStyleClass().add("text-muted");
            todayCoursContainer.getChildren().add(empty);
            return;
        }

        for (CoursDisplay c : today) {
            todayCoursContainer.getChildren().add(buildCoursMini(c));
        }
    }

    // Petite carte colorée pour un cours
    private VBox buildCoursMini(CoursDisplay c) {
        VBox card = new VBox(4);
        String type  = c.typeCours() != null ? c.typeCours().getLibelle() : "?";
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

    private void applyRoleVisibility(Utilisateur u) {
        boolean canRequest = (u instanceof Professeur) || (u instanceof Gestionnaire_Planning);
        cardDemandeModif.setVisible(canRequest);
        cardDemandeModif.setManaged(canRequest);
    }

    @FXML private void goToTimetable() {
        SceneManager.getInstance().getMainLayoutController().navigateTo(View.TIMETABLE);
    }

    @FXML private void goToRoomSelection() {
        SceneManager.getInstance().getMainLayoutController().navigateTo(View.MODIFICATION_REQUEST);
    }
}
