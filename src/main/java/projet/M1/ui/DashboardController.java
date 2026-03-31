package projet.M1.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import projet.M1.BDD.dao.CoursDAO;
import projet.M1.BDD.entity.CoursEntity;
import projet.M1.BDD.entity.Role;
import projet.M1.BDD.entity.UserEntity;
import projet.M1.session.SessionManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Controller du tableau de bord — fichier FXML : dashboard.fxml
 *
 * Affiche : message de bienvenue, stats de la semaine, raccourcis rapides,
 * et la liste des cours du jour en bas.
 *
 * Intégration BDD : les cours viennent désormais de CoursDAO (PostgreSQL).
 * La carte "Demande de modification" est masquée pour les Étudiants et Invités.
 */
public class DashboardController {

    @FXML private Label labelWelcome;
    @FXML private Label labelDate;
    @FXML private Label labelCoursAujourdHui;
    @FXML private Label labelCoursSemaine;
    @FXML private VBox  cardDemandeModif;
    @FXML private VBox  todayCoursContainer;

    private final CoursDAO coursDAO = new CoursDAO();

    @FXML
    public void initialize() {
        UserEntity u = SessionManager.getInstance().getUtilisateurConnecte();
        if (u == null) return;

        labelWelcome.setText("Bonjour, " + u.getPrenom() + " !");
        labelDate.setText(LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH)));

        List<CoursEntity> semaine = loadCoursSemaine(u);
        loadStats(semaine);
        loadTodayCours(semaine);
        applyRoleVisibility(u);
    }

    /**
     * Charge les cours de la semaine en cours selon le rôle :
     * - ETUDIANT   → cours où il est inscrit
     * - PROFESSEUR → cours qu'il enseigne
     * - Autres     → liste vide (pas d'EDT personnel)
     */
    private List<CoursEntity> loadCoursSemaine(UserEntity u) {
        LocalDate lundi = LocalDate.now().with(DayOfWeek.MONDAY);
        try {
            return switch (u.getRole()) {
                case ETUDIANT   -> coursDAO.findByEtudiantAndSemaine(u, lundi);
                case PROFESSEUR -> coursDAO.findByProfesseurAndSemaine(u, lundi);
                default         -> List.of();
            };
        } catch (Exception e) {
            // BDD inaccessible : on affiche zéro cours plutôt que de crasher
            return List.of();
        }
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

    // Petite carte colorée pour un cours (couleur = type CM/TD/TP…)
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

    private void applyRoleVisibility(UserEntity u) {
        boolean canRequest = u.getRole() == Role.PROFESSEUR
                          || u.getRole() == Role.GESTIONNAIRE_PLANNING;
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
