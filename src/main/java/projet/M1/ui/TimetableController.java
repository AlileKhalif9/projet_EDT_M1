package projet.M1.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.*;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import projet.M1.BDD.dao.CoursDAO;
import projet.M1.BDD.dao.GroupeDAO;
import projet.M1.BDD.dao.SalleDAO;
import projet.M1.BDD.entity.CoursEntity;
import projet.M1.BDD.entity.Role;
import projet.M1.BDD.entity.SalleEntity;
import projet.M1.BDD.entity.UserEntity;
import projet.M1.controller.EmploiDuTempsController;
import projet.M1.controller.GroupeController;
import projet.M1.controller.SalleController;
import projet.M1.model.planning.TypeCours;
import projet.M1.session.SessionManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Controller de la page EDT.
 * Passe par EmploiDuTempsController / SalleController / GroupeController (back-end) —
 * jamais les DAOs directement.
 */
public class TimetableController {

    // -------------------------------------------------------------------------
    //  Constantes de mise en page
    // -------------------------------------------------------------------------

    private static final int    HEURE_DEBUT     = 8;
    private static final int    HEURE_FIN       = 19;
    private static final int    NB_HEURES       = HEURE_FIN - HEURE_DEBUT;
    private static final double PX_PAR_HEURE    = 80.0;
    private static final double HAUTEUR_HEADER  = 44.0;
    private static final double LARGEUR_HEURE   = 64.0;

    private static final String[] JOURS = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi"};
    private static final DateTimeFormatter FMT_JOUR =
            DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH);

    // -------------------------------------------------------------------------
    //  Composants FXML
    // -------------------------------------------------------------------------

    @FXML private Label          labelSemaine;
    @FXML private Button         btnPrecedent;
    @FXML private Button         btnSuivant;
    @FXML private Button         btnAujourdHui;
    @FXML private Button         btnAjouterCours;
    @FXML private ToggleButton   tabMonEDT;
    @FXML private ToggleButton   tabTiers;
    @FXML private ToggleButton   tabSalle;
    @FXML private HBox           selectorBar;
    @FXML private ComboBox<String> comboSelector;
    @FXML private ScrollPane     scrollPane;
    @FXML private HBox           gridContainer;
    @FXML private StackPane      loadingPane;

    // -------------------------------------------------------------------------
    //  Back-end controllers (jamais de DAO directement dans le front)
    // -------------------------------------------------------------------------

    private final EmploiDuTempsController edtController =
            new EmploiDuTempsController(new CoursDAO());
    private final SalleController   salleController  = new SalleController(new SalleDAO());
    private final GroupeController  groupeController = new GroupeController(new GroupeDAO());

    // -------------------------------------------------------------------------
    //  État interne
    // -------------------------------------------------------------------------

    private LocalDate currentMonday;

    private enum TabMode { MON_EDT, TIERS, SALLE }
    private TabMode currentTab = TabMode.MON_EDT;

    private List<SalleEntity> allSalles = List.of();

    // -------------------------------------------------------------------------
    //  Initialisation
    // -------------------------------------------------------------------------

    @FXML
    public void initialize() {
        currentMonday = LocalDate.now().with(DayOfWeek.MONDAY);
        setupTabs();
        updateWeekLabel();
        buildGrid();
        loadCours();

        // US14 — bouton visible uniquement pour le gestionnaire
        UserEntity u = SessionManager.getInstance().getUtilisateurConnecte();
        if (u != null && u.getRole() == Role.GESTIONNAIRE_PLANNING) {
            btnAjouterCours.setVisible(true);
            btnAjouterCours.setManaged(true);
        }
    }

    private void setupTabs() {
        ToggleGroup group = new ToggleGroup();
        tabMonEDT.setToggleGroup(group);
        tabTiers.setToggleGroup(group);
        tabSalle.setToggleGroup(group);
        tabMonEDT.setSelected(true);
        hideSelectorBar();
        tabTiers.setText("EDT classe");
    }

    // -------------------------------------------------------------------------
    //  Navigation semaines
    // -------------------------------------------------------------------------

    @FXML private void onPrecedent()  { currentMonday = currentMonday.minusWeeks(1); refresh(); }
    @FXML private void onSuivant()    { currentMonday = currentMonday.plusWeeks(1);  refresh(); }
    @FXML private void onAujourdHui() { currentMonday = LocalDate.now().with(DayOfWeek.MONDAY); refresh(); }

    private void refresh() {
        updateWeekLabel();
        buildGrid();
        loadCours();
    }

    private void updateWeekLabel() {
        LocalDate vendredi = currentMonday.plusDays(4);
        labelSemaine.setText("Semaine du "
                + currentMonday.format(FMT_JOUR) + " – "
                + vendredi.format(FMT_JOUR) + " "
                + currentMonday.getYear());
    }

    // -------------------------------------------------------------------------
    //  Onglets
    // -------------------------------------------------------------------------

    @FXML private void onTabMonEDT() {
        currentTab = TabMode.MON_EDT;
        hideSelectorBar();
        loadCours();
    }

    @FXML private void onTabTiers() {
        currentTab = TabMode.TIERS;
        comboSelector.setPromptText("Choisir une classe…");
        try {
            List<String> noms = groupeController.getAllGroupes().stream()
                    .map(g -> g.getNom())
                    .toList();
            comboSelector.getItems().setAll(noms);
        } catch (Exception e) {
            comboSelector.getItems().clear();
        }
        showSelectorBar();
        loadCours();
    }

    @FXML private void onTabSalle() {
        currentTab = TabMode.SALLE;
        comboSelector.setPromptText("Choisir une salle…");
        try {
            allSalles = salleController.getAllSalles();
            comboSelector.getItems().setAll(
                    allSalles.stream().map(SalleEntity::getNom).toList());
        } catch (Exception e) {
            allSalles = List.of();
            comboSelector.getItems().clear();
        }
        showSelectorBar();
        loadCours();
    }

    @FXML private void onSelectorChanged() { loadCours(); }

    private void showSelectorBar() { selectorBar.setVisible(true);  selectorBar.setManaged(true); }
    private void hideSelectorBar() {
        selectorBar.setVisible(false);
        selectorBar.setManaged(false);
        comboSelector.getSelectionModel().clearSelection();
        comboSelector.getItems().clear();
    }

    // -------------------------------------------------------------------------
    //  Construction de la grille (structure fixe)
    // -------------------------------------------------------------------------

    private void buildGrid() {
        gridContainer.getChildren().clear();
        gridContainer.setSpacing(0);
        gridContainer.getChildren().add(buildTimeColumn());
        for (int i = 0; i < 5; i++) {
            VBox col = buildDayColumn(i);
            HBox.setHgrow(col, Priority.ALWAYS);
            gridContainer.getChildren().add(col);
        }
    }

    private VBox buildTimeColumn() {
        VBox col = new VBox();
        col.setMinWidth(LARGEUR_HEURE);
        col.setMaxWidth(LARGEUR_HEURE);
        col.getStyleClass().add("time-column");
        Region header = new Region();
        header.setPrefHeight(HAUTEUR_HEADER);
        col.getChildren().add(header);
        for (int h = HEURE_DEBUT; h <= HEURE_FIN; h++) {
            Label lbl = new Label(String.format("%02dh00", h));
            lbl.getStyleClass().add("time-label");
            lbl.setPrefHeight(PX_PAR_HEURE);
            lbl.setAlignment(Pos.TOP_RIGHT);
            col.getChildren().add(lbl);
        }
        return col;
    }

    private VBox buildDayColumn(int dayIndex) {
        VBox col = new VBox();
        col.getStyleClass().add("day-column");
        LocalDate date  = currentMonday.plusDays(dayIndex);
        Label header    = new Label(JOURS[dayIndex] + " " + date.format(FMT_JOUR));
        header.getStyleClass().add("day-header");
        header.setPrefHeight(HAUTEUR_HEADER);
        header.setMaxWidth(Double.MAX_VALUE);
        header.setAlignment(Pos.CENTER);
        if (date.equals(LocalDate.now())) header.getStyleClass().add("day-header-today");

        StackPane body = new StackPane();
        body.getStyleClass().add("day-body");
        body.setPrefHeight(NB_HEURES * PX_PAR_HEURE);
        body.setId("dayBody_" + dayIndex);
        body.getChildren().add(buildDayGrid());

        Pane coursPane = new Pane();
        coursPane.setId("coursPane_" + dayIndex);
        coursPane.prefHeightProperty().bind(body.heightProperty());
        body.getChildren().add(coursPane);

        col.getChildren().addAll(header, body);
        VBox.setVgrow(body, Priority.ALWAYS);
        return col;
    }

    private VBox buildDayGrid() {
        VBox grid = new VBox();
        grid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        for (int h = 0; h < NB_HEURES; h++) {
            Region row = new Region();
            row.setPrefHeight(PX_PAR_HEURE);
            row.setMaxWidth(Double.MAX_VALUE);
            row.getStyleClass().add(h % 2 == 0 ? "grid-row-even" : "grid-row-odd");
            grid.getChildren().add(row);
        }
        return grid;
    }

    // -------------------------------------------------------------------------
    //  Chargement des cours via le back-end
    // -------------------------------------------------------------------------

    private void loadCours() {
        UserEntity u = SessionManager.getInstance().getUtilisateurConnecte();
        TabMode tab   = currentTab;
        LocalDate monday = currentMonday;
        String sel    = comboSelector.getValue();

        // Vider la grille immédiatement
        for (int i = 0; i < 5; i++) {
            Pane p = findCoursPane(i);
            if (p != null) p.getChildren().clear();
        }

        // Affiche le spinner
        loadingPane.setVisible(true);
        loadingPane.setManaged(true);

        // Requête BDD en arrière-plan pour ne pas bloquer l'UI
        Thread t = new Thread(() -> {
            List<CoursDisplay> cours;
            try {
                cours = switch (tab) {
                    case MON_EDT -> toDisplayList(edtController.getEmploiDuTempsConnecte(u, monday));
                    case TIERS   -> sel == null ? List.of()
                            : toDisplayList(edtController.getEmploiDuTempsGroupe(sel, monday));
                    case SALLE   -> sel == null ? List.of()
                            : allSalles.stream()
                                .filter(s -> s.getNom().equals(sel))
                                .findFirst()
                                .map(salle -> toDisplayList(edtController.getEmploiDuTempsSalle(salle, monday)))
                                .orElse(List.of());
                };
            } catch (Exception e) {
                cours = List.of();
            }

            final List<CoursDisplay> result = cours;
            Platform.runLater(() -> {
                loadingPane.setVisible(false);
                loadingPane.setManaged(false);
                displayCours(result);
            });
        });
        t.setDaemon(true);
        t.start();
    }

    private void displayCours(List<CoursDisplay> cours) {
        for (int i = 0; i < 5; i++) {
            Pane p = findCoursPane(i);
            if (p != null) p.getChildren().clear();
        }

        for (CoursDisplay c : cours) {
            int dayIndex = c.jour().getDayOfWeek().getValue() - 1;
            if (dayIndex < 0 || dayIndex > 4) continue;

            Pane coursPane = findCoursPane(dayIndex);
            if (coursPane == null) continue;

            VBox block    = buildCoursBlock(c);
            double top    = minutesFromStart(c.heureDebut()) / 60.0 * PX_PAR_HEURE;
            double height = minutesFromStart(c.heureFin())   / 60.0 * PX_PAR_HEURE - top - 2;

            block.setLayoutY(top);
            block.setPrefHeight(height);
            block.setLayoutX(4);
            block.setPrefWidth(Math.max(0, coursPane.getWidth() - 8));
            coursPane.widthProperty().addListener((obs, o, w) ->
                    block.setPrefWidth(w.doubleValue() - 8));
            coursPane.getChildren().add(block);
        }
    }

    private List<CoursDisplay> toDisplayList(List<CoursEntity> entities) {
        return entities.stream()
                .map(CoursDisplay::fromEntity)
                .filter(Objects::nonNull)
                .toList();
    }

    // -------------------------------------------------------------------------
    //  Construction du bloc visuel d'un cours
    // -------------------------------------------------------------------------

    private VBox buildCoursBlock(CoursDisplay c) {
        VBox block = new VBox(3);
        block.getStyleClass().add("cours-block");
        block.setAlignment(Pos.TOP_LEFT);

        TypeCours type = c.typeCours() != null ? c.typeCours() : TypeCours.CM;
        block.setStyle(
                "-fx-background-color: " + type.getCouleurFond() + ";"
              + "-fx-border-color: "     + type.getCouleurBordure() + ";"
              + "-fx-border-width: 0 0 0 4;"
              + "-fx-background-radius: 6; -fx-border-radius: 6;"
              + "-fx-padding: 6 8 6 10;");

        Label badge = new Label(type.getLibelle());
        badge.setStyle(
                "-fx-background-color: " + type.getCouleurBordure() + ";"
              + "-fx-text-fill: white; -fx-font-size: 10; -fx-font-weight: bold;"
              + "-fx-background-radius: 4; -fx-padding: 1 5 1 5;");

        Label nomLbl = new Label(c.nom() != null ? c.nom() : "Cours");
        nomLbl.getStyleClass().add("cours-block-nom");
        nomLbl.setWrapText(true);

        block.getChildren().addAll(badge, nomLbl);

        if (c.nomSalle()  != null) {
            Label l = new Label("\uD83D\uDCCD " + c.nomSalle());
            l.getStyleClass().add("cours-block-detail");
            block.getChildren().add(l);
        }
        if (c.nomGroupe() != null) {
            Label l = new Label("\uD83D\uDC65 " + c.nomGroupe());
            l.getStyleClass().add("cours-block-detail");
            block.getChildren().add(l);
        }
        if (c.nomProf()   != null) {
            Label l = new Label("\uD83D\uDC64 " + c.nomProf());
            l.getStyleClass().add("cours-block-detail");
            block.getChildren().add(l);
        }

        Tooltip tip = new Tooltip(buildTooltip(c));
        tip.setWrapText(true);
        tip.setMaxWidth(280);
        Tooltip.install(block, tip);

        UserEntity u = SessionManager.getInstance().getUtilisateurConnecte();
        // US13 — Annuler un cours : clic uniquement pour le prof sur son EDT
        if (u != null && u.getRole() == Role.PROFESSEUR && currentTab == TabMode.MON_EDT
                && type != TypeCours.ANNULE) {
            block.setStyle(block.getStyle() + "-fx-cursor: hand;");
            block.setOnMouseClicked(e -> onClickCours(c, block));
        }
        // US15 — Modifier un cours : clic uniquement pour le gestionnaire
        if (u != null && u.getRole() == Role.GESTIONNAIRE_PLANNING && type != TypeCours.ANNULE) {
            block.setStyle(block.getStyle() + "-fx-cursor: hand;");
            block.setOnMouseClicked(e -> onModifierCours(c, block));
        }

        return block;
    }

    private void onClickCours(CoursDisplay c, VBox block) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Annuler le cours");
        confirm.setHeaderText("Annuler « " + c.nom() + " » ?");
        confirm.setContentText(
                "Date : " + c.jour().format(DateTimeFormatter.ofPattern("EEEE d MMM", Locale.FRENCH))
                + "\nHoraire : " + c.heureDebut() + " – " + c.heureFin()
                + (c.nomSalle() != null ? "\nSalle : " + c.nomSalle() : "")
                + "\n\nCette action est irréversible.");

        ButtonType btnAnnuler = new ButtonType("Annuler le cours", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnGarder  = new ButtonType("Garder", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnAnnuler, btnGarder);

        confirm.showAndWait().ifPresent(result -> {
            if (result == btnAnnuler) {
                appliquerAnnulation(block);
            }
        });
    }

    private void appliquerAnnulation(VBox block) {
        // Visuel : fond gris barré
        block.setStyle(
                "-fx-background-color: #F3F4F6;"
              + "-fx-border-color: #9CA3AF;"
              + "-fx-border-width: 0 0 0 4;"
              + "-fx-background-radius: 6; -fx-border-radius: 6;"
              + "-fx-padding: 6 8 6 10; -fx-opacity: 0.6;");
        block.setOnMouseClicked(null);
        block.setStyle(block.getStyle() + "-fx-cursor: default;");

        // Met à jour le badge type en "ANNULÉ"
        block.getChildren().stream()
                .filter(n -> n instanceof Label && ((Label) n).getStyle().contains("-fx-background-color"))
                .findFirst()
                .ifPresent(n -> {
                    Label badge = (Label) n;
                    badge.setText("ANNULÉ");
                    badge.setStyle(
                            "-fx-background-color: #6B7280;"
                          + "-fx-text-fill: white; -fx-font-size: 10; -fx-font-weight: bold;"
                          + "-fx-background-radius: 4; -fx-padding: 1 5 1 5;");
                });
    }

    private String buildTooltip(CoursDisplay c) {
        return (c.nom()       != null ? c.nom()       + "\n" : "")
             + (c.typeCours() != null ? "Type : " + c.typeCours().getLibelle() + "\n" : "")
             + "Horaire : " + c.heureDebut() + " – " + c.heureFin() + "\n"
             + (c.nomSalle()  != null ? "Salle : "  + c.nomSalle()  + "\n" : "")
             + (c.nomGroupe() != null ? "Groupe : " + c.nomGroupe()         : "");
    }

    // -------------------------------------------------------------------------
    //  US14 — Ajouter un cours (gestionnaire, front uniquement)
    // -------------------------------------------------------------------------

    @FXML
    private void onAjouterCours() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un cours");
        dialog.setHeaderText("Nouveau cours dans l'EDT");

        ButtonType btnValider = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);

        // Formulaire
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new javafx.geometry.Insets(16));

        TextField fieldNom = new TextField();
        fieldNom.setPromptText("Ex : Algorithmique");

        ComboBox<String> comboType = new ComboBox<>();
        comboType.getItems().addAll("CM", "TD", "TP", "EXAMEN");
        comboType.setPromptText("Type");
        comboType.setPrefWidth(120);

        DatePicker datePicker = new DatePicker(currentMonday);

        ComboBox<String> comboDebut = new ComboBox<>();
        ComboBox<String> comboFin   = new ComboBox<>();
        List<String> slots = java.util.stream.Stream
                .iterate(LocalTime.of(7, 30), t -> !t.isAfter(LocalTime.of(19, 0)), t -> t.plusMinutes(30))
                .map(t -> t.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")))
                .toList();
        comboDebut.getItems().addAll(slots);
        comboFin.getItems().addAll(slots);
        comboDebut.setPromptText("Heure début");
        comboFin.setPromptText("Heure fin");
        comboDebut.setPrefWidth(120);
        comboFin.setPrefWidth(120);

        ComboBox<String> comboSalle = new ComboBox<>();
        try {
            salleController.getAllSalles().forEach(s -> comboSalle.getItems().add(s.getNom()));
        } catch (Exception ignored) {}
        comboSalle.setPromptText("Salle (optionnel)");

        ComboBox<String> comboGroupe = new ComboBox<>();
        try {
            groupeController.getAllGroupes().forEach(g -> comboGroupe.getItems().add(g.getNom()));
        } catch (Exception ignored) {}
        comboGroupe.setPromptText("Groupe (optionnel)");

        form.add(new Label("Nom du cours *"), 0, 0);   form.add(fieldNom,   1, 0);
        form.add(new Label("Type *"),          0, 1);   form.add(comboType,  1, 1);
        form.add(new Label("Date *"),          0, 2);   form.add(datePicker, 1, 2);
        form.add(new Label("Heure début *"),   0, 3);   form.add(comboDebut, 1, 3);
        form.add(new Label("Heure fin *"),     0, 4);   form.add(comboFin,   1, 4);
        form.add(new Label("Salle"),           0, 5);   form.add(comboSalle, 1, 5);
        form.add(new Label("Groupe"),          0, 6);   form.add(comboGroupe,1, 6);

        // Styles labels
        form.getChildren().stream()
                .filter(n -> n instanceof Label)
                .forEach(n -> ((Label) n).getStyleClass().add("form-label"));

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/projet/M1/css/main.css").toExternalForm());

        dialog.showAndWait().ifPresent(result -> {
            if (result != btnValider) return;

            String nom    = fieldNom.getText().trim();
            String type   = comboType.getValue();
            LocalDate date = datePicker.getValue();
            String debutStr = comboDebut.getValue();
            String finStr   = comboFin.getValue();

            if (nom.isEmpty() || type == null || date == null || debutStr == null || finStr == null) {
                Alert err = new Alert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs obligatoires (*).");
                err.setHeaderText(null);
                err.showAndWait();
                return;
            }

            LocalTime debut = LocalTime.parse(debutStr, java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime fin   = LocalTime.parse(finStr,   java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

            if (!fin.isAfter(debut)) {
                Alert err = new Alert(Alert.AlertType.WARNING, "L'heure de fin doit être après l'heure de début.");
                err.setHeaderText(null);
                err.showAndWait();
                return;
            }

            // Construire un CoursDisplay temporaire (visuel uniquement, pas de BDD)
            CoursDisplay nouveau = new CoursDisplay(
                    null,
                    nom,
                    TypeCours.fromString(type),
                    comboGroupe.getValue(),
                    null,
                    comboSalle.getValue(),
                    date,
                    debut,
                    fin
            );

            afficherCoursTemporaire(nouveau);
        });
    }

    private void afficherCoursTemporaire(CoursDisplay c) {
        int dayIndex = c.jour().getDayOfWeek().getValue() - 1;
        if (dayIndex < 0 || dayIndex > 4) return;

        // Vérifier que la date est dans la semaine affichée
        LocalDate lundi = currentMonday;
        LocalDate vendredi = currentMonday.plusDays(4);
        if (c.jour().isBefore(lundi) || c.jour().isAfter(vendredi)) {
            Alert info = new Alert(Alert.AlertType.INFORMATION,
                    "Le cours a été créé mais la date est hors de la semaine affichée.\nNaviguez vers la bonne semaine pour le voir.");
            info.setHeaderText(null);
            info.showAndWait();
            return;
        }

        Pane coursPane = findCoursPane(dayIndex);
        if (coursPane == null) return;

        VBox block    = buildCoursBlock(c);
        double top    = minutesFromStart(c.heureDebut()) / 60.0 * PX_PAR_HEURE;
        double height = minutesFromStart(c.heureFin())   / 60.0 * PX_PAR_HEURE - top - 2;

        block.setLayoutY(top);
        block.setPrefHeight(height);
        block.setLayoutX(4);
        block.setPrefWidth(Math.max(0, coursPane.getWidth() - 8));
        coursPane.widthProperty().addListener((obs, o, w) ->
                block.setPrefWidth(w.doubleValue() - 8));
        coursPane.getChildren().add(block);
    }

    // -------------------------------------------------------------------------
    //  US15 — Modifier un cours (gestionnaire, front uniquement)
    // -------------------------------------------------------------------------

    private void onModifierCours(CoursDisplay c, VBox block) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier le cours");
        dialog.setHeaderText("Modifier « " + c.nom() + " »");

        ButtonType btnValider = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new javafx.geometry.Insets(16));

        // Pré-rempli avec les valeurs actuelles
        TextField fieldNom = new TextField(c.nom() != null ? c.nom() : "");
        fieldNom.setPromptText("Nom du cours");

        ComboBox<String> comboType = new ComboBox<>();
        comboType.getItems().addAll("CM", "TD", "TP", "EXAMEN");
        comboType.setValue(c.typeCours() != null ? c.typeCours().name() : "CM");
        comboType.setPrefWidth(120);

        DatePicker datePicker = new DatePicker(c.jour());

        List<String> slots = java.util.stream.Stream
                .iterate(LocalTime.of(7, 30), t -> !t.isAfter(LocalTime.of(19, 0)), t -> t.plusMinutes(30))
                .map(t -> t.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")))
                .toList();

        ComboBox<String> comboDebut = new ComboBox<>();
        comboDebut.getItems().addAll(slots);
        comboDebut.setValue(c.heureDebut().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        comboDebut.setPrefWidth(120);

        ComboBox<String> comboFin = new ComboBox<>();
        comboFin.getItems().addAll(slots);
        comboFin.setValue(c.heureFin().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        comboFin.setPrefWidth(120);

        ComboBox<String> comboSalle = new ComboBox<>();
        try {
            salleController.getAllSalles().forEach(s -> comboSalle.getItems().add(s.getNom()));
        } catch (Exception ignored) {}
        comboSalle.setValue(c.nomSalle());
        comboSalle.setPromptText("Salle (optionnel)");

        ComboBox<String> comboGroupe = new ComboBox<>();
        try {
            groupeController.getAllGroupes().forEach(g -> comboGroupe.getItems().add(g.getNom()));
        } catch (Exception ignored) {}
        comboGroupe.setValue(c.nomGroupe());
        comboGroupe.setPromptText("Groupe (optionnel)");

        form.add(new Label("Nom du cours *"), 0, 0); form.add(fieldNom,    1, 0);
        form.add(new Label("Type *"),          0, 1); form.add(comboType,   1, 1);
        form.add(new Label("Date *"),          0, 2); form.add(datePicker,  1, 2);
        form.add(new Label("Heure début *"),   0, 3); form.add(comboDebut,  1, 3);
        form.add(new Label("Heure fin *"),     0, 4); form.add(comboFin,    1, 4);
        form.add(new Label("Salle"),           0, 5); form.add(comboSalle,  1, 5);
        form.add(new Label("Groupe"),          0, 6); form.add(comboGroupe, 1, 6);

        form.getChildren().stream()
                .filter(n -> n instanceof Label)
                .forEach(n -> ((Label) n).getStyleClass().add("form-label"));

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/projet/M1/css/main.css").toExternalForm());

        dialog.showAndWait().ifPresent(result -> {
            if (result != btnValider) return;

            String nom     = fieldNom.getText().trim();
            String type    = comboType.getValue();
            LocalDate date = datePicker.getValue();
            String debutStr = comboDebut.getValue();
            String finStr   = comboFin.getValue();

            if (nom.isEmpty() || type == null || date == null || debutStr == null || finStr == null) {
                Alert err = new Alert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs obligatoires (*).");
                err.setHeaderText(null);
                err.showAndWait();
                return;
            }

            LocalTime debut = LocalTime.parse(debutStr, java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime fin   = LocalTime.parse(finStr,   java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

            if (!fin.isAfter(debut)) {
                Alert err = new Alert(Alert.AlertType.WARNING, "L'heure de fin doit être après l'heure de début.");
                err.setHeaderText(null);
                err.showAndWait();
                return;
            }

            CoursDisplay modifie = new CoursDisplay(
                    c.id(),
                    nom,
                    TypeCours.fromString(type),
                    comboGroupe.getValue(),
                    c.nomProf(),
                    comboSalle.getValue(),
                    date,
                    debut,
                    fin
            );

            // Remplacer le bloc existant par le bloc mis à jour
            Pane parent = (Pane) block.getParent();
            if (parent != null) {
                parent.getChildren().remove(block);
            }
            afficherCoursTemporaire(modifie);
        });
    }

    // -------------------------------------------------------------------------
    //  Helpers
    // -------------------------------------------------------------------------

    private Pane findCoursPane(int dayIndex) {
        return (Pane) gridContainer.lookup("#coursPane_" + dayIndex);
    }

    private double minutesFromStart(LocalTime time) {
        return (time.getHour() - HEURE_DEBUT) * 60.0 + time.getMinute();
    }
}
