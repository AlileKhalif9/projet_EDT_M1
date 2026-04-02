package projet.M1.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import projet.M1.BDD.dao.SalleDAO;
import projet.M1.BDD.entity.SalleEntity;
import projet.M1.controller.SalleController;

import java.util.List;

/**
 * US18 — Consulter les salles (Gestionnaire).
 */
public class SallesController {

    @FXML private TextField fieldRecherche;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private FlowPane sallesContainer;

    private final SalleController salleController = new SalleController(new SalleDAO());

    private List<SalleEntity> toutesLesSalles = List.of();

    @FXML
    public void initialize() {
        fieldRecherche.textProperty().addListener((obs, oldVal, newVal) -> {
            if (toutesLesSalles.isEmpty()) return;
            String query = newVal.trim().toLowerCase();
            if (query.isEmpty()) {
                afficherSalles(toutesLesSalles);
                return;
            }
            List<SalleEntity> filtres = toutesLesSalles.stream()
                    .filter(s -> s.getNom() != null && s.getNom().toLowerCase().contains(query))
                    .toList();
            afficherSalles(filtres);
        });

        Thread t = new Thread(() -> {
            List<SalleEntity> salles;
            try {
                salles = salleController.getAllSalles();
            } catch (Exception e) {
                salles = List.of();
            }
            final List<SalleEntity> result = salles;
            Platform.runLater(() -> {
                toutesLesSalles = result;
                loadingIndicator.setVisible(false);
                loadingIndicator.setManaged(false);
                sallesContainer.setVisible(true);
                sallesContainer.setManaged(true);
                afficherSalles(toutesLesSalles);
            });
        });
        t.setDaemon(true);
        t.start();
    }

    private void afficherSalles(List<SalleEntity> salles) {
        sallesContainer.getChildren().clear();
        if (salles.isEmpty()) {
            Label vide = new Label("Aucune salle trouvée.");
            vide.getStyleClass().add("text-muted");
            sallesContainer.getChildren().add(vide);
            return;
        }
        for (SalleEntity s : salles) {
            sallesContainer.getChildren().add(buildSalleCard(s));
        }
    }

    private VBox buildSalleCard(SalleEntity s) {
        VBox card = new VBox(12);
        card.getStyleClass().add("salle-card-us18");
        card.setPrefWidth(260);
        card.setOnMouseClicked(e -> ouvrirDetail(s));

        // Nom
        Label nomLabel = new Label(s.getNom() != null ? s.getNom() : "—");
        nomLabel.getStyleClass().add("groupe-card-nom");

        // Capacité
        Label capLabel = new Label("👥 " + s.getPlace() + " places");
        capLabel.getStyleClass().add("groupe-card-stats");

        // Équipements (3 max)
        List<String> materiel = s.getListe_materiel() != null ? s.getListe_materiel() : List.of();
        HBox equipBox = new HBox(6);
        int shown = Math.min(materiel.size(), 3);
        for (int i = 0; i < shown; i++) {
            Label tag = new Label(materiel.get(i));
            tag.getStyleClass().add("salle-tag");
            equipBox.getChildren().add(tag);
        }
        if (materiel.size() > 3) {
            Label more = new Label("+" + (materiel.size() - 3));
            more.getStyleClass().add("salle-tag-more");
            equipBox.getChildren().add(more);
        }

        card.getChildren().addAll(nomLabel, capLabel, equipBox);
        return card;
    }

    private void ouvrirDetail(SalleEntity s) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Salle " + s.getNom());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/projet/M1/css/main.css").toExternalForm());
        dialog.getDialogPane().setPrefWidth(480);

        VBox content = new VBox(16);
        content.getStyleClass().add("page-container");

        Label titre = new Label(s.getNom() != null ? s.getNom() : "—");
        titre.getStyleClass().add("page-title");

        // Stats
        HBox stats = new HBox(16);
        VBox capBox = buildStatBox("Capacité", s.getPlace() + " places");
        HBox.setHgrow(capBox, Priority.ALWAYS);
        capBox.setMaxWidth(Double.MAX_VALUE);
        stats.getChildren().add(capBox);

        // Équipements
        List<String> materiel = s.getListe_materiel() != null ? s.getListe_materiel() : List.of();
        Label equipTitre = new Label("Équipements");
        equipTitre.getStyleClass().add("form-step-title");

        VBox equipList = new VBox(6);
        if (materiel.isEmpty()) {
            Label vide = new Label("Aucun équipement renseigné.");
            vide.getStyleClass().add("text-muted");
            equipList.getChildren().add(vide);
        } else {
            for (String item : materiel) {
                Label l = new Label("• " + item);
                l.getStyleClass().add("cours-block-detail");
                equipList.getChildren().add(l);
            }
        }

        content.getChildren().addAll(titre, stats, equipTitre, equipList);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private VBox buildStatBox(String label, String valeur) {
        VBox box = new VBox(4);
        box.getStyleClass().add("schedule-panel-current");
        Label lbl = new Label(label);
        lbl.getStyleClass().add("schedule-panel-label");
        Label val = new Label(valeur);
        val.getStyleClass().add("schedule-info");
        val.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        box.getChildren().addAll(lbl, val);
        return box;
    }
}
