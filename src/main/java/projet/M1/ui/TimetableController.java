package projet.M1.ui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import projet.M1.model.academique.Groupe_Etudiant;
import projet.M1.model.planning.TypeCours;
import projet.M1.model.utilisateur_systeme.Professeur;
import projet.M1.model.utilisateur_systeme.Utilisateur;
import projet.M1.service.MockDataService;
import projet.M1.session.SessionManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Controller de la page EDT — fichier FXML : timetable.fxml
 *
 * C'est la page principale de l'appli, elle couvre US2 à US5 :
 *   US2 – Mon EDT (onglet par défaut)
 *   US3 – EDT d'un autre utilisateur (onglet "Tiers")
 *   US4 – EDT d'une salle (onglet "Salle")
 *   US5 – Pour les profs : l'onglet "Tiers" s'appelle "EDT classe"
 *
 * La grille est construite entièrement en Java (dans buildGrid/buildDayColumn)
 * parce que son contenu change selon la semaine et l'onglet.
 * Chaque cours est positionné via layoutY = (heureDebut - 8h) × 80px.
 *
 * Les données viennent de MockDataService (fictives pour l'instant).
 * Pour brancher la vraie BDD, remplacer les appels MockDataService dans loadCours().
 */
public class TimetableController {

    // -------------------------------------------------------------------------
    //  Constantes de mise en page de la grille
    // -------------------------------------------------------------------------

    private static final int HEURE_DEBUT = 8;   // 8h00
    private static final int HEURE_FIN = 19;  // 19h00
    private static final int NB_HEURES = HEURE_FIN - HEURE_DEBUT; // 11 lignes
    private static final double PX_PAR_HEURE = 80.0; // hauteur d'une heure en pixels
    private static final double HAUTEUR_HEADER = 44.0; // hauteur de l'en-tête jour
    private static final double LARGEUR_HEURE  = 64.0; // largeur de la colonne d'heures

    private static final String[] JOURS = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi"};
    private static final DateTimeFormatter FMT_JOUR =
            DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH);

    // -------------------------------------------------------------------------
    //  Composants FXML (liés via fx:id dans timetable.fxml)
    // -------------------------------------------------------------------------

    @FXML private Label        labelSemaine;   // "Semaine du 17-21 mars 2026"
    @FXML private Button       btnPrecedent;   // ◀ semaine précédente
    @FXML private Button       btnSuivant;     // ▶ semaine suivante
    @FXML private Button       btnAujourdHui;  // retour à la semaine courante

    @FXML private ToggleButton tabMonEDT;      // onglet "Mon EDT" (US2)
    @FXML private ToggleButton tabTiers;       // onglet "Tiers" (US3) ou "EDT classe" (US5)
    @FXML private ToggleButton tabSalle;       // onglet "Salle" (US4)
    @FXML private HBox         selectorBar;    // barre de sélection (masquée par défaut)
    @FXML private ComboBox<String> comboSelector; // liste déroulante tiers/salles

    @FXML private ScrollPane   scrollPane;     // zone scrollable contenant la grille
    @FXML private HBox         gridContainer;  // conteneur rempli dynamiquement

    // -------------------------------------------------------------------------
    //  État interne
    // -------------------------------------------------------------------------

    /** Lundi de la semaine actuellement affichée. */
    private LocalDate currentMonday;

    /** Mode d'affichage actif (Mon EDT / Tiers / Salle). */
    private enum TabMode { MON_EDT, TIERS, SALLE }
    private TabMode currentTab = TabMode.MON_EDT;

    // -------------------------------------------------------------------------
    //  Initialisation
    // -------------------------------------------------------------------------

    /** Appelé automatiquement par JavaFX après le chargement du FXML. */
    @FXML
    public void initialize() {
        // On démarre sur la semaine en cours
        currentMonday = LocalDate.now().with(DayOfWeek.MONDAY);
        setupTabs();
        updateWeekLabel();
        buildGrid();   // Construit la structure de la grille (colonnes, fond)
        loadCours();   // Charge et place les blocs de cours dans la grille
    }

    /** Configure les onglets de vue et adapte leur libellé selon le rôle. */
    private void setupTabs() {
        ToggleGroup group = new ToggleGroup();
        tabMonEDT.setToggleGroup(group);
        tabTiers.setToggleGroup(group);
        tabSalle.setToggleGroup(group);
        tabMonEDT.setSelected(true);
        hideSelectorBar(); // La barre de sélection est masquée par défaut (US2)

        // Tous les utilisateurs peuvent voir l'EDT d'une promo
        tabTiers.setText("EDT classe");
    }

    // -------------------------------------------------------------------------
    //  Navigation entre les semaines
    // -------------------------------------------------------------------------

    /** Passe à la semaine précédente. */
    @FXML private void onPrecedent()  { currentMonday = currentMonday.minusWeeks(1); refresh(); }

    /** Passe à la semaine suivante. */
    @FXML private void onSuivant()    { currentMonday = currentMonday.plusWeeks(1);  refresh(); }

    /** Revient à la semaine contenant aujourd'hui. */
    @FXML private void onAujourdHui() { currentMonday = LocalDate.now().with(DayOfWeek.MONDAY); refresh(); }

    /** Met à jour le label et recharge les cours pour la nouvelle semaine. */
    private void refresh() {
        updateWeekLabel();
        loadCours();
    }

    /** Met à jour le label "Semaine du X – Y". */
    private void updateWeekLabel() {
        LocalDate vendredi = currentMonday.plusDays(4);
        labelSemaine.setText("Semaine du "
                + currentMonday.format(FMT_JOUR) + " – "
                + vendredi.format(FMT_JOUR) + " "
                + currentMonday.getYear());
    }

    // -------------------------------------------------------------------------
    //  Gestion des onglets de vue
    // -------------------------------------------------------------------------

    /**
     * US2 – Mode "Mon EDT" : affiche les cours de l'utilisateur connecté.
     * La barre de sélection est masquée (pas besoin de choisir).
     */
    @FXML private void onTabMonEDT() {
        currentTab = TabMode.MON_EDT;
        hideSelectorBar();
        loadCours();
    }

    /**
     * US3 (Étudiant/Gestionnaire) – Mode "Tiers" : choisir un autre utilisateur.
     * US5 (Professeur) – Mode "EDT classe" : choisir une de ses classes/groupes.
     * Affiche la barre de sélection avec la liste appropriée.
     */
    @FXML private void onTabTiers() {
        currentTab = TabMode.TIERS;

        // Tous les rôles voient la liste des promotions
        List<String> choices = MockDataService.getInstance().getAllGroupes()
                .stream().map(Groupe_Etudiant::getNom).toList();
        comboSelector.setPromptText("Choisir une classe…");
        comboSelector.getItems().setAll(choices);

        showSelectorBar();
        loadCours();
    }

    /**
     * US4 – Mode "Salle" : affiche les cours planifiés dans une salle.
     * La liste déroulante propose toutes les salles disponibles.
     */
    @FXML private void onTabSalle() {
        currentTab = TabMode.SALLE;
        List<String> salles = MockDataService.getInstance().getAllSalles()
                .stream().map(s -> s.getNom()).toList();
        comboSelector.getItems().setAll(salles);
        comboSelector.setPromptText("Choisir une salle…");
        showSelectorBar();
        loadCours();
    }

    /** Déclenché quand l'utilisateur choisit une valeur dans la liste déroulante. */
    @FXML private void onSelectorChanged() { loadCours(); }

    private void showSelectorBar() { selectorBar.setVisible(true);  selectorBar.setManaged(true);  }
    private void hideSelectorBar() {
        selectorBar.setVisible(false);
        selectorBar.setManaged(false);
        comboSelector.getSelectionModel().clearSelection();
        comboSelector.getItems().clear();
    }

    // -------------------------------------------------------------------------
    //  Construction de la grille (structure fixe, bâtie une seule fois)
    // -------------------------------------------------------------------------

    /**
     * Construit la structure complète de la grille :
     *   colonne heures + 5 colonnes jours (lundi à vendredi).
     * Les cours sont ajoutés/mis à jour séparément dans loadCours().
     */
    private void buildGrid() {
        gridContainer.getChildren().clear();
        gridContainer.setSpacing(0);

        // Première colonne : les heures (8h00, 9h00, … 19h00)
        gridContainer.getChildren().add(buildTimeColumn());

        // Une colonne par jour de la semaine
        for (int i = 0; i < 5; i++) {
            VBox col = buildDayColumn(i);
            HBox.setHgrow(col, Priority.ALWAYS); // chaque colonne prend la largeur disponible
            gridContainer.getChildren().add(col);
        }
    }

    /**
     * Crée la colonne de gauche avec les labels d'heure.
     * Hauteur de chaque label = PX_PAR_HEURE (80px).
     */
    private VBox buildTimeColumn() {
        VBox col = new VBox();
        col.setMinWidth(LARGEUR_HEURE);
        col.setMaxWidth(LARGEUR_HEURE);
        col.getStyleClass().add("time-column");

        // Espace vide en haut pour aligner avec les en-têtes de jours
        Region header = new Region();
        header.setPrefHeight(HAUTEUR_HEADER);
        col.getChildren().add(header);

        // Un label par heure
        for (int h = HEURE_DEBUT; h <= HEURE_FIN; h++) {
            Label lbl = new Label(String.format("%02dh00", h));
            lbl.getStyleClass().add("time-label");
            lbl.setPrefHeight(PX_PAR_HEURE);
            lbl.setAlignment(Pos.TOP_RIGHT);
            col.getChildren().add(lbl);
        }
        return col;
    }

    /**
     * Crée la colonne d'un jour (0=Lundi … 4=Vendredi).
     * Chaque colonne contient :
     *   - un en-tête avec le nom du jour et la date
     *   - un StackPane avec le fond quadrillé + le calque des cours
     */
    private VBox buildDayColumn(int dayIndex) {
        VBox col = new VBox();
        col.getStyleClass().add("day-column");

        // En-tête : "Lundi 17 mars" (surligné en bleu si c'est aujourd'hui)
        LocalDate date = currentMonday.plusDays(dayIndex);
        Label header = new Label(JOURS[dayIndex] + " " + date.format(FMT_JOUR));
        header.getStyleClass().add("day-header");
        header.setPrefHeight(HAUTEUR_HEADER);
        header.setMaxWidth(Double.MAX_VALUE);
        header.setAlignment(Pos.CENTER);
        if (date.equals(LocalDate.now())) header.getStyleClass().add("day-header-today");

        // Corps de la colonne : fond quadrillé + calque cours (superposés)
        StackPane body = new StackPane();
        body.getStyleClass().add("day-body");
        body.setPrefHeight(NB_HEURES * PX_PAR_HEURE); // 11 × 80 = 880px
        body.setId("dayBody_" + dayIndex);

        // Couche 1 : fond quadrillé (lignes horizontales alternant blanc/gris clair)
        body.getChildren().add(buildDayGrid());

        // Couche 2 : calque transparent où les cours seront positionnés (Pane = positionnement absolu)
        Pane coursPane = new Pane();
        coursPane.setId("coursPane_" + dayIndex); // ID utilisé pour retrouver ce Pane via lookup()
        coursPane.prefHeightProperty().bind(body.heightProperty());
        body.getChildren().add(coursPane);

        col.getChildren().addAll(header, body);
        VBox.setVgrow(body, Priority.ALWAYS);
        return col;
    }

    /**
     * Crée le fond quadrillé d'une colonne jour.
     * Alternance de bandes blanches/grises pour visualiser les heures.
     */
    private VBox buildDayGrid() {
        VBox grid = new VBox();
        grid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        for (int h = 0; h < NB_HEURES; h++) {
            Region row = new Region();
            row.setPrefHeight(PX_PAR_HEURE);
            row.setMaxWidth(Double.MAX_VALUE);
            // Alternance de couleur de fond (voir main.css : grid-row-even / grid-row-odd)
            row.getStyleClass().add(h % 2 == 0 ? "grid-row-even" : "grid-row-odd");
            grid.getChildren().add(row);
        }
        return grid;
    }

    // -------------------------------------------------------------------------
    //  Chargement et affichage des cours
    // -------------------------------------------------------------------------

    /**
     * Récupère les cours selon le mode actif et les place dans la grille.
     * Cette méthode est appelée à chaque changement de semaine ou d'onglet.
     */
    private void loadCours() {
        // Récupération des cours selon le mode d'affichage actif
        List<CoursDisplay> cours = switch (currentTab) {
            case MON_EDT -> MockDataService.getInstance().getCoursEtudiant(currentMonday);
            case TIERS -> {
                String sel = comboSelector.getValue();
                if (sel == null) yield List.of();
                // Tous les rôles consultent l'EDT par classe/promotion
                yield MockDataService.getInstance().getCoursGroupe(sel, currentMonday);
            }
            case SALLE -> {
                String salle = comboSelector.getValue();
                yield salle == null
                        ? List.of()
                        : MockDataService.getInstance().getCoursSalle(salle, currentMonday);
            }
        };

        // Vider tous les panneaux de cours avant de les re-remplir
        for (int i = 0; i < 5; i++) {
            Pane p = findCoursPane(i);
            if (p != null) p.getChildren().clear();
        }

        // Placer chaque cours dans la bonne colonne au bon endroit vertical
        for (CoursDisplay c : cours) {
            // Calcul de l'indice de la colonne (0=Lundi, 4=Vendredi)
            int dayIndex = c.jour().getDayOfWeek().getValue() - 1;
            if (dayIndex < 0 || dayIndex > 4) continue; // Ignorer week-end

            Pane coursPane = findCoursPane(dayIndex);
            if (coursPane == null) continue;

            VBox block = buildCoursBlock(c);

            // Positionnement vertical : (heureDebut - 8h) × 80px depuis le haut
            double top    = minutesFromStart(c.heureDebut()) / 60.0 * PX_PAR_HEURE;
            double height = minutesFromStart(c.heureFin())   / 60.0 * PX_PAR_HEURE - top - 2;

            block.setLayoutY(top);
            block.setPrefHeight(height);
            block.setLayoutX(4);
            block.setPrefWidth(Math.max(0, coursPane.getWidth() - 8));

            // Ajustement dynamique de la largeur quand la fenêtre est redimensionnée
            coursPane.widthProperty().addListener((obs, o, w) ->
                    block.setPrefWidth(w.doubleValue() - 8));

            coursPane.getChildren().add(block);
        }
    }

    /**
     * Construit le bloc visuel d'un cours (couleur selon le type CM/TD/TP).
     * Contenu du bloc :
     *   - Badge de type (ex: [CM] en bleu)
     *   - Nom du cours en gras
     *   - Salle, groupe, professeur (si l'espace le permet)
     *   - Tooltip au survol avec toutes les infos
     */
    private VBox buildCoursBlock(CoursDisplay c) {
        VBox block = new VBox(3);
        block.getStyleClass().add("cours-block");
        block.setAlignment(Pos.TOP_LEFT);

        TypeCours type = c.typeCours() != null ? c.typeCours() : TypeCours.CM;

        // Style du bloc : fond clair + bordure gauche colorée selon le type
        block.setStyle(
                "-fx-background-color: " + type.getCouleurFond() + ";"
              + "-fx-border-color: "     + type.getCouleurBordure() + ";"
              + "-fx-border-width: 0 0 0 4;"          // bordure gauche seulement
              + "-fx-background-radius: 6; -fx-border-radius: 6;"
              + "-fx-padding: 6 8 6 10;");

        // Badge type (ex: "CM" sur fond bleu)
        Label badge = new Label(type.getLibelle());
        badge.setStyle(
                "-fx-background-color: " + type.getCouleurBordure() + ";"
              + "-fx-text-fill: white; -fx-font-size: 10; -fx-font-weight: bold;"
              + "-fx-background-radius: 4; -fx-padding: 1 5 1 5;");

        // Nom du cours en gras
        Label nomLbl = new Label(c.nom() != null ? c.nom() : "Cours");
        nomLbl.getStyleClass().add("cours-block-nom");
        nomLbl.setWrapText(true);

        block.getChildren().addAll(badge, nomLbl);

        // Informations secondaires (affichées si le bloc est assez grand)
        if (c.nomSalle() != null) {
            Label l = new Label("\uD83D\uDCCD " + c.nomSalle()); // 📍
            l.getStyleClass().add("cours-block-detail");
            block.getChildren().add(l);
        }
        if (c.nomGroupe() != null) {
            Label l = new Label("\uD83D\uDC65 " + c.nomGroupe()); // 👥
            l.getStyleClass().add("cours-block-detail");
            block.getChildren().add(l);
        }
        if (c.nomProf() != null) {
            Label l = new Label("\uD83D\uDC64 " + c.nomProf()); // 👤
            l.getStyleClass().add("cours-block-detail");
            block.getChildren().add(l);
        }

        // Tooltip (infobulle au survol) avec toutes les informations du cours
        Tooltip tip = new Tooltip(buildTooltip(c));
        tip.setWrapText(true);
        tip.setMaxWidth(280);
        Tooltip.install(block, tip);

        return block;
    }

    /** Construit le texte de l'infobulle affichée au survol d'un cours. */
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

    /**
     * Retrouve le Pane de cours d'une colonne jour via son ID.
     * L'ID est défini dans buildDayColumn() : "coursPane_0" à "coursPane_4".
     */
    private Pane findCoursPane(int dayIndex) {
        return (Pane) gridContainer.lookup("#coursPane_" + dayIndex);
    }

    /**
     * Calcule le nombre de minutes écoulées depuis HEURE_DEBUT (8h00).
     * Ex: 10h30 → (10-8)×60 + 30 = 150 minutes.
     */
    private double minutesFromStart(LocalTime time) {
        return (time.getHour() - HEURE_DEBUT) * 60.0 + time.getMinute();
    }
}
