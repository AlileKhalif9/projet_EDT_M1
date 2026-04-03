package projet.M1.ui;

import javafx.application.Platform;
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
import projet.M1.controller.DemandeModificationController;
import projet.M1.controller.EmploiDuTempsController;
import projet.M1.controller.HoraireController;
import projet.M1.controller.SalleController;
import projet.M1.session.SessionManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Controller de la page "Demandes de modification".
 * Passe par les back-end controllers — jamais les DAOs directement.
 *
 * Mode PROFESSEUR   → formulaire 3 étapes (US6–9)
 * Mode GESTIONNAIRE → liste PENDING avec Approuver / Rejeter / EDT croisés (US10–12)
 */
public class ModificationRequestController {

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

    private static final List<String> TIME_SLOTS = Stream
            .iterate(LocalTime.of(7, 30), t -> !t.isAfter(LocalTime.of(19, 0)), t -> t.plusMinutes(30))
            .map(t -> t.format(DateTimeFormatter.ofPattern("HH:mm")))
            .toList();

    private static final DateTimeFormatter FMT_DATE  = DateTimeFormatter.ofPattern("EEEE d MMM", Locale.FRENCH);
    private static final DateTimeFormatter FMT_TIME  = DateTimeFormatter.ofPattern("HH'h'mm");
    private static final DateTimeFormatter FMT_PARSE = DateTimeFormatter.ofPattern("HH:mm");

    // -------------------------------------------------------------------------
    //  Composants FXML
    // -------------------------------------------------------------------------

    @FXML private Label  labelTitle;
    @FXML private Label  labelSubtitle;
    @FXML private Button btnNouvellesDemande;
    @FXML private VBox   formPanel;
    @FXML private VBox   requestsContainer;

    @FXML private DatePicker        datePickerActuel;
    @FXML private ComboBox<String>  comboHeureActuelle;
    @FXML private Label             labelCoursFound;

    @FXML private DatePicker            datePickerNouveau;
    @FXML private ComboBox<String>      comboHeureDebutNouveau;
    @FXML private ComboBox<String>      comboHeureFinNouveau;
    @FXML private ComboBox<SalleEntity> comboSalle;

    @FXML private ComboBox<String> comboRaison;

    // -------------------------------------------------------------------------
    //  Back-end controllers (jamais de DAO directement dans le front)
    // -------------------------------------------------------------------------

    private final DemandeModificationController demandeController =
            new DemandeModificationController(new DemandeDAO(), new CoursDAO());
    private final EmploiDuTempsController edtController =
            new EmploiDuTempsController(new CoursDAO());
    private final SalleController   salleController   = new SalleController(new SalleDAO());
    private final HoraireController horaireController = new HoraireController(new HoraireDAO());

    /** Cours identifié à l'étape 1 — null si introuvable. */
    private CoursEntity coursSelectionne = null;

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

    private void setupFormCombos(UserEntity u) {
        comboHeureActuelle.getItems().setAll(TIME_SLOTS);
        comboHeureDebutNouveau.getItems().setAll(TIME_SLOTS);
        comboHeureFinNouveau.getItems().setAll(TIME_SLOTS);

        datePickerActuel.setOnAction(e -> searchCours(u));
        comboHeureActuelle.setOnAction(e -> searchCours(u));

        comboSalle.setConverter(new StringConverter<>() {
            public String toString(SalleEntity s) {
                if (s == null) return "";
                return s.getNom() + "  (" + s.getPlace() + " places)";
            }
            public SalleEntity fromString(String s) { return null; }
        });
        try {
            comboSalle.getItems().setAll(salleController.getAllSalles());
        } catch (Exception e) {
            comboSalle.getItems().clear();
        }

        comboRaison.getItems().setAll(RAISONS);
    }

    // -------------------------------------------------------------------------
    //  Étape 1 — recherche du cours via EmploiDuTempsController
    // -------------------------------------------------------------------------

    private void searchCours(UserEntity u) {
        coursSelectionne = null;
        LocalDate date     = datePickerActuel.getValue();
        String    heureStr = comboHeureActuelle.getValue();

        if (date == null || heureStr == null) {
            setCoursLabel("idle", "Choisissez une date et une heure pour identifier le cours");
            return;
        }

        LocalTime heure = LocalTime.parse(heureStr, FMT_PARSE);

        try {
            List<CoursEntity> semaine = (u.getRole() == Role.PROFESSEUR)
                    ? edtController.getEmploiDuTempsConnecte(u, date.with(DayOfWeek.MONDAY))
                    : List.of();

            coursSelectionne = semaine.stream()
                    .filter(c -> c.getHoraire() != null
                            && date.equals(c.getHoraire().getJour())
                            && !heure.isBefore(c.getHoraire().getHeureDebut())
                            && heure.isBefore(c.getHoraire().getHeureFin()))
                    .findFirst()
                    .orElse(null);

            if (coursSelectionne != null) {
                HoraireEntity h = coursSelectionne.getHoraire();
                String salle = coursSelectionne.getSalle() != null
                        ? coursSelectionne.getSalle().getNom() : "salle inconnue";
                setCoursLabel("ok",
                        "Cours trouvé : " + coursSelectionne.getNom()
                                + " (" + coursSelectionne.getTypeCours() + ")"
                                + "  ·  " + h.getHeureDebut().format(FMT_TIME)
                                + " – " + h.getHeureFin().format(FMT_TIME)
                                + "  ·  " + salle);
            } else {
                setCoursLabel("error", "Aucun cours à ce créneau — vérifiez la date et l'heure");
            }
        } catch (Exception e) {
            setCoursLabel("error", "Impossible de vérifier (base de données inaccessible)");
        }
    }

    private void setCoursLabel(String state, String text) {
        labelCoursFound.setText(text);
        labelCoursFound.getStyleClass().setAll(
                switch (state) {
                    case "ok"    -> "cours-found-ok";
                    case "error" -> "cours-found-error";
                    default      -> "cours-found-idle";
                }
        );
    }

    // -------------------------------------------------------------------------
    //  Formulaire prof
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
        if (coursSelectionne == null) {
            showAlert(Alert.AlertType.WARNING, "Cours non identifié",
                    "Aucun cours n'a été trouvé au créneau sélectionné.\n"
                            + "Corrigez la date ou l'heure à l'étape 1.");
            return;
        }

        if (datePickerNouveau.getValue() == null
                || comboHeureDebutNouveau.getValue() == null
                || comboHeureFinNouveau.getValue() == null
                || comboRaison.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants",
                    "Veuillez renseigner le nouveau créneau (date, heure début, heure fin) et la raison.");
            return;
        }

        LocalDate nouvelleDate = datePickerNouveau.getValue();
        LocalTime heureDebut   = LocalTime.parse(comboHeureDebutNouveau.getValue(), FMT_PARSE);
        LocalTime heureFin     = LocalTime.parse(comboHeureFinNouveau.getValue(),   FMT_PARSE);

        if (!heureFin.isAfter(heureDebut)) {
            showAlert(Alert.AlertType.WARNING, "Horaire invalide",
                    "L'heure de fin doit être strictement après l'heure de début.");
            return;
        }

        UserEntity u = SessionManager.getInstance().getUtilisateurConnecte();

        try {
            HoraireEntity nouvelHoraire =
                    horaireController.findOrCreate(nouvelleDate, heureDebut, heureFin);

            demandeController.soumettreDemande(
                    coursSelectionne,
                    nouvelHoraire,
                    comboSalle.getValue(),
                    comboRaison.getValue(),
                    u);

            showAlert(Alert.AlertType.INFORMATION, "Demande soumise",
                    "Votre demande a été soumise avec succès.\nElle sera traitée par le gestionnaire.");
            resetForm();
            onAnnulerForm();
            buildRequestCards(false, u);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de soumettre la demande. Vérifiez la connexion à la base de données.");
        }
    }

    @FXML
    private void onAnnulerForm() {
        formPanel.setVisible(false);
        formPanel.setManaged(false);
        btnNouvellesDemande.setText("+ Nouvelle demande");
    }

    private void resetForm() {
        datePickerActuel.setValue(null);
        comboHeureActuelle.getSelectionModel().clearSelection();
        setCoursLabel("idle", "Choisissez une date et une heure pour identifier le cours");
        coursSelectionne = null;
        datePickerNouveau.setValue(null);
        comboHeureDebutNouveau.getSelectionModel().clearSelection();
        comboHeureFinNouveau.getSelectionModel().clearSelection();
        comboSalle.getSelectionModel().clearSelection();
        comboRaison.getSelectionModel().clearSelection();
    }

    // -------------------------------------------------------------------------
    //  Liste des demandes via DemandeModificationController
    // -------------------------------------------------------------------------

    private void buildRequestCards(boolean isGestionnaire, UserEntity u) {
        requestsContainer.getChildren().clear();
        try {
            List<CoursModificationRequestEntity> demandes = isGestionnaire
                    ? demandeController.getDemandesEnAttente()
                    : demandeController.getDemandesByProfesseur(u);

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

    private VBox buildCard(CoursModificationRequestEntity d, boolean isGestionnaire) {
        VBox card = new VBox();
        card.getStyleClass().add("request-card");

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
                buildSchedulePanel(true,  creneauNouveau));

        HBox reasonRow = new HBox(4);
        Label rKey = new Label("Raison : ");
        rKey.getStyleClass().add("reason-label");
        Label rVal = new Label(d.getRaison() != null ? d.getRaison() : "—");
        rVal.getStyleClass().add("reason-text");
        rVal.setWrapText(true);
        reasonRow.getChildren().addAll(rKey, rVal);

        body.getChildren().addAll(schedules, reasonRow);

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

            // US11 — EDT croisés
            Button btnEdtCroise = new Button("Voir les EDT croisés");
            btnEdtCroise.getStyleClass().add("btn-edt-croise");

            VBox edtCroisePanel = new VBox(12);
            edtCroisePanel.getStyleClass().add("edt-croise-panel");
            edtCroisePanel.setVisible(false);
            edtCroisePanel.setManaged(false);

            btnEdtCroise.setOnAction(e -> toggleEdtCroise(d, edtCroisePanel, btnEdtCroise));

            actions.getChildren().addAll(btnApprove, btnReject, btnEdtCroise);
            body.getChildren().addAll(sep, actions, edtCroisePanel);
        }

        card.getChildren().addAll(header, body);
        return card;
    }

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
    //  Actions gestionnaire via DemandeModificationController
    // -------------------------------------------------------------------------

    private void onApprouver(Long id, String nomCours, VBox card) {
        try {
            demandeController.approuverDemande(id);
            showAlert(Alert.AlertType.INFORMATION, "Demande approuvée",
                    "La demande pour \"" + nomCours + "\" a été approuvée et le cours a été déplacé.");
            requestsContainer.getChildren().remove(card);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'approuver la demande.");
        }
    }

    private void onRejeter(Long id, String nomCours, VBox card) {
        try {
            demandeController.rejeterDemande(id);
            showAlert(Alert.AlertType.INFORMATION, "Demande rejetée",
                    "La demande pour \"" + nomCours + "\" a été rejetée.");
            requestsContainer.getChildren().remove(card);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de rejeter la demande.");
        }
    }

    // -------------------------------------------------------------------------
    //  US11 — EDT croisés (prof + groupe) pour la semaine de la demande
    // -------------------------------------------------------------------------

    private void toggleEdtCroise(CoursModificationRequestEntity d, VBox panel, Button btn) {
        boolean wasVisible = panel.isVisible();
        if (wasVisible) {
            panel.setVisible(false);
            panel.setManaged(false);
            btn.setText("Voir les EDT croisés");
            return;
        }

        btn.setText("Chargement…");
        btn.setDisable(true);
        panel.getChildren().clear();
        panel.setVisible(true);
        panel.setManaged(true);

        LocalDate semaine = d.getCours() != null && d.getCours().getHoraire() != null
                ? d.getCours().getHoraire().getJour().with(DayOfWeek.MONDAY)
                : LocalDate.now().with(DayOfWeek.MONDAY);

        Thread t = new Thread(() -> {
            List<CoursEntity> coursProfList;
            List<CoursEntity> coursGroupeList;

            try {
                UserEntity prof = d.getCours() != null && d.getCours().getList_professeur() != null
                        && !d.getCours().getList_professeur().isEmpty()
                        ? d.getCours().getList_professeur().get(0) : null;

                coursProfList = prof != null
                        ? edtController.getEmploiDuTempsConnecte(prof, semaine)
                        : List.of();

                String nomGroupe = d.getCours() != null && d.getCours().getList_etudiant() != null
                        && !d.getCours().getList_etudiant().isEmpty()
                        && d.getCours().getList_etudiant().get(0).getGroupe() != null
                        ? d.getCours().getList_etudiant().get(0).getGroupe().getNom() : null;

                coursGroupeList = nomGroupe != null
                        ? edtController.getEmploiDuTempsGroupe(nomGroupe, semaine)
                        : List.of();

                String nomProf    = prof != null ? prof.getPrenom() + " " + prof.getNom() : "Professeur inconnu";
                String nomGroupe2 = nomGroupe != null ? nomGroupe : "Groupe inconnu";

                Platform.runLater(() -> {
                    buildEdtCroisePanel(panel, semaine, nomProf, coursProfList, nomGroupe2, coursGroupeList);
                    btn.setText("Masquer les EDT croisés");
                    btn.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    Label err = new Label("Erreur : " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
                    err.getStyleClass().add("cours-found-error");
                    err.setWrapText(true);
                    panel.getChildren().add(err);
                    btn.setText("Voir les EDT croisés");
                    btn.setDisable(false);
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void buildEdtCroisePanel(VBox panel, LocalDate semaine,
                                     String nomProf, List<CoursEntity> coursProfList,
                                     String nomGroupe, List<CoursEntity> coursGroupeList) {
        DateTimeFormatter fmtSemaine = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.FRENCH);
        Label titre = new Label("EDT croisés — semaine du " + semaine.format(fmtSemaine));
        titre.getStyleClass().add("edt-croise-title");

        HBox grilles = new HBox(16);

        VBox colProf   = buildEdtMiniGrid(nomProf,   coursProfList);
        VBox colGroupe = buildEdtMiniGrid(nomGroupe, coursGroupeList);
        HBox.setHgrow(colProf,   Priority.ALWAYS);
        HBox.setHgrow(colGroupe, Priority.ALWAYS);
        colProf.setMaxWidth(Double.MAX_VALUE);
        colGroupe.setMaxWidth(Double.MAX_VALUE);

        grilles.getChildren().addAll(colProf, colGroupe);
        panel.getChildren().addAll(titre, grilles);
    }

    private VBox buildEdtMiniGrid(String titre, List<CoursEntity> cours) {
        VBox col = new VBox(6);
        col.getStyleClass().add("edt-croise-col");

        Label header = new Label(titre);
        header.getStyleClass().add("edt-croise-col-title");
        header.setWrapText(true);
        col.getChildren().add(header);

        if (cours.isEmpty()) {
            Label vide = new Label("Aucun cours cette semaine");
            vide.getStyleClass().add("edt-croise-empty");
            col.getChildren().add(vide);
            return col;
        }

        for (CoursEntity c : cours) {
            HoraireEntity h = c.getHoraire();
            if (h == null) continue;

            HBox row = new HBox(8);
            row.getStyleClass().add("edt-croise-row");
            row.setAlignment(Pos.CENTER_LEFT);

            String jour  = h.getJour().format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.FRENCH));
            String heure = h.getHeureDebut().format(FMT_TIME) + " – " + h.getHeureFin().format(FMT_TIME);

            Label nomLabel = new Label(c.getNom() != null ? c.getNom() : "—");
            nomLabel.getStyleClass().add("edt-croise-cours-nom");
            HBox.setHgrow(nomLabel, Priority.ALWAYS);
            nomLabel.setMaxWidth(Double.MAX_VALUE);

            Label infoLabel = new Label(jour + "  " + heure);
            infoLabel.getStyleClass().add("edt-croise-cours-info");

            row.getChildren().addAll(nomLabel, infoLabel);
            col.getChildren().add(row);
        }

        return col;
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