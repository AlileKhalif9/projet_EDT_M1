package projet.M1.ui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import projet.M1.model.planning.Salle;
import projet.M1.service.MockDataService;

import java.util.List;

/**
 * Controller de la page sélection de salle (US6) — fichier FXML : room-selection.fxml
 *
 * C'est la page où un prof choisit une salle pour sa demande de modif d'EDT.
 * La suite du flux (US7 créneau, US8 validation) sera faite au Sprint 2.
 *
 * Ce que ça fait :
 *   - Affiche les 6 salles fictives sous forme de cartes (MockDataService)
 *   - Recherche en temps réel par nom ou équipement
 *   - Clic sur une carte → elle se surligne en bleu + bouton Confirmer s'active
 *   - Confirmer → dialogue provisoire (à remplacer par la nav vers US7 au Sprint 2)
 *
 * La salle choisie est stockée dans this.selectedSalle — à passer au prochain controller.
 * Les données viennent de MockDataService, la classe Salle est celle du projet (non modifiée).
 */
public class RoomSelectionController {

    @FXML private TextField searchField;       // Barre de recherche
    @FXML private FlowPane  roomGrid;          // Grille de cartes de salles
    @FXML private Button    btnConfirm;        // Bouton "Confirmer" (désactivé au départ)
    @FXML private Label     labelSelected;     // Affiche le nom de la salle choisie

    private List<Salle> allSalles;     // Toutes les salles (chargées au démarrage)
    private Salle       selectedSalle; // La salle actuellement sélectionnée (null par défaut)
    private VBox        selectedCard;  // La carte visuellement surlignée

    /** Appelé automatiquement par JavaFX après le chargement du FXML. */
    @FXML
    public void initialize() {
        // Chargement des salles depuis le service (fictif pour l'instant)
        allSalles = MockDataService.getInstance().getAllSalles();

        // Bouton désactivé tant qu'aucune salle n'est choisie
        btnConfirm.setDisable(true);
        labelSelected.setText("Aucune salle sélectionnée");

        // Filtrage en temps réel lors de la saisie dans la barre de recherche
        searchField.textProperty().addListener((obs, old, val) -> filterRooms(val));

        // Affichage initial de toutes les salles
        renderRooms(allSalles);
    }

    // -------------------------------------------------------------------------
    //  Filtrage
    // -------------------------------------------------------------------------

    /**
     * Filtre les salles affichées selon le texte saisi.
     * La recherche porte sur le nom de la salle ET sur ses équipements.
     * Ex: taper "Ordinateurs" n'affiche que les salles avec des ordinateurs.
     */
    private void filterRooms(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        List<Salle> filtered = allSalles.stream()
                .filter(s -> s.getNom().toLowerCase().contains(q)
                          || s.getListe_materiel().stream()
                              .anyMatch(m -> m.toLowerCase().contains(q)))
                .toList();
        renderRooms(filtered);
    }

    // -------------------------------------------------------------------------
    //  Rendu des cartes de salle
    // -------------------------------------------------------------------------

    /** Vide la grille et recrée les cartes pour la liste de salles donnée. */
    private void renderRooms(List<Salle> salles) {
        roomGrid.getChildren().clear();
        selectedCard = null; // Réinitialise la sélection visuelle lors du filtrage
        for (Salle s : salles) {
            roomGrid.getChildren().add(buildRoomCard(s));
        }
    }

    /**
     * Construit une carte visuelle pour une salle.
     * Affiche : nom, capacité, séparateur, liste des équipements sous forme de tags.
     * Un clic sur la carte appelle selectCard().
     */
    private VBox buildRoomCard(Salle salle) {
        VBox card = new VBox(10);
        card.getStyleClass().add("room-card");
        card.setPrefWidth(260);

        // En-tête : nom de la salle + capacité
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nomLbl = new Label(salle.getNom());
        nomLbl.getStyleClass().add("room-card-title");
        HBox.setHgrow(nomLbl, Priority.ALWAYS);

        Label capLbl = new Label("\uD83D\uDC65 " + salle.getPlace() + " places");
        capLbl.getStyleClass().add("room-card-capacity");

        header.getChildren().addAll(nomLbl, capLbl);

        // Séparateur horizontal
        Separator sep = new Separator();

        // Tags d'équipements (ex: "Projecteur", "WiFi")
        FlowPane equipFlow = new FlowPane(6, 6);
        for (String mat : salle.getListe_materiel()) {
            Label tag = new Label(mat);
            tag.getStyleClass().add("equipment-tag"); // Style bleu clair (voir main.css)
            equipFlow.getChildren().add(tag);
        }

        card.getChildren().addAll(header, sep, equipFlow);

        // Sélection de la salle au clic sur la carte
        card.setOnMouseClicked(e -> selectCard(card, salle));

        return card;
    }

    // -------------------------------------------------------------------------
    //  Sélection
    // -------------------------------------------------------------------------

    /**
     * Marque une carte comme sélectionnée visuellement (bordure bleue)
     * et enregistre la salle correspondante.
     * Active le bouton "Confirmer".
     */
    private void selectCard(VBox card, Salle salle) {
        // Retire la surbrillance de la carte précédemment sélectionnée
        if (selectedCard != null) {
            selectedCard.getStyleClass().remove("room-card-selected");
        }

        selectedCard  = card;
        selectedSalle = salle;

        card.getStyleClass().add("room-card-selected"); // Bordure bleue (voir main.css)
        btnConfirm.setDisable(false);
        labelSelected.setText("Salle sélectionnée : " + salle.getNom()
                + "  (" + salle.getPlace() + " places)");
    }

    // -------------------------------------------------------------------------
    //  Actions boutons
    // -------------------------------------------------------------------------

    /**
     * Confirme la salle sélectionnée.
     * SPRINT 2 : remplacer ce dialogue par la navigation vers le choix du créneau (US7).
     * La salle choisie est accessible via this.selectedSalle.
     */
    @FXML
    private void onConfirm() {
        if (selectedSalle == null) return;

        // TODO Sprint 2 : passer selectedSalle au contrôleur de l'étape suivante (US7)
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Salle sélectionnée");
        alert.setHeaderText(null);
        alert.setContentText("Salle choisie : " + selectedSalle.getNom()
                + "\nLa suite du flux (choix du créneau) sera intégrée au Sprint 2.");
        alert.showAndWait();
    }

    /** Annule la sélection et retourne à l'emploi du temps. */
    @FXML
    private void onCancel() {
        SceneManager.getInstance().getMainLayoutController().navigateTo(View.TIMETABLE);
    }
}
