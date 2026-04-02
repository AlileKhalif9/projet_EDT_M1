package projet.M1.ui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import projet.M1.BDD.dao.CoursDAO;
import projet.M1.BDD.dao.GroupeDAO;
import projet.M1.BDD.dao.SalleDAO;
import projet.M1.BDD.entity.CoursEntity;
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
    @FXML private ToggleButton   tabMonEDT;
    @FXML private ToggleButton   tabTiers;
    @FXML private ToggleButton   tabSalle;
    @FXML private HBox           selectorBar;
    @FXML private ComboBox<String> comboSelector;
    @FXML private ScrollPane     scrollPane;
    @FXML private HBox           gridContainer;

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

        List<CoursDisplay> cours;
        try {
            cours = switch (currentTab) {
                case MON_EDT -> loadMonEDT(u);
                case TIERS   -> loadTiersEDT();
                case SALLE   -> loadSalleEDT();
            };
        } catch (Exception e) {
            cours = List.of();
        }

        for (int i = 0; i < 5; i++) {
            Pane p = findCoursPane(i);
            if (p != null) p.getChildren().clear();
        }

        for (CoursDisplay c : cours) {
            int dayIndex = c.jour().getDayOfWeek().getValue() - 1;
            if (dayIndex < 0 || dayIndex > 4) continue;

            Pane coursPane = findCoursPane(dayIndex);
            if (coursPane == null) continue;

            VBox block  = buildCoursBlock(c);
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

    private List<CoursDisplay> loadMonEDT(UserEntity u) {
        if (u == null) return List.of();
        return toDisplayList(edtController.getEmploiDuTempsConnecte(u, currentMonday));
    }

    private List<CoursDisplay> loadTiersEDT() {
        String sel = comboSelector.getValue();
        if (sel == null) return List.of();
        return toDisplayList(edtController.getEmploiDuTempsGroupe(sel, currentMonday));
    }

    private List<CoursDisplay> loadSalleEDT() {
        String sel = comboSelector.getValue();
        if (sel == null) return List.of();
        return allSalles.stream()
                .filter(s -> s.getNom().equals(sel))
                .findFirst()
                .map(salle -> toDisplayList(edtController.getEmploiDuTempsSalle(salle, currentMonday)))
                .orElse(List.of());
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

        return block;
    }

    private String buildTooltip(CoursDisplay c) {
        return (c.nom()       != null ? c.nom()       + "\n" : "")
             + (c.typeCours() != null ? "Type : " + c.typeCours().getLibelle() + "\n" : "")
             + "Horaire : " + c.heureDebut() + " – " + c.heureFin() + "\n"
             + (c.nomSalle()  != null ? "Salle : "  + c.nomSalle()  + "\n" : "")
             + (c.nomGroupe() != null ? "Groupe : " + c.nomGroupe()         : "");
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
