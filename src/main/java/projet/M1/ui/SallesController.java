package projet.M1.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import projet.M1.BDD.dao.CoursDAO;
import projet.M1.BDD.dao.SalleDAO;
import projet.M1.BDD.entity.CoursEntity;
import projet.M1.BDD.entity.Role;
import projet.M1.BDD.entity.SalleEntity;
import projet.M1.BDD.entity.UserEntity;
import projet.M1.controller.SalleController;
import projet.M1.session.SessionManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * UC9/US18 — Consulter les salles (Gestionnaire + Professeur).
 * Filtres : nom, capacité min, équipement.
 * Clic sur une salle → dialog avec équipements + EDT hebdomadaire de la salle.
 */
public class SallesController {

    @FXML private TextField fieldRecherche;
    @FXML private TextField fieldCapaciteMin;
    @FXML private TextField fieldEquipement;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private VBox sallesContainer;

    private final SalleController salleController = new SalleController(new SalleDAO());
    private final CoursDAO coursDAO = new CoursDAO();

    private List<SalleEntity> toutesLesSalles = List.of();

    private static final DateTimeFormatter FMT_JOUR =
            DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH);
    private static final String[] JOURS = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi"};

    @FXML
    public void initialize() {
        fieldRecherche.textProperty().addListener((obs, o, n) -> applyFilters());
        fieldCapaciteMin.textProperty().addListener((obs, o, n) -> applyFilters());
        fieldEquipement.textProperty().addListener((obs, o, n) -> applyFilters());

        Thread t = new Thread(() -> {
            List<SalleEntity> salles;
            try { salles = salleController.getAllSalles(); }
            catch (Exception e) { salles = List.of(); }
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

    private void applyFilters() {
        if (toutesLesSalles.isEmpty()) return;
        String nom = fieldRecherche.getText().trim().toLowerCase();
        String equip = fieldEquipement.getText().trim().toLowerCase();
        int capMin = 0;
        try { capMin = Integer.parseInt(fieldCapaciteMin.getText().trim()); }
        catch (NumberFormatException ignored) {}
        final int capMinFinal = capMin;

        List<SalleEntity> filtres = toutesLesSalles.stream()
                .filter(s -> nom.isEmpty() || (s.getNom() != null && s.getNom().toLowerCase().contains(nom)))
                .filter(s -> capMinFinal == 0 || s.getPlace() >= capMinFinal)
                .filter(s -> {
                    if (equip.isEmpty()) return true;
                    try {
                        return s.getListe_materiel() != null
                                && s.getListe_materiel().stream()
                                        .anyMatch(m -> m.toLowerCase().contains(equip));
                    } catch (Exception e) { return false; }
                })
                .toList();
        afficherSalles(filtres);
    }

    private void afficherSalles(List<SalleEntity> salles) {
        sallesContainer.getChildren().clear();
        if (salles.isEmpty()) {
            Label vide = new Label("Aucune salle trouvée.");
            vide.getStyleClass().add("text-muted");
            sallesContainer.getChildren().add(vide);
            return;
        }
        for (SalleEntity s : salles) sallesContainer.getChildren().add(buildSalleCard(s));
    }

    private HBox buildSalleCard(SalleEntity s) {
        HBox card = new HBox(16);
        card.getStyleClass().add("salle-card-us18");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(48);
        card.setOnMouseClicked(e -> ouvrirDetail(s));

        Label nomLabel = new Label(s.getNom() != null ? s.getNom() : "—");
        nomLabel.getStyleClass().add("groupe-card-nom");
        HBox.setHgrow(nomLabel, Priority.ALWAYS);
        nomLabel.setMaxWidth(Double.MAX_VALUE);

        Label capLabel = new Label("👥 " + s.getPlace() + " places");
        capLabel.getStyleClass().add("groupe-card-stats");

        Label chevron = new Label("›");
        chevron.getStyleClass().add("quick-action-chevron");

        card.getChildren().addAll(nomLabel, capLabel, chevron);
        return card;
    }

    private void ouvrirDetail(SalleEntity s) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Salle " + s.getNom());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/projet/M1/css/main.css").toExternalForm());
        dialog.getDialogPane().setPrefWidth(600);

        VBox content = new VBox(20);
        content.getStyleClass().add("page-container");

        Label titre = new Label(s.getNom() != null ? s.getNom() : "—");
        titre.getStyleClass().add("page-title");

        // Capacité
        HBox stats = new HBox(16);
        VBox capBox = buildStatBox("Capacité", s.getPlace() + " places");
        HBox.setHgrow(capBox, Priority.ALWAYS);
        capBox.setMaxWidth(Double.MAX_VALUE);
        stats.getChildren().add(capBox);

        // Équipements
        List<String> materiel = List.of();
        try { materiel = s.getListe_materiel() != null ? s.getListe_materiel() : List.of(); }
        catch (Exception ignored) {}

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

        // EDT de la salle
        Label edtTitre = new Label("Emploi du temps de la salle");
        edtTitre.getStyleClass().add("form-step-title");

        VBox edtPanel = buildEdtPanel(s, LocalDate.now().with(DayOfWeek.MONDAY));

        // US19 — Bouton "Modifier les équipements" visible uniquement pour le gestionnaire
        UserEntity moi = SessionManager.getInstance().getUtilisateurConnecte();
        if (moi != null && moi.getRole() == Role.GESTIONNAIRE_PLANNING) {
            Button btnModifEquip = new Button("Modifier les équipements");
            btnModifEquip.getStyleClass().add("btn-secondary");
            btnModifEquip.setOnAction(ev -> ouvrirDialogModifierEquipements(s, equipList));
            content.getChildren().addAll(titre, stats, equipTitre, equipList, btnModifEquip, edtTitre, edtPanel);
        } else {
            content.getChildren().addAll(titre, stats, equipTitre, equipList, edtTitre, edtPanel);
        }

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    /**
     * US19 — Dialog pour modifier la liste des équipements d'une salle.
     * Ouvre une TextArea pré-remplie (un équipement par ligne).
     * À la validation, persiste en BDD et rafraîchit equipList dans le dialog parent.
     */
    private void ouvrirDialogModifierEquipements(SalleEntity salle, VBox equipList) {
        List<String> actuel;
        try { actuel = salle.getListe_materiel() != null ? salle.getListe_materiel() : List.of(); }
        catch (Exception e) { actuel = List.of(); }

        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Modifier les équipements — " + salle.getNom());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/projet/M1/css/main.css").toExternalForm());

        TextArea area = new TextArea(String.join("\n", actuel));
        area.setPromptText("Un équipement par ligne…");
        area.setPrefRowCount(8);
        area.setWrapText(true);

        Label hint = new Label("Saisissez un équipement par ligne.");
        hint.getStyleClass().add("text-muted");

        VBox content = new VBox(8, hint, area);
        content.getStyleClass().add("page-container");
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            return java.util.Arrays.stream(area.getText().split("\n"))
                    .map(String::trim).filter(s -> !s.isEmpty())
                    .collect(java.util.stream.Collectors.toList());
        });

        dialog.showAndWait().ifPresent(newList -> {
            try {
                salleController.modifierEquipements(salle.getId(), newList);
                salle.setListe_materiel(newList);
                // Rafraîchir la liste affichée dans le dialog parent
                equipList.getChildren().clear();
                if (newList.isEmpty()) {
                    Label vide = new Label("Aucun équipement renseigné.");
                    vide.getStyleClass().add("text-muted");
                    equipList.getChildren().add(vide);
                } else {
                    for (String item : newList) {
                        Label l = new Label("• " + item);
                        l.getStyleClass().add("cours-block-detail");
                        equipList.getChildren().add(l);
                    }
                }
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur"); alert.setHeaderText(null);
                alert.setContentText("Impossible de sauvegarder : " + e.getMessage());
                alert.getDialogPane().getStylesheets().add(
                        getClass().getResource("/projet/M1/css/main.css").toExternalForm());
                alert.showAndWait();
            }
        });
    }

    /** Construit le panel EDT navigable par semaine pour une salle. */
    private VBox buildEdtPanel(SalleEntity salle, LocalDate initMonday) {
        VBox panel = new VBox(8);

        // Référence mutable sur la semaine courante
        final LocalDate[] monday = {initMonday};

        Label labelSemaine = new Label(formatSemaine(initMonday));
        labelSemaine.getStyleClass().add("schedule-panel-label");
        labelSemaine.setStyle("-fx-font-weight: bold;");

        Button btnPrev = new Button("‹");
        btnPrev.getStyleClass().add("btn-secondary");
        Button btnNext = new Button("›");
        btnNext.getStyleClass().add("btn-secondary");

        HBox navBar = new HBox(8, btnPrev, labelSemaine, btnNext);
        navBar.setAlignment(Pos.CENTER_LEFT);

        VBox listeCours = new VBox(4);
        ProgressIndicator loading = new ProgressIndicator();
        loading.setPrefSize(24, 24);
        listeCours.getChildren().add(loading);

        panel.getChildren().addAll(navBar, listeCours);

        // Charge les cours pour la semaine donnée
        Runnable charger = () -> {
            listeCours.getChildren().setAll(new ProgressIndicator());
            Thread t = new Thread(() -> {
                List<CoursEntity> cours;
                try { cours = coursDAO.findBySalleAndSemaine(salle, monday[0]); }
                catch (Exception e) { cours = List.of(); }
                final List<CoursEntity> result = cours;
                final LocalDate m = monday[0];
                Platform.runLater(() -> {
                    listeCours.getChildren().clear();
                    fillListeCours(listeCours, result, m);
                    labelSemaine.setText(formatSemaine(m));
                });
            });
            t.setDaemon(true);
            t.start();
        };

        btnPrev.setOnAction(e -> { monday[0] = monday[0].minusWeeks(1); charger.run(); });
        btnNext.setOnAction(e -> { monday[0] = monday[0].plusWeeks(1);  charger.run(); });

        // Charge la semaine initiale
        charger.run();

        return panel;
    }

    private void fillListeCours(VBox list, List<CoursEntity> cours, LocalDate monday) {
        if (cours.isEmpty()) {
            Label vide = new Label("Aucun cours cette semaine.");
            vide.getStyleClass().add("text-muted");
            list.getChildren().add(vide);
            return;
        }
        for (int d = 0; d < 5; d++) {
            final LocalDate date = monday.plusDays(d);
            List<CoursEntity> duJour = cours.stream()
                    .filter(c -> {
                        try { return c.getHoraire() != null && date.equals(c.getHoraire().getJour()); }
                        catch (Exception e) { return false; }
                    })
                    .toList();
            if (duJour.isEmpty()) continue;

            Label jourLabel = new Label(JOURS[d] + " " + date.format(FMT_JOUR));
            jourLabel.getStyleClass().add("groupe-col-header");
            jourLabel.setStyle("-fx-padding: 6 0 2 0;");
            list.getChildren().add(jourLabel);

            for (CoursEntity c : duJour) {
                HBox row = new HBox(12);
                row.getStyleClass().add("groupe-tableau-row");
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-padding: 6 10 6 10; -fx-background-radius: 6;");

                String heure = "";
                try {
                    if (c.getHoraire() != null)
                        heure = c.getHoraire().getHeureDebut() + " – " + c.getHoraire().getHeureFin();
                } catch (Exception ignored) {}

                String nomCours = "—";
                try { if (c.getNom() != null) nomCours = c.getNom(); } catch (Exception ignored) {}

                String nomGroupe = "";
                try {
                    if (c.getList_etudiant() != null && !c.getList_etudiant().isEmpty()) {
                        var grp = c.getList_etudiant().get(0).getGroupe();
                        if (grp != null && grp.getNom() != null) nomGroupe = grp.getNom();
                    }
                } catch (Exception ignored) {}

                Label heureLabel = new Label(heure);
                heureLabel.getStyleClass().add("cours-block-time");
                heureLabel.setPrefWidth(140);

                Label nomLabel = new Label(nomCours);
                nomLabel.getStyleClass().add("cours-block-title");
                HBox.setHgrow(nomLabel, Priority.ALWAYS);

                Label grpLabel = new Label(nomGroupe);
                grpLabel.getStyleClass().add("groupe-etudiant-login");

                row.getChildren().addAll(heureLabel, nomLabel, grpLabel);
                list.getChildren().add(row);
            }
        }
    }

    private String formatSemaine(LocalDate monday) {
        return "Semaine du " + monday.format(FMT_JOUR)
                + " – " + monday.plusDays(4).format(FMT_JOUR)
                + " " + monday.getYear();
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
