package projet.M1.ui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import projet.M1.BDD.dao.CoursDAO;
import projet.M1.BDD.dao.DemandeDAO;
import projet.M1.BDD.dao.HoraireDAO;
import projet.M1.BDD.dao.SalleDAO;
import projet.M1.BDD.entity.*;
import projet.M1.session.SessionManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Page "Demandes de modification" — FXML : modification-request.fxml
 *
 * Mode PROFESSEUR  → formulaire en 3 étapes pour soumettre une demande (US6–US9)
 * Mode GESTIONNAIRE → liste des demandes en attente avec Approuver/Rejeter (US10, US12)
 *
 * Intégration BDD :
 *  - Les cours du prof viennent de CoursDAO (ComboBox créneau actuel)
 *  - Les salles viennent de SalleDAO (ComboBox salle)
 *  - Les horaires viennent de HoraireDAO (ComboBox nouveau créneau)
 *  - Les demandes viennent de DemandeDAO (liste gestionnaire + historique prof)
 *  - La soumission persiste via DemandeDAO.save()
 *  - L'approbation/rejet via DemandeDAO.updateStatut()
 */
public class ModificationRequestController {

    // Raisons prédéfinies (données métier fixes, pas en BDD)
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

    private static final DateTimeFormatter FMT_DATE =
            DateTimeFormatter.ofPattern("EEEE d MMM", Locale.FRENCH);
    private static final DateTimeFormatter FMT_TIME =
            DateTimeFormatter.ofPattern("HH'h'mm");

    // -------------------------------------------------------------------------
    //  Composants FXML
    // -------------------------------------------------------------------------

    @FXML private Label  labelTitle;
    @FXML private Label  labelSubtitle;
    @FXML private Button btnNouvellesDemande;
    @FXML private VBox   formPanel;
    @FXML private VBox   requestsContainer;

    @FXML private DatePicker              datePicker;
    @FXML private ComboBox<CoursEntity>   comboCreneauActuel;
    @FXML private ComboBox<HoraireEntity> comboNouveauCreneau;
    @FXML private ComboBox<SalleEntity>   comboSalle;
    @FXML private ComboBox<String>        comboRaison;

    // -------------------------------------------------------------------------
    //  DAOs
    // -------------------------------------------------------------------------

    private final CoursDAO   coursDAO   = new CoursDAO();
    private final SalleDAO   salleDAO   = new SalleDAO();
    private final HoraireDAO horaireDAO = new HoraireDAO();
    private final DemandeDAO demandeDAO = new DemandeDAO();

    // -------------------------------------------------------------------------
    //  Initialisation
    // -------------------------------------------------------------------------

    @FXML
    public void initialize() {
        UserEntity u = SessionManager.getInstance().getUtilisateurConnecte();
        boolean isGestionnaire = u != null && u.getRole() == Role.GESTIONNAIRE_PLANNING;

        if (isGestionnaire) {
            labelTitle.setText("Demandes de modification");
            labelSubtitle.setText("Examinez et approuvez les demandes en attente");
            btnNouvellesDemande.setVisible(false);
            btnNouvellesDemande.setManaged(false);
        }

        setupFormCombos(u);
        buildRequestCards(isGestionnaire, u);
    }

    /**
     * Remplit les ComboBox du formulaire avec les vraies données de la BDD.
     * Chaque ComboBox a un StringConverter pour afficher un texte lisible.
     */
    private void setupFormCombos(UserEntity u) {
        // -- ComboBox : cours du professeur (créneau à modifier) --
        comboCreneauActuel.setConverter(new StringConverter<>() {
            public String toString(CoursEntity c) {
                if (c == null) return "";
                String horaire = c.getHoraire() != null
                        ? c.getHoraire().getJour().format(FMT_DATE)
                          + " " + c.getHoraire().getHeureDebut().format(FMT_TIME)
                          + " – " + c.getHoraire().getHeureFin().format(FMT_TIME)
                        : "?";
                return (c.getNom() != null ? c.getNom() : "Cours") + "  ·  " + horaire;
            }
            public CoursEntity fromString(String s) { return null; }
        });

        // Charge les cours des 4 prochaines semaines du prof
        if (u != null && u.getRole() == Role.PROFESSEUR) {
            try {
                LocalDate lundi = LocalDate.now().with(DayOfWeek.MONDAY);
                List<CoursEntity> cours = coursDAO.findByProfesseurAndSemaine(u, lundi);
                comboCreneauActuel.getItems().setAll(cours);
            } catch (Exception e) {
                comboCreneauActuel.getItems().clear();
            }
        }

        // -- ComboBox : toutes les salles --
        comboSalle.setConverter(new StringConverter<>() {
            public String toString(SalleEntity s) {
                if (s == null) return "";
                return s.getNom() + "  (" + s.getPlace() + " places)";
            }
            public SalleEntity fromString(String s) { return null; }
        });
        try {
            comboSalle.getItems().setAll(salleDAO.findAll());
        } catch (Exception e) {
            comboSalle.getItems().clear();
        }

        // -- ComboBox : horaires disponibles pour le nouveau créneau --
        // Quand le prof sélectionne une date, on recharge les horaires de ce jour
        comboNouveauCreneau.setConverter(new StringConverter<>() {
            public String toString(HoraireEntity h) {
                if (h == null) return "";
                return h.getJour().format(FMT_DATE)
                        + "  " + h.getHeureDebut().format(FMT_TIME)
                        + " – " + h.getHeureFin().format(FMT_TIME);
            }
            public HoraireEntity fromString(String s) { return null; }
        });
        datePicker.setValue(LocalDate.now());
        datePicker.setOnAction(e -> reloadHoraires());
        reloadHoraires();

        // -- ComboBox : raisons (données fixes) --
        comboRaison.getItems().setAll(RAISONS);
    }

    /** Recharge les horaires disponibles pour la date sélectionnée dans le DatePicker. */
    private void reloadHoraires() {
        LocalDate date = datePicker.getValue();
        if (date == null) return;
        try {
            List<HoraireEntity> horaires = horaireDAO.findByJour(date);
            comboNouveauCreneau.getItems().setAll(horaires);
        } catch (Exception e) {
            comboNouveauCreneau.getItems().clear();
        }
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

    /**
     * US8 – Soumet la demande en BDD.
     * Crée un CoursModificationRequestEntity et le persiste via DemandeDAO.
     */
    @FXML
    private void onSubmit() {
        if (comboCreneauActuel.getValue() == null
                || comboSalle.getValue() == null
                || comboRaison.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants",
                    "Veuillez renseigner tous les champs avant de soumettre.");
            return;
        }

        UserEntity u = SessionManager.getInstance().getUtilisateurConnecte();

        CoursModificationRequestEntity demande = new CoursModificationRequestEntity();
        demande.setCours(comboCreneauActuel.getValue());
        demande.setDemandeur(u);
        demande.setNouvelleSalle(comboSalle.getValue());
        demande.setNouveauCreneau(comboNouveauCreneau.getValue()); // peut être null si aucun horaire sélectionné
        demande.setRaison(comboRaison.getValue());
        demande.setStatut(StatutDemande.PENDING);

        try {
            demandeDAO.save(demande);
            showAlert(Alert.AlertType.INFORMATION, "Demande soumise",
                    "Votre demande a été soumise avec succès.\nElle sera traitée par le gestionnaire.");
            resetForm();
            onAnnulerForm();
            // Rafraîchit la liste des demandes du prof
            buildRequestCards(false, u);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de soumettre la demande. Vérifiez la connexion à la base de données.");
        }
    }

    /** US9 – Annule et ferme le formulaire sans rien envoyer. */
    @FXML
    private void onAnnulerForm() {
        formPanel.setVisible(false);
        formPanel.setManaged(false);
        btnNouvellesDemande.setText("+ Nouvelle demande");
    }

    private void resetForm() {
        datePicker.setValue(LocalDate.now());
        comboCreneauActuel.getSelectionModel().clearSelection();
        comboSalle.getSelectionModel().clearSelection();
        comboNouveauCreneau.getSelectionModel().clearSelection();
        comboRaison.getSelectionModel().clearSelection();
    }

    // -------------------------------------------------------------------------
    //  Liste des demandes (US10 gestionnaire / historique prof)
    // -------------------------------------------------------------------------

    /**
     * Construit les cartes de demandes depuis la BDD.
     * Gestionnaire → toutes les demandes PENDING.
     * Professeur   → toutes ses propres demandes.
     */
    private void buildRequestCards(boolean isGestionnaire, UserEntity u) {
        requestsContainer.getChildren().clear();
        try {
            List<CoursModificationRequestEntity> demandes = isGestionnaire
                    ? demandeDAO.findByStatut(StatutDemande.PENDING)
                    : demandeDAO.findByDemandeur(u);

            if (demandes.isEmpty()) {
                Label empty = new Label(isGestionnaire
                        ? "Aucune demande en attente."
                        : "Vous n'avez soumis aucune demande.");
                empty.getStyleClass().add("text-muted");
                requestsContainer.getChildren().add(empty);
                return;
            }

            for (CoursModificationRequestEntity d : demandes) {
                requestsContainer.getChildren().add(buildCard(d, isGestionnaire));
            }
        } catch (Exception e) {
            Label err = new Label("Impossible de charger les demandes (BDD inaccessible).");
            err.getStyleClass().add("text-muted");
            requestsContainer.getChildren().add(err);
        }
    }

    /** Construit la carte visuelle d'une demande. */
    private VBox buildCard(CoursModificationRequestEntity d, boolean isGestionnaire) {
        VBox card = new VBox();
        card.getStyleClass().add("request-card");

        // -- Header --
        HBox header = new HBox(12);
        header.getStyleClass().add("request-card-header");
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(4);
        HBox.setHgrow(titles, Priority.ALWAYS);

        String nomCours = d.getCours() != null && d.getCours().getNom() != null
                ? d.getCours().getNom() : "Cours inconnu";
        Label coursLbl = new Label(nomCours);
        coursLbl.getStyleClass().add("request-card-title");

        String demandeur = d.getDemandeur() != null
                ? d.getDemandeur().getPrenom() + " " + d.getDemandeur().getNom()
                : "Inconnu";
        Label subLbl = new Label("Demandé par " + demandeur);
        subLbl.getStyleClass().add("request-card-subtitle");
        titles.getChildren().addAll(coursLbl, subLbl);

        Label badge = buildStatusBadge(d.getStatut());
        header.getChildren().addAll(titles, badge);

        // -- Corps --
        VBox body = new VBox(16);
        body.getStyleClass().add("request-card-body");

        String creneauActuel = formatCreneau(
                d.getCours() != null ? d.getCours().getHoraire() : null,
                d.getCours() != null && d.getCours().getSalle() != null
                        ? d.getCours().getSalle().getNom() : null);

        String creneauNouveau = formatCreneau(d.getNouveauCreneau(),
                d.getNouvelleSalle() != null ? d.getNouvelleSalle().getNom() : null);

        HBox schedules = new HBox(12);
        schedules.getChildren().addAll(
                buildSchedulePanel(false, creneauActuel),
                buildSchedulePanel(true,  creneauNouveau)
        );

        HBox reasonRow = new HBox(4);
        Label rKey = new Label("Raison : ");
        rKey.getStyleClass().add("reason-label");
        Label rVal = new Label(d.getRaison() != null ? d.getRaison() : "—");
        rVal.getStyleClass().add("reason-text");
        rVal.setWrapText(true);
        reasonRow.getChildren().addAll(rKey, rVal);

        body.getChildren().addAll(schedules, reasonRow);

        // Boutons gestionnaire (US12) — uniquement sur les demandes PENDING
        if (isGestionnaire && d.getStatut() == StatutDemande.PENDING) {
            Separator sep = new Separator();
            HBox actions = new HBox(10);
            actions.setAlignment(Pos.CENTER_LEFT);

            Button btnApprove = new Button("Approuver");
            btnApprove.getStyleClass().add("btn-approve");
            btnApprove.setOnAction(e -> onApprouver(d.getId(), nomCours, card));

            Button btnReject = new Button("Rejeter");
            btnReject.getStyleClass().add("btn-reject");
            btnReject.setOnAction(e -> onRejeter(d.getId(), nomCours, card));

            actions.getChildren().addAll(btnApprove, btnReject);
            body.getChildren().addAll(sep, actions);
        }

        card.getChildren().addAll(header, body);
        return card;
    }

    /** Formate un HoraireEntity + nom de salle en texte lisible pour les panneaux. */
    private String formatCreneau(HoraireEntity h, String nomSalle) {
        if (h == null) return "Non défini";
        String horaire = h.getJour().format(FMT_DATE)
                + "  " + h.getHeureDebut().format(FMT_TIME)
                + " – " + h.getHeureFin().format(FMT_TIME);
        return horaire + (nomSalle != null ? "\n" + nomSalle : "");
    }

    private VBox buildSchedulePanel(boolean isNew, String texte) {
        VBox panel = new VBox(8);
        panel.getStyleClass().add(isNew ? "schedule-panel-requested" : "schedule-panel-current");
        HBox.setHgrow(panel, Priority.ALWAYS);
        panel.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label(isNew ? "CRÉNEAU DEMANDÉ" : "CRÉNEAU ACTUEL");
        title.getStyleClass().add(isNew ? "schedule-panel-label-blue" : "schedule-panel-label");

        Label info = new Label("  " + texte);
        info.getStyleClass().add(isNew ? "schedule-info-blue" : "schedule-info");
        info.setWrapText(true);

        panel.getChildren().addAll(title, info);
        return panel;
    }

    private Label buildStatusBadge(StatutDemande statut) {
        String texte = switch (statut) {
            case PENDING  -> "En attente";
            case ACCEPTED -> "Approuvée";
            case REFUSED  -> "Rejetée";
        };
        String styleClass = switch (statut) {
            case ACCEPTED -> "status-approuvee";
            case REFUSED  -> "status-rejetee";
            default       -> "status-attente";
        };
        Label badge = new Label(texte);
        badge.getStyleClass().addAll("status-badge", styleClass);
        return badge;
    }

    // -------------------------------------------------------------------------
    //  Actions gestionnaire (US12)
    // -------------------------------------------------------------------------

    private void onApprouver(Long id, String nomCours, VBox card) {
        try {
            demandeDAO.updateStatut(id, StatutDemande.ACCEPTED);
            showAlert(Alert.AlertType.INFORMATION, "Demande approuvée",
                    "La demande pour \"" + nomCours + "\" a été approuvée.");
            requestsContainer.getChildren().remove(card);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'approuver la demande.");
        }
    }

    private void onRejeter(Long id, String nomCours, VBox card) {
        try {
            demandeDAO.updateStatut(id, StatutDemande.REFUSED);
            showAlert(Alert.AlertType.INFORMATION, "Demande rejetée",
                    "La demande pour \"" + nomCours + "\" a été rejetée.");
            requestsContainer.getChildren().remove(card);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de rejeter la demande.");
        }
    }

    // -------------------------------------------------------------------------
    //  Helper
    // -------------------------------------------------------------------------

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
