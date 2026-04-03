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

    private static final int    HEURE_DEBUT    = 8;
    private static final int    HEURE_FIN      = 19;
    private static final int    NB_HEURES      = HEURE_FIN - HEURE_DEBUT;
    private static final double PX_PAR_HEURE   = 80.0;
    private static final double HAUTEUR_HEADER = 44.0;
    private static final double LARGEUR_HEURE  = 64.0;

    private static final String[] JOURS = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi"};
    private static final DateTimeFormatter FMT_JOUR =
            DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH);

    // -------------------------------------------------------------------------
    //  Composants FXML
    // -------------------------------------------------------------------------

    @FXML private Label            labelSemaine;
    @FXML private Button           btnPrecedent;
    @FXML private Button           btnSuivant;
    @FXML private Button           btnAujourdHui;
    @FXML private Button           btnAjouterCours;
    @FXML private ToggleButton     tabMonEDT;
    @FXML private ToggleButton     tabTiers;
    @FXML private ToggleButton     tabSalle;
    @FXML private HBox             selectorBar;
    @FXML private ComboBox<String> comboSelector;
    @FXML private ScrollPane       scrollPane;
    @FXML private HBox             gridContainer;
    @FXML private StackPane        loadingPane;

    // -------------------------------------------------------------------------
    //  Back-end controllers
    // -------------------------------------------------------------------------

    private final EmploiDuTempsController edtController =
            new EmploiDuTempsController(new CoursDAO());
    private final SalleController  salleController  = new SalleController(new SalleDAO());
    private final GroupeController groupeController = new GroupeController(new GroupeDAO());

    // -------------------------------------------------------------------------
    //  État interne
    // -------------------------------------------------------------------------

    private LocalDate currentMonday;

    private final java.util.Map<Integer, List<CoursDisplay>> coursParJour = new java.util.HashMap<>();

    private enum TabMode { MON_EDT, TIERS, SALLE }
    private TabMode currentTab = TabMode.MON_EDT;

    private List<SalleEntity> allSalles = List.of();

    // -------------------------------------------------------------------------
    //  Initialisation
    // -------------------------------------------------------------------------

    @FXML
    public void initialize() {
        currentMonday = LocalDate.now().with(DayOfWeek.MONDAY);

        UserEntity u = SessionManager.getInstance().getUtilisateurConnecte();
        boolean isGestionnaire = u != null && u.getRole() == Role.GESTIONNAIRE_PLANNING;

        // US14 — bouton "Ajouter un cours" visible uniquement pour le gestionnaire
        btnAjouterCours.setVisible(isGestionnaire);
        btnAjouterCours.setManaged(isGestionnaire);

        // Le gestionnaire n'a pas d'EDT personnel : on masque l'onglet "Mon EDT"
        if (isGestionnaire) {
            tabMonEDT.setVisible(false);
            tabMonEDT.setManaged(false);
            // Démarrer directement sur l'onglet "EDT classe"
            currentTab = TabMode.TIERS;
        }

        setupTabs(isGestionnaire);
        updateWeekLabel();
        buildGrid();
        loadCours();
    }

    private void setupTabs(boolean isGestionnaire) {
        ToggleGroup group = new ToggleGroup();
        tabMonEDT.setToggleGroup(group);
        tabTiers.setToggleGroup(group);
        tabSalle.setToggleGroup(group);
        tabTiers.setText("EDT classe");

        if (isGestionnaire) {
            // Sélectionner EDT classe par défaut et afficher le sélecteur de groupe
            tabTiers.setSelected(true);
            try {
                comboSelector.setPromptText("Choisir une classe…");
                comboSelector.getItems().setAll(
                        groupeController.getAllGroupes().stream().map(g -> g.getNom()).toList());
            } catch (Exception ignored) {}
            showSelectorBar();
        } else {
            tabMonEDT.setSelected(true);
            hideSelectorBar();
        }
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
            comboSelector.getItems().setAll(
                    groupeController.getAllGroupes().stream().map(g -> g.getNom()).toList());
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
    //  Construction de la grille
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
        LocalDate date = currentMonday.plusDays(dayIndex);
        Label header   = new Label(JOURS[dayIndex] + " " + date.format(FMT_JOUR));
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
    //  Chargement des cours
    // -------------------------------------------------------------------------

    private void loadCours() {
        UserEntity u  = SessionManager.getInstance().getUtilisateurConnecte();
        TabMode tab   = currentTab;
        LocalDate monday = currentMonday;
        String sel    = comboSelector.getValue();

        for (int i = 0; i < 5; i++) {
            Pane p = findCoursPane(i);
            if (p != null) p.getChildren().clear();
        }

        // Gestionnaire sur EDT classe sans groupe sélectionné :
        // grille vide, pas de spinner (l'utilisateur doit d'abord sélectionner un groupe)
        if (tab == TabMode.TIERS && (sel == null || sel.isBlank())) {
            loadingPane.setVisible(false);
            loadingPane.setManaged(false);
            displayCours(List.of());
            return;
        }

        loadingPane.setVisible(true);
        loadingPane.setManaged(true);

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
        coursParJour.clear();
        for (int i = 0; i < 5; i++) {
            Pane p = findCoursPane(i);
            if (p != null) p.getChildren().clear();
        }
        for (CoursDisplay c : cours) {
            int dayIndex = c.jour().getDayOfWeek().getValue() - 1;
            if (dayIndex < 0 || dayIndex > 4) continue;
            coursParJour.computeIfAbsent(dayIndex, k -> new java.util.ArrayList<>()).add(c);
        }
        for (int i = 0; i < 5; i++) renderDay(i);
    }

    private void renderDay(int dayIndex) {
        Pane coursPane = findCoursPane(dayIndex);
        if (coursPane == null) return;
        coursPane.getChildren().clear();

        List<CoursDisplay> jourCours = coursParJour.getOrDefault(dayIndex, List.of());
        int n = jourCours.size();
        if (n == 0) return;

        int[] colonne    = new int[n];
        int[] nbColonnes = new int[n];

        for (int i = 0; i < n; i++) {
            java.util.Set<Integer> colonnesUtilisees = new java.util.HashSet<>();
            for (int j = 0; j < i; j++) {
                if (seChevauche(jourCours.get(i), jourCours.get(j)))
                    colonnesUtilisees.add(colonne[j]);
            }
            int col = 0;
            while (colonnesUtilisees.contains(col)) col++;
            colonne[i] = col;
            for (int j = 0; j < n; j++) {
                if (seChevauche(jourCours.get(i), jourCours.get(j)))
                    nbColonnes[j] = Math.max(nbColonnes[j], col + 1);
            }
        }

        for (int i = 0; i < n; i++) {
            CoursDisplay c  = jourCours.get(i);
            final int col   = colonne[i];
            final int total = Math.max(nbColonnes[i], 1);

            VBox block  = buildCoursBlock(c);
            double top    = minutesFromStart(c.heureDebut()) / 60.0 * PX_PAR_HEURE;
            double height = minutesFromStart(c.heureFin())   / 60.0 * PX_PAR_HEURE - top - 2;
            block.setLayoutY(top);
            block.setPrefHeight(height);

            coursPane.widthProperty().addListener((obs, o, w) -> {
                double colW = (w.doubleValue() - 4) / total;
                block.setLayoutX(4 + col * colW);
                block.setPrefWidth(colW - 4);
            });
            double paneW = coursPane.getWidth() - 4;
            if (paneW > 0) {
                double colW = paneW / total;
                block.setLayoutX(4 + col * colW);
                block.setPrefWidth(colW - 4);
            } else {
                block.setLayoutX(4);
                block.setPrefWidth(100);
            }
            coursPane.getChildren().add(block);
        }
    }

    private boolean seChevauche(CoursDisplay a, CoursDisplay b) {
        return a.heureDebut().isBefore(b.heureFin()) && b.heureDebut().isBefore(a.heureFin());
    }

    private List<CoursDisplay> toDisplayList(List<CoursEntity> entities) {
        return entities.stream()
                .map(CoursDisplay::fromEntity)
                .filter(Objects::nonNull)
                .toList();
    }

    // -------------------------------------------------------------------------
    //  Construction du bloc visuel
    // -------------------------------------------------------------------------

    private VBox buildCoursBlock(CoursDisplay c) {
        VBox block = new VBox(3);
        block.getStyleClass().add("cours-block");
        block.setAlignment(Pos.TOP_LEFT);

        TypeCours type = c.typeCours() != null ? c.typeCours() : TypeCours.CM;

        appliquerStyleNormal(block, type);

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

        // US13 — Professeur sur son EDT : toggle annulation/réactivation (pas l'invité)
        if (u != null && u.getRole() == Role.PROFESSEUR && currentTab == TabMode.MON_EDT) {
            block.setStyle(block.getStyle() + "-fx-cursor: hand;");
            if (type == TypeCours.ANNULE) {
                appliquerStyleAnnule(block, badge);
                block.setOnMouseClicked(e -> onReactiverCours(c, block, badge));
            } else {
                block.setOnMouseClicked(e -> onAnnulerCours(c, block, badge, type));
            }
        }

        // US15 — Gestionnaire : modifier au clic
        if (u != null && u.getRole() == Role.GESTIONNAIRE_PLANNING && type != TypeCours.ANNULE) {
            block.setStyle(block.getStyle() + "-fx-cursor: hand;");
            block.setOnMouseClicked(e -> onModifierCours(c, block));
        }

        return block;
    }

    // -------------------------------------------------------------------------
    //  US13 — Annuler / Réactiver un cours (professeur)
    // -------------------------------------------------------------------------

    private void onAnnulerCours(CoursDisplay c, VBox block, Label badge, TypeCours typeOrigine) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Annuler le cours");
        confirm.setHeaderText("Annuler « " + c.nom() + " » ?");
        confirm.setContentText(
                "Date : " + c.jour().format(DateTimeFormatter.ofPattern("EEEE d MMM", Locale.FRENCH))
                        + "\nHoraire : " + c.heureDebut() + " – " + c.heureFin()
                        + (c.nomSalle() != null ? "\nSalle : " + c.nomSalle() : "")
                        + "\n\nVous pourrez réactiver ce cours en recliquant dessus.");

        ButtonType btnAnnuler = new ButtonType("Annuler le cours", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnGarder  = new ButtonType("Garder",           ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnAnnuler, btnGarder);

        confirm.showAndWait().ifPresent(result -> {
            if (result != btnAnnuler) return;
            try {
                edtController.annulerCours(c.id());
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR,
                        "Impossible d'annuler le cours en base de données.").showAndWait();
                return;
            }
            appliquerStyleAnnule(block, badge);
            block.setOnMouseClicked(e -> onReactiverCours(c, block, badge, typeOrigine));
        });
    }

    private void onReactiverCours(CoursDisplay c, VBox block, Label badge) {
        onReactiverCours(c, block, badge, TypeCours.CM);
    }

    private void onReactiverCours(CoursDisplay c, VBox block, Label badge, TypeCours typeOrigine) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Réactiver le cours");
        confirm.setHeaderText("Réactiver « " + c.nom() + " » ?");
        confirm.setContentText(
                "Date : " + c.jour().format(DateTimeFormatter.ofPattern("EEEE d MMM", Locale.FRENCH))
                        + "\nHoraire : " + c.heureDebut() + " – " + c.heureFin()
                        + "\n\nLe cours sera de nouveau visible pour les étudiants.");

        ButtonType btnReactiver = new ButtonType("Réactiver", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnAnnuler   = new ButtonType("Annuler",   ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnReactiver, btnAnnuler);

        confirm.showAndWait().ifPresent(result -> {
            if (result != btnReactiver) return;
            try {
                edtController.reactiverCours(c.id(), typeOrigine.name());
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR,
                        "Impossible de réactiver le cours en base de données.").showAndWait();
                return;
            }
            appliquerStyleNormal(block, typeOrigine);
            badge.setText(typeOrigine.getLibelle());
            badge.setStyle(
                    "-fx-background-color: " + typeOrigine.getCouleurBordure() + ";"
                            + "-fx-text-fill: white; -fx-font-size: 10; -fx-font-weight: bold;"
                            + "-fx-background-radius: 4; -fx-padding: 1 5 1 5;");
            block.setOnMouseClicked(e -> onAnnulerCours(c, block, badge, typeOrigine));
        });
    }

    // -------------------------------------------------------------------------
    //  Helpers visuels annulation
    // -------------------------------------------------------------------------

    private void appliquerStyleNormal(VBox block, TypeCours type) {
        block.setStyle(
                "-fx-background-color: " + type.getCouleurFond() + ";"
                        + "-fx-border-color: "     + type.getCouleurBordure() + ";"
                        + "-fx-border-width: 0 0 0 4;"
                        + "-fx-background-radius: 6; -fx-border-radius: 6;"
                        + "-fx-padding: 6 8 6 10; -fx-opacity: 1.0;");
    }

    private void appliquerStyleAnnule(VBox block, Label badge) {
        block.setStyle(
                "-fx-background-color: #F3F4F6;"
                        + "-fx-border-color: #9CA3AF;"
                        + "-fx-border-width: 0 0 0 4;"
                        + "-fx-background-radius: 6; -fx-border-radius: 6;"
                        + "-fx-padding: 6 8 6 10; -fx-opacity: 0.6;"
                        + "-fx-cursor: hand;");
        badge.setText("ANNULÉ");
        badge.setStyle(
                "-fx-background-color: #6B7280;"
                        + "-fx-text-fill: white; -fx-font-size: 10; -fx-font-weight: bold;"
                        + "-fx-background-radius: 4; -fx-padding: 1 5 1 5;");
    }

    // -------------------------------------------------------------------------
    //  US14 — Ajouter un cours (gestionnaire)
    // -------------------------------------------------------------------------

    @FXML
    private void onAjouterCours() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un cours");
        dialog.setHeaderText("Nouveau cours dans l'EDT");

        ButtonType btnValider = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);

        // Pré-remplir le groupe avec celui sélectionné dans le sélecteur (onglet EDT classe)
        String groupePreselectionne = (currentTab == TabMode.TIERS)
                ? comboSelector.getValue() : null;
        GridPane form = buildFormulaireCours(null, groupePreselectionne);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/projet/M1/css/main.css").toExternalForm());

        TextField        fieldNom    = (TextField)        form.getUserData();
        ComboBox<String> comboType   = (ComboBox<String>) lookup(form, 1);
        DatePicker       datePicker  = (DatePicker)       lookup(form, 2);
        ComboBox<String> comboDebut  = (ComboBox<String>) lookup(form, 3);
        ComboBox<String> comboFin    = (ComboBox<String>) lookup(form, 4);
        ComboBox<String> comboSalle  = (ComboBox<String>) lookup(form, 5);
        ComboBox<String> comboGroupe = (ComboBox<String>) lookup(form, 6);

        dialog.showAndWait().ifPresent(result -> {
            if (result != btnValider) return;

            String nom      = fieldNom.getText().trim();
            String type     = comboType.getValue();
            LocalDate date  = datePicker.getValue();
            String debutStr = comboDebut.getValue();
            String finStr   = comboFin.getValue();
            String salle    = comboSalle.getValue();
            String groupe   = comboGroupe.getValue();

            if (!validerChamps(nom, type, date, debutStr, finStr, salle, groupe)) return;

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime debut = LocalTime.parse(debutStr, fmt);
            LocalTime fin   = LocalTime.parse(finStr,   fmt);
            if (!validerHeures(debut, fin)) return;

            CoursEntity saved;
            try {
                saved = edtController.ajouterCours(nom, type, date, debut, fin, salle, groupe);
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR,
                        "Impossible d'enregistrer le cours en base de données.").showAndWait();
                return;
            }

            CoursDisplay nouveau = new CoursDisplay(
                    saved.getId(), nom, TypeCours.fromString(type),
                    groupe, null, salle, date, debut, fin);
            afficherCoursTemporaire(nouveau);
        });
    }

    // -------------------------------------------------------------------------
    //  US15 — Modifier un cours (gestionnaire)
    // -------------------------------------------------------------------------

    private void onModifierCours(CoursDisplay c, VBox block) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier le cours");
        dialog.setHeaderText("Modifier « " + c.nom() + " »");

        ButtonType btnValider = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);

        GridPane form = buildFormulaireCours(c, null);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/projet/M1/css/main.css").toExternalForm());

        TextField        fieldNom    = (TextField)        form.getUserData();
        ComboBox<String> comboType   = (ComboBox<String>) lookup(form, 1);
        DatePicker       datePicker  = (DatePicker)       lookup(form, 2);
        ComboBox<String> comboDebut  = (ComboBox<String>) lookup(form, 3);
        ComboBox<String> comboFin    = (ComboBox<String>) lookup(form, 4);
        ComboBox<String> comboSalle  = (ComboBox<String>) lookup(form, 5);
        ComboBox<String> comboGroupe = (ComboBox<String>) lookup(form, 6);

        dialog.showAndWait().ifPresent(result -> {
            if (result != btnValider) return;

            String nom      = fieldNom.getText().trim();
            String type     = comboType.getValue();
            LocalDate date  = datePicker.getValue();
            String debutStr = comboDebut.getValue();
            String finStr   = comboFin.getValue();
            String salle    = comboSalle.getValue();
            String groupe   = comboGroupe.getValue();

            if (!validerChamps(nom, type, date, debutStr, finStr, salle, groupe)) return;

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime debut = LocalTime.parse(debutStr, fmt);
            LocalTime fin   = LocalTime.parse(finStr,   fmt);
            if (!validerHeures(debut, fin)) return;

            CoursEntity saved;
            try {
                saved = edtController.modifierCours(
                        c.id(), nom, type, date, debut, fin, salle, groupe);
            } catch (IllegalArgumentException ex) {
                new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
                return;
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR,
                        "Impossible de modifier le cours en base de données.").showAndWait();
                return;
            }

            CoursDisplay modifie = new CoursDisplay(
                    saved.getId(), nom, TypeCours.fromString(type),
                    groupe, c.nomProf(), salle, date, debut, fin);

            retirerCours(c);
            if (!afficherCoursTemporaire(modifie)) afficherCoursTemporaire(c);
        });
    }

    // -------------------------------------------------------------------------
    //  Formulaire partagé ajouter/modifier
    // -------------------------------------------------------------------------

    /**
     * Construit le formulaire ajouter/modifier.
     * @param prefill           cours à pré-remplir (null = formulaire vide)
     * @param groupePreselectionne groupe à pré-sélectionner quand prefill est null
     *                             (typiquement le groupe sélectionné dans le comboSelector)
     */
    private GridPane buildFormulaireCours(CoursDisplay prefill, String groupePreselectionne) {
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new javafx.geometry.Insets(16));

        DateTimeFormatter fmtSlot = DateTimeFormatter.ofPattern("HH:mm");
        List<String> slots = buildTimeSlots();

        TextField fieldNom = new TextField(prefill != null && prefill.nom() != null ? prefill.nom() : "");
        fieldNom.setPromptText("Ex : Algorithmique");
        form.setUserData(fieldNom);

        ComboBox<String> comboType = new ComboBox<>();
        comboType.getItems().addAll("CM", "TD", "TP", "EXAMEN");
        comboType.setValue(prefill != null && prefill.typeCours() != null
                ? prefill.typeCours().name() : null);
        comboType.setPromptText("Type");
        comboType.setPrefWidth(120);

        DatePicker datePicker = new DatePicker(
                prefill != null ? prefill.jour() : currentMonday);

        ComboBox<String> comboDebut = new ComboBox<>();
        comboDebut.getItems().addAll(slots);
        comboDebut.setValue(prefill != null ? prefill.heureDebut().format(fmtSlot) : null);
        comboDebut.setPromptText("Heure début");
        comboDebut.setPrefWidth(120);

        ComboBox<String> comboFin = new ComboBox<>();
        comboFin.getItems().addAll(slots);
        comboFin.setValue(prefill != null ? prefill.heureFin().format(fmtSlot) : null);
        comboFin.setPromptText("Heure fin");
        comboFin.setPrefWidth(120);

        // Salle — obligatoire
        ComboBox<String> comboSalle = new ComboBox<>();
        try { salleController.getAllSalles().forEach(s -> comboSalle.getItems().add(s.getNom())); }
        catch (Exception ignored) {}
        comboSalle.setValue(prefill != null ? prefill.nomSalle() : null);
        comboSalle.setPromptText("Choisir une salle");

        // Groupe — obligatoire, pré-sélectionné si un groupe est actif dans l'onglet EDT classe
        ComboBox<String> comboGroupe = new ComboBox<>();
        try { groupeController.getAllGroupes().forEach(g -> comboGroupe.getItems().add(g.getNom())); }
        catch (Exception ignored) {}
        if (prefill != null) {
            comboGroupe.setValue(prefill.nomGroupe());
        } else if (groupePreselectionne != null) {
            comboGroupe.setValue(groupePreselectionne);
        }
        comboGroupe.setPromptText("Choisir un groupe");

        // Labels — * sur tous les champs obligatoires dont salle et groupe
        form.add(new Label("Nom du cours *"), 0, 0); form.add(fieldNom,    1, 0);
        form.add(new Label("Type *"),          0, 1); form.add(comboType,   1, 1);
        form.add(new Label("Date *"),          0, 2); form.add(datePicker,  1, 2);
        form.add(new Label("Heure début *"),   0, 3); form.add(comboDebut,  1, 3);
        form.add(new Label("Heure fin *"),     0, 4); form.add(comboFin,    1, 4);
        form.add(new Label("Salle *"),         0, 5); form.add(comboSalle,  1, 5);
        form.add(new Label("Groupe *"),        0, 6); form.add(comboGroupe, 1, 6);

        form.getChildren().stream()
                .filter(n -> n instanceof Label)
                .forEach(n -> ((Label) n).getStyleClass().add("form-label"));

        return form;
    }

    @SuppressWarnings("unchecked")
    private <T> T lookup(GridPane grid, int row) {
        return (T) grid.getChildren().stream()
                .filter(n -> GridPane.getColumnIndex(n) != null && GridPane.getColumnIndex(n) == 1
                        && GridPane.getRowIndex(n)    != null && GridPane.getRowIndex(n)    == row)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Composant introuvable ligne " + row));
    }

    // -------------------------------------------------------------------------
    //  Helpers affichage temporaire
    // -------------------------------------------------------------------------

    private boolean afficherCoursTemporaire(CoursDisplay c) {
        LocalDate vendredi = currentMonday.plusDays(4);
        if (c.jour().isBefore(currentMonday) || c.jour().isAfter(vendredi)) {
            new Alert(Alert.AlertType.INFORMATION,
                    "Le cours a été enregistré mais la date est hors de la semaine affichée.\n"
                            + "Naviguez vers la bonne semaine pour le voir.").showAndWait();
            return false;
        }
        int dayIndex = c.jour().getDayOfWeek().getValue() - 1;
        if (dayIndex < 0 || dayIndex > 4) return false;

        List<CoursDisplay> existing = coursParJour.getOrDefault(dayIndex, List.of());
        for (CoursDisplay other : existing) {
            if (seChevauche(c, other)) {
                Alert err = new Alert(Alert.AlertType.WARNING);
                err.setTitle("Créneau occupé");
                err.setHeaderText("Conflit d'horaire");
                err.setContentText("Le créneau " + c.heureDebut() + " – " + c.heureFin()
                        + " est déjà occupé par « " + other.nom() + " ».\nChoisissez un autre horaire.");
                err.showAndWait();
                return false;
            }
        }
        coursParJour.computeIfAbsent(dayIndex, k -> new java.util.ArrayList<>()).add(c);
        renderDay(dayIndex);
        return true;
    }

    private void retirerCours(CoursDisplay c) {
        int dayIndex = c.jour().getDayOfWeek().getValue() - 1;
        if (dayIndex < 0 || dayIndex > 4) return;
        List<CoursDisplay> liste = coursParJour.get(dayIndex);
        if (liste != null) liste.removeIf(x -> x == c);
        renderDay(dayIndex);
    }

    // -------------------------------------------------------------------------
    //  Validation formulaire
    // -------------------------------------------------------------------------

    private boolean validerChamps(String nom, String type, LocalDate date,
                                  String debutStr, String finStr,
                                  String salle, String groupe) {
        if (nom.isEmpty() || type == null || date == null || debutStr == null || finStr == null
                || salle == null || salle.isBlank()
                || groupe == null || groupe.isBlank()) {
            new Alert(Alert.AlertType.WARNING,
                    "Veuillez remplir tous les champs obligatoires (*) :\n"
                            + "nom, type, date, horaires, salle et groupe.").showAndWait();
            return false;
        }
        return true;
    }

    private boolean validerHeures(LocalTime debut, LocalTime fin) {
        if (!fin.isAfter(debut)) {
            new Alert(Alert.AlertType.WARNING,
                    "L'heure de fin doit être après l'heure de début.").showAndWait();
            return false;
        }
        return true;
    }

    // -------------------------------------------------------------------------
    //  Helpers généraux
    // -------------------------------------------------------------------------

    private String buildTooltip(CoursDisplay c) {
        return (c.nom()       != null ? c.nom()       + "\n" : "")
                + (c.typeCours() != null ? "Type : " + c.typeCours().getLibelle() + "\n" : "")
                + "Horaire : " + c.heureDebut() + " – " + c.heureFin() + "\n"
                + (c.nomSalle()  != null ? "Salle : "  + c.nomSalle()  + "\n" : "")
                + (c.nomGroupe() != null ? "Groupe : " + c.nomGroupe()         : "");
    }

    private List<String> buildTimeSlots() {
        return java.util.stream.Stream
                .iterate(LocalTime.of(7, 30), t -> !t.isAfter(LocalTime.of(19, 0)), t -> t.plusMinutes(30))
                .map(t -> t.format(DateTimeFormatter.ofPattern("HH:mm")))
                .toList();
    }

    private Pane findCoursPane(int dayIndex) {
        return (Pane) gridContainer.lookup("#coursPane_" + dayIndex);
    }

    private double minutesFromStart(LocalTime time) {
        return (time.getHour() - HEURE_DEBUT) * 60.0 + time.getMinute();
    }
}