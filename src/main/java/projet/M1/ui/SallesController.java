package projet.M1.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import projet.M1.BDD.dao.CoursDAO;
import projet.M1.BDD.dao.SalleDAO;
import projet.M1.BDD.entity.CoursEntity;
import projet.M1.BDD.entity.HoraireEntity;
import projet.M1.BDD.entity.SalleEntity;
import projet.M1.controller.EmploiDuTempsController;
import projet.M1.controller.SalleController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * UC9/US18 — Consulter les salles (Gestionnaire et Professeur).
 *
 * Affiche la liste des salles avec :
 *   - nom, capacité, résumé des équipements
 *
 * Filtrage disponible :
 *   - par nom (champ texte)
 *   - par capacité (Toutes / Petite <20 / Moyenne 20-40 / Grande >40)
 *   - par équipement (champ texte sur le matériel)
 *
 * Clic sur une salle → dialog avec capacité, matériel et EDT de la salle (semaine courante).
 */
public class SallesController {

    @FXML private TextField          fieldRecherche;
    @FXML private ComboBox<String>   comboFiltreCapacite;
    @FXML private TextField          fieldFiltreEquipement;
    @FXML private ProgressIndicator  loadingIndicator;
    @FXML private VBox               sallesContainer;

    private final SalleController         salleController = new SalleController(new SalleDAO());
    private final EmploiDuTempsController edtController   = new EmploiDuTempsController(new CoursDAO());

    private List<SalleEntity> toutesLesSalles = List.of();

    private static final DateTimeFormatter FMT_DATE  = DateTimeFormatter.ofPattern("EEE d MMM", Locale.FRENCH);
    private static final DateTimeFormatter FMT_TIME  = DateTimeFormatter.ofPattern("HH'h'mm");

    @FXML
    public void initialize() {
        comboFiltreCapacite.getItems().setAll(
                "Toutes capacités", "Petite (< 20)", "Moyenne (20–40)", "Grande (> 40)");
        comboFiltreCapacite.setValue("Toutes capacités");

        fieldRecherche.textProperty().addListener((obs, o, n) -> appliquerFiltres());
        comboFiltreCapacite.setOnAction(e -> appliquerFiltres());
        fieldFiltreEquipement.textProperty().addListener((obs, o, n) -> appliquerFiltres());

        Thread t = new Thread(() -> {
            List<SalleEntity> salles;
            try {
                salles = salleController.getAllSalles();
            } catch (Exception e) {
                salles = List.of();
            }
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

    // -------------------------------------------------------------------------
    //  Filtrage UC9
    // -------------------------------------------------------------------------

    private void appliquerFiltres() {
        if (toutesLesSalles.isEmpty()) return;

        String recherche  = fieldRecherche.getText().trim().toLowerCase();
        String capacite   = comboFiltreCapacite.getValue();
        String equipement = fieldFiltreEquipement.getText().trim().toLowerCase();

        List<SalleEntity> filtres = toutesLesSalles.stream()
                .filter(s -> {
                    // Filtre nom
                    if (!recherche.isEmpty() && (s.getNom() == null
                            || !s.getNom().toLowerCase().contains(recherche)))
                        return false;

                    // Filtre capacité
                    int places = s.getPlace();
                    if (capacite != null) {
                        switch (capacite) {
                            case "Petite (< 20)"   -> { if (places >= 20) return false; }
                            case "Moyenne (20–40)" -> { if (places < 20 || places > 40) return false; }
                            case "Grande (> 40)"   -> { if (places <= 40) return false; }
                        }
                    }

                    // Filtre équipement
                    if (!equipement.isEmpty()) {
                        List<String> mat = safeMateriel(s);
                        boolean found = mat.stream()
                                .anyMatch(m -> m.toLowerCase().contains(equipement));
                        if (!found) return false;
                    }

                    return true;
                })
                .toList();

        afficherSalles(filtres);
    }

    // -------------------------------------------------------------------------
    //  Affichage liste
    // -------------------------------------------------------------------------

    private void afficherSalles(List<SalleEntity> salles) {
        sallesContainer.getChildren().clear();
        if (salles.isEmpty()) {
            Label vide = new Label("Aucune salle trouvée.");
            vide.getStyleClass().add("text-muted");
            sallesContainer.getChildren().add(vide);
            return;
        }
        for (SalleEntity s : salles) {
            sallesContainer.getChildren().add(buildSalleCard(s));
        }
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

        // Résumé équipements
        List<String> mat = safeMateriel(s);
        String equipResume = mat.isEmpty() ? "Aucun équipement"
                : mat.size() + " équipement" + (mat.size() > 1 ? "s" : "");
        Label equipLabel = new Label("🔧 " + equipResume);
        equipLabel.getStyleClass().add("groupe-card-stats");

        Label chevron = new Label("›");
        chevron.getStyleClass().add("quick-action-chevron");

        card.getChildren().addAll(nomLabel, capLabel, equipLabel, chevron);
        return card;
    }

    // -------------------------------------------------------------------------
    //  Dialog détail salle (capacité + matériel + EDT semaine courante)
    // -------------------------------------------------------------------------

    private void ouvrirDetail(SalleEntity s) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Salle " + s.getNom());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/projet/M1/css/main.css").toExternalForm());
        dialog.getDialogPane().setPrefWidth(560);

        VBox content = new VBox(16);
        content.getStyleClass().add("page-container");

        Label titre = new Label(s.getNom() != null ? s.getNom() : "—");
        titre.getStyleClass().add("page-title");

        // Stats capacité
        HBox stats = new HBox(16);
        VBox capBox = buildStatBox("Capacité", s.getPlace() + " places");
        HBox.setHgrow(capBox, Priority.ALWAYS);
        capBox.setMaxWidth(Double.MAX_VALUE);
        stats.getChildren().add(capBox);

        // Équipements
        Label equipTitre = new Label("Équipements");
        equipTitre.getStyleClass().add("form-step-title");

        VBox equipList = new VBox(6);
        List<String> materiel = safeMateriel(s);
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

        // EDT de la salle — semaine courante, chargé en arrière-plan
        Label edtTitre = new Label("EDT de la salle — semaine courante");
        edtTitre.getStyleClass().add("form-step-title");

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(24, 24);

        VBox edtList = new VBox(6);
        edtList.getChildren().add(spinner);

        content.getChildren().addAll(titre, stats, equipTitre, equipList, edtTitre, edtList);
        dialog.getDialogPane().setContent(content);

        // Charger l'EDT en arrière-plan une fois le dialog ouvert
        LocalDate semaine = LocalDate.now().with(DayOfWeek.MONDAY);
        Thread t = new Thread(() -> {
            List<CoursEntity> cours;
            try {
                cours = edtController.getEmploiDuTempsSalle(s, semaine);
            } catch (Exception e) {
                cours = List.of();
            }
            final List<CoursEntity> result = cours;
            Platform.runLater(() -> {
                edtList.getChildren().clear();
                if (result.isEmpty()) {
                    Label vide = new Label("Aucun cours cette semaine.");
                    vide.getStyleClass().add("text-muted");
                    edtList.getChildren().add(vide);
                } else {
                    for (CoursEntity c : result) {
                        edtList.getChildren().add(buildEdtRow(c));
                    }
                }
            });
        });
        t.setDaemon(true);
        t.start();

        dialog.showAndWait();
    }

    private HBox buildEdtRow(CoursEntity c) {
        HBox row = new HBox(12);
        row.getStyleClass().add("edt-croise-row");
        row.setAlignment(Pos.CENTER_LEFT);

        HoraireEntity h = c.getHoraire();
        String jour  = h != null ? h.getJour().format(FMT_DATE) : "—";
        String heure = h != null
                ? h.getHeureDebut().format(FMT_TIME) + " – " + h.getHeureFin().format(FMT_TIME)
                : "—";

        Label nomLbl = new Label(c.getNom() != null ? c.getNom() : "Cours");
        nomLbl.getStyleClass().add("edt-croise-cours-nom");
        HBox.setHgrow(nomLbl, Priority.ALWAYS);
        nomLbl.setMaxWidth(Double.MAX_VALUE);

        Label typeLbl = new Label(c.getTypeCours() != null ? "[" + c.getTypeCours() + "]" : "");
        typeLbl.getStyleClass().add("groupe-card-stats");

        Label infoLbl = new Label(jour + "  " + heure);
        infoLbl.getStyleClass().add("edt-croise-cours-info");

        row.getChildren().addAll(nomLbl, typeLbl, infoLbl);
        return row;
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

    private List<String> safeMateriel(SalleEntity s) {
        try {
            return s.getListe_materiel() != null ? s.getListe_materiel() : List.of();
        } catch (Exception ignored) {
            return List.of();
        }
    }
}
