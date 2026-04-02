package projet.M1.ui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import projet.M1.BDD.dao.SalleDAO;
import projet.M1.BDD.entity.SalleEntity;
import projet.M1.controller.SalleController;

import java.util.List;

/**
 * Controller de la page de sélection de salle — FXML : room-selection.fxml
 *
 * Affiche toutes les salles disponibles en grille, avec filtrage par nom.
 * Passe par SalleController (back-end) — jamais SalleDAO directement.
 *
 * Utilisé pour les US de sélection de salle lors d'une demande de modification.
 */
public class RoomSelectionController {

    @FXML private TextField  searchField;
    @FXML private FlowPane   roomGrid;
    @FXML private Label      labelSelected;
    @FXML private Button     btnConfirm;

    // Passe par le back-end
    private final SalleController salleController = new SalleController(new SalleDAO());

    private List<SalleEntity> allSalles = List.of();
    private SalleEntity salleSelectionnee = null;

    @FXML
    public void initialize() {
        try {
            allSalles = salleController.getAllSalles();
        } catch (Exception e) {
            allSalles = List.of();
        }

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterRooms(newVal));
        displayRooms(allSalles);
    }

    private void filterRooms(String query) {
        if (query == null || query.isBlank()) {
            displayRooms(allSalles);
            return;
        }
        String q = query.toLowerCase();
        displayRooms(allSalles.stream()
                .filter(s -> s.getNom().toLowerCase().contains(q))
                .toList());
    }

    private void displayRooms(List<SalleEntity> salles) {
        roomGrid.getChildren().clear();
        if (salles.isEmpty()) {
            Label empty = new Label("Aucune salle trouvée.");
            empty.getStyleClass().add("text-muted");
            roomGrid.getChildren().add(empty);
            return;
        }
        for (SalleEntity s : salles) {
            roomGrid.getChildren().add(buildRoomCard(s));
        }
    }

    private VBox buildRoomCard(SalleEntity salle) {
        VBox card = new VBox(8);
        card.getStyleClass().add("room-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(200);

        Label nom = new Label(salle.getNom());
        nom.getStyleClass().add("room-card-title");

        Label places = new Label(salle.getPlace() + " places");
        places.getStyleClass().add("room-card-detail");

        card.getChildren().addAll(nom, places);

        card.setOnMouseClicked(e -> selectSalle(salle, card));
        return card;
    }

    private void selectSalle(SalleEntity salle, VBox card) {
        // Désélectionne toutes les cartes
        roomGrid.getChildren().forEach(n -> n.getStyleClass().remove("room-card-selected"));
        // Sélectionne celle cliquée
        card.getStyleClass().add("room-card-selected");
        salleSelectionnee = salle;
        labelSelected.setText("Salle sélectionnée : " + salle.getNom()
                + "  (" + salle.getPlace() + " places)");
        btnConfirm.setDisable(false);
    }

    @FXML
    private void onConfirm() {
        if (salleSelectionnee == null) return;
        // Navigue vers la demande de modification avec la salle sélectionnée
        SceneManager.getInstance().getMainLayoutController().navigateTo(View.MODIFICATION_REQUEST);
    }

    @FXML
    private void onCancel() {
        SceneManager.getInstance().getMainLayoutController().navigateTo(View.TIMETABLE);
    }
}
