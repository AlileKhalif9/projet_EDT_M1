package projet.M1.ui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import projet.M1.model.utilisateur_systeme.Gestionnaire_Planning;
import projet.M1.model.utilisateur_systeme.Utilisateur;
import projet.M1.session.SessionManager;

import java.time.LocalDate;
import java.util.List;

/**
 * Page "Demandes de modification"
 *
 * Les listes déroulantes + les demandes sont hardcodées ici (front only).
 * Le back-end remplacera ces données par de vrais appels DAO.
 */
public class ModificationRequestController {


    private static final List<String> SALLES = List.of(
            "Salle 305 — Bâtiment A, 50 places",
            "Lab 102 — Bâtiment A, 25 places",
            "Salle 401 — Bâtiment B, 60 places",
            "Salle 302 — Bâtiment A, 40 places"
    );

    private static final List<String> CRENEAUX = List.of(
            "Lundi 8h00 – 10h00",   "Lundi 10h00 – 12h00",  "Lundi 14h00 – 16h00",
            "Mardi 8h00 – 10h00",   "Mardi 10h00 – 12h00",  "Mardi 14h00 – 16h00",
            "Mercredi 8h00 – 10h00","Mercredi 14h00 – 16h00",
            "Jeudi 8h00 – 10h00",   "Jeudi 14h00 – 16h00",
            "Vendredi 10h00 – 12h00","Vendredi 14h00 – 16h00"
    );

    private static final List<String> RAISONS = List.of(
            "Conflit avec une réunion de département",
            "Besoin d'équipement de laboratoire",
            "Indisponibilité personnelle",
            "Préférence du groupe d'étudiants",
            "Salle inadaptée (capacité insuffisante)",
            "Salle inadaptée (équipement manquant)",
            "Déplacement professionnel",
            "Autre raison"
    );

    // Demandes fictives pour la liste (gestionnaire + historique prof)
    // Format : { cours, demandéPar, créneauActuel, nouveauCréneau, salleActuelle, nouvelleSalle, raison, statut, date }
    private static final String[][] DEMANDES_MOCK = {
            {"Algorithmique",  "M. Martin", "Lundi 9h00 – 11h00",    "Lundi 14h00 – 16h00",
             "Salle 305",      "Salle 305", "Conflit avec une réunion",      "en attente", "Il y a 2 jours"},
            {"Base de données","M. Dupont",  "Mercredi 14h00 – 16h00","Mercredi 14h00 – 16h00",
             "Salle 201",      "Lab 102",   "Besoin d'équipement",           "approuvée",  "Il y a 5 jours"},
            {"Réseaux",        "M. Bernard","Vendredi 10h00 – 12h00","Vendredi 15h00 – 17h00",
             "Salle TP2",      "Salle TP2", "Préférence du groupe",          "rejetée",    "Il y a 1 semaine"}
    };

    // -------------------------------------------------------------------------
    //  Composants FXML
    // -------------------------------------------------------------------------

    @FXML private Label labelTitle;
    @FXML private Label labelSubtitle;
    @FXML private Button btnNouvellesDemande;
    @FXML private VBox formPanel;
    @FXML private VBox requestsContainer;

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> comboSalle;
    @FXML private ComboBox<String> comboCreneauActuel;
    @FXML private ComboBox<String> comboNouveauCreneau;
    @FXML private ComboBox<String> comboRaison;

    // -------------------------------------------------------------------------
    //  Initialisation
    // -------------------------------------------------------------------------

    @FXML
    public void initialize() {
        Utilisateur u = SessionManager.getInstance().getUtilisateurConnecte();
        boolean isGestionnaire = u instanceof Gestionnaire_Planning;

        if (isGestionnaire) {
            // Vue gestionnaire : titre différent, pas de bouton "Nouvelle demande"
            labelTitle.setText("Demandes de modification");
            labelSubtitle.setText("Examinez et approuvez les demandes en attente");
            btnNouvellesDemande.setVisible(false);
            btnNouvellesDemande.setManaged(false);
        }

        // Remplir les listes du formulaire (prof uniquement mais on charge quand même)
        comboSalle.getItems().setAll(SALLES);
        comboCreneauActuel.getItems().setAll(CRENEAUX);
        comboNouveauCreneau.getItems().setAll(CRENEAUX);
        comboRaison.getItems().setAll(RAISONS);
        datePicker.setValue(LocalDate.now());

        // Construire la liste des demandes
        buildRequestCards(isGestionnaire);
    }

    // -------------------------------------------------------------------------
    //  Formulaire (prof)
    // -------------------------------------------------------------------------

    @FXML
    private void onToggleForm() {
        boolean visible = formPanel.isVisible();
        formPanel.setVisible(!visible);
        formPanel.setManaged(!visible);
        btnNouvellesDemande.setText(visible ? "+ Nouvelle demande" : "Voir les demandes");
    }

    @FXML
    private void onSubmit() {
        if (comboSalle.getValue() == null
                || comboCreneauActuel.getValue() == null
                || comboNouveauCreneau.getValue() == null
                || comboRaison.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Champs manquants");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez renseigner tous les champs avant de soumettre.");
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Demande soumise");
        alert.setHeaderText(null);
        alert.setContentText("Votre demande a été soumise avec succès.\nElle sera traitée par le gestionnaire.");
        alert.showAndWait();

        resetForm();
        onAnnulerForm();
    }

    @FXML
    private void onAnnulerForm() {
        formPanel.setVisible(false);
        formPanel.setManaged(false);
        btnNouvellesDemande.setText("+ Nouvelle demande");
    }

    private void resetForm() {
        datePicker.setValue(LocalDate.now());
        comboSalle.getSelectionModel().clearSelection();
        comboCreneauActuel.getSelectionModel().clearSelection();
        comboNouveauCreneau.getSelectionModel().clearSelection();
        comboRaison.getSelectionModel().clearSelection();
    }

    // -------------------------------------------------------------------------
    //  Liste des demandes
    // -------------------------------------------------------------------------

    /**
     * Construit une carte par demande.
     * Le gestionnaire voit les boutons Approuver/Rejeter sur les demandes "en attente".
     * Le prof voit juste ses demandes avec leur statut.
     */
    private void buildRequestCards(boolean isGestionnaire) {
        requestsContainer.getChildren().clear();
        for (String[] d : DEMANDES_MOCK) {
            requestsContainer.getChildren().add(buildCard(d, isGestionnaire));
        }
    }

    /**
     * Carte d'une demande.
     */
    private VBox buildCard(String[] d, boolean isGestionnaire) {
        VBox card = new VBox();
        card.getStyleClass().add("request-card");

        // -- Header --
        HBox header = new HBox(12);
        header.getStyleClass().add("request-card-header");
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(4);
        HBox.setHgrow(titles, Priority.ALWAYS);
        Label coursLbl = new Label(d[0]);
        coursLbl.getStyleClass().add("request-card-title");
        Label subLbl = new Label("Demandé par " + d[1] + " · " + d[8]);
        subLbl.getStyleClass().add("request-card-subtitle");
        titles.getChildren().addAll(coursLbl, subLbl);

        Label badge = buildStatusBadge(d[7]);
        header.getChildren().addAll(titles, badge);

        // -- Corps --
        VBox body = new VBox(16);
        body.getStyleClass().add("request-card-body");

        // Deux panneaux : actuel (gris) / demandé (bleu)
        HBox schedules = new HBox(12);
        schedules.getChildren().addAll(
                buildSchedulePanel(false, d[2], d[4]),
                buildSchedulePanel(true,  d[3], d[5])
        );

        // Raison
        HBox reasonRow = new HBox(4);
        Label rKey = new Label("Raison : ");
        rKey.getStyleClass().add("reason-label");
        Label rVal = new Label(d[6]);
        rVal.getStyleClass().add("reason-text");
        rVal.setWrapText(true);
        reasonRow.getChildren().addAll(rKey, rVal);

        body.getChildren().addAll(schedules, reasonRow);

        // Boutons gestionnaire (US12) — uniquement sur les demandes "en attente"
        if (isGestionnaire && "en attente".equals(d[7])) {
            Separator sep = new Separator();
            HBox actions = new HBox(10);
            actions.setAlignment(Pos.CENTER_LEFT);

            Button btnApprove = new Button("Approuver");
            btnApprove.getStyleClass().add("btn-approve");
            btnApprove.setOnAction(e -> onApprouver(d[0], card));

            Button btnReject = new Button("Rejeter");
            btnReject.getStyleClass().add("btn-reject");
            btnReject.setOnAction(e -> onRejeter(d[0], card));

            actions.getChildren().addAll(btnApprove, btnReject);
            body.getChildren().addAll(sep, actions);
        }

        card.getChildren().addAll(header, body);
        return card;
    }

    private VBox buildSchedulePanel(boolean isNew, String horaire, String salle) {
        VBox panel = new VBox(8);
        panel.getStyleClass().add(isNew ? "schedule-panel-requested" : "schedule-panel-current");
        HBox.setHgrow(panel, Priority.ALWAYS);
        panel.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label(isNew ? "CRÉNEAU DEMANDÉ" : "CRÉNEAU ACTUEL");
        title.getStyleClass().add(isNew ? "schedule-panel-label-blue" : "schedule-panel-label");

        Label horaireLbl = new Label("  " + horaire);
        horaireLbl.getStyleClass().add(isNew ? "schedule-info-blue" : "schedule-info");

        Label salleLbl = new Label("  " + salle);
        salleLbl.getStyleClass().add(isNew ? "schedule-info-blue" : "schedule-info");

        panel.getChildren().addAll(title, horaireLbl, salleLbl);
        return panel;
    }

    private Label buildStatusBadge(String status) {
        Label badge = new Label(capitalize(status));
        badge.getStyleClass().add("status-badge");
        badge.getStyleClass().add(switch (status) {
            case "approuvée" -> "status-approuvee";
            case "rejetée" -> "status-rejetee";
            default -> "status-attente";
        });
        return badge;
    }

    private void onApprouver(String cours, VBox card) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Demande approuvée");
        alert.setHeaderText(null);
        alert.setContentText("La demande pour \"" + cours + "\" a été approuvée.\nLe professeur et le groupe ont été notifiés.");
        alert.showAndWait();
        requestsContainer.getChildren().remove(card);
    }

    private void onRejeter(String cours, VBox card) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Demande rejetée");
        alert.setHeaderText(null);
        alert.setContentText("La demande pour \"" + cours + "\" a été rejetée.\nLe professeur a été notifié.");
        alert.showAndWait();
        requestsContainer.getChildren().remove(card);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
