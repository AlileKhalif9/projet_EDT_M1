package projet.M1.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import projet.M1.BDD.dao.ModuleDAO;
import projet.M1.BDD.dao.NoteDAO;
import projet.M1.BDD.entity.ModuleEntity;
import projet.M1.BDD.entity.NoteEntity;
import projet.M1.BDD.entity.PromotionEntity;
import projet.M1.BDD.entity.Role;
import projet.M1.BDD.entity.UserEntity;
import projet.M1.controller.NoteController;
import projet.M1.session.SessionManager;

import java.util.*;

/**
 * UC11 — Professeur : consulter les notes (sélection promotion → module).
 * UC12 — Étudiant   : consulter ses propres notes (sélection module).
 * UC13 — Professeur : ajouter / modifier les notes.
 */
public class NotesController {

    @FXML private Label             labelTitle;
    @FXML private Label             labelSubtitle;
    @FXML private HBox              selectorBarProf;
    @FXML private ComboBox<String>  comboPromotion;
    @FXML private ComboBox<String>  comboModuleProf;
    @FXML private HBox              selectorBarEtu;
    @FXML private ComboBox<String>  comboModuleEtu;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private VBox              notesContainer;

    private final NoteController noteController =
            new NoteController(new NoteDAO(), new ModuleDAO());

    private final Map<String, PromotionEntity> promotionMap = new LinkedHashMap<>();
    private final Map<String, ModuleEntity>    moduleMap    = new LinkedHashMap<>();

    private PromotionEntity      promotionCourante;
    private List<UserEntity>     etudiantsCourants = new ArrayList<>();

    // -------------------------------------------------------------------------
    //  Initialisation
    // -------------------------------------------------------------------------

    @FXML
    public void initialize() {
        UserEntity u = SessionManager.getInstance().getUtilisateurConnecte();
        if (u == null) return;

        boolean isProf = u.getRole() == Role.PROFESSEUR;

        if (isProf) {
            labelTitle.setText("Notes des étudiants");
            labelSubtitle.setText("Sélectionnez une promotion puis un module");
            selectorBarProf.setVisible(true);
            selectorBarProf.setManaged(true);
            chargerPromotions(u);
        } else {
            labelTitle.setText("Mes notes");
            labelSubtitle.setText("Consultez vos notes et vos moyennes par module");
            selectorBarEtu.setVisible(true);
            selectorBarEtu.setManaged(true);
            chargerModulesEtudiant(u);
        }
    }

    // -------------------------------------------------------------------------
    //  Professeur — chargement promotions
    // -------------------------------------------------------------------------

    private void chargerPromotions(UserEntity u) {
        Thread t = new Thread(() -> {
            List<PromotionEntity> promotions;
            try {
                promotions = noteController.getPromotionsProfesseur(u.getId());
            } catch (Exception ex) {
                ex.printStackTrace();
                promotions = List.of();
            }
            final List<PromotionEntity> result = promotions;
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                loadingIndicator.setManaged(false);
                if (result.isEmpty()) {
                    Label vide = new Label("Aucune promotion trouvée.");
                    vide.getStyleClass().add("text-muted");
                    notesContainer.getChildren().add(vide);
                    return;
                }
                for (PromotionEntity p : result) {
                    promotionMap.put(p.getNom(), p);
                    comboPromotion.getItems().add(p.getNom());
                }
                comboPromotion.setOnAction(e -> onPromotionSelectionnee(u));
                comboPromotion.getSelectionModel().selectFirst();
                onPromotionSelectionnee(u);
            });
        });
        t.setDaemon(true);
        t.start();
    }

    private void onPromotionSelectionnee(UserEntity u) {
        String nomPromo = comboPromotion.getValue();
        if (nomPromo == null) return;
        PromotionEntity promo = promotionMap.get(nomPromo);
        if (promo == null) return;

        promotionCourante = promo;
        comboModuleProf.getItems().clear();
        comboModuleProf.setOnAction(null);
        moduleMap.clear();
        notesContainer.getChildren().clear();

        Thread t = new Thread(() -> {
            List<ModuleEntity> modules;
            try {
                modules = noteController.getModulesProfesseurEtPromotion(u.getId(), promo.getId());
            } catch (Exception ex) {
                ex.printStackTrace();
                modules = List.of();
            }
            final List<ModuleEntity> result = modules;
            Platform.runLater(() -> {
                if (result.isEmpty()) {
                    Label vide = new Label("Aucun module trouvé pour cette promotion.");
                    vide.getStyleClass().add("text-muted");
                    notesContainer.getChildren().add(vide);
                    return;
                }
                for (ModuleEntity m : result) {
                    moduleMap.put(m.getNom(), m);
                    comboModuleProf.getItems().add(m.getNom());
                }
                comboModuleProf.setOnAction(e -> onModuleProfSelectionne(u, promo));
                comboModuleProf.getSelectionModel().selectFirst();
                onModuleProfSelectionne(u, promo);
            });
        });
        t.setDaemon(true);
        t.start();
    }

    private void onModuleProfSelectionne(UserEntity u, PromotionEntity promo) {
        String nomModule = comboModuleProf.getValue();
        if (nomModule == null) return;
        ModuleEntity module = moduleMap.get(nomModule);
        if (module == null) return;

        notesContainer.getChildren().clear();
        loadingIndicator.setVisible(true);
        loadingIndicator.setManaged(true);

        Thread t = new Thread(() -> {
            try {
                List<UserEntity> etudiants =
                        noteController.getEtudiantsDuModuleEtPromotion(module.getId(), promo.getId());
                List<String> intitules =
                        noteController.getIntitulesControles(module.getId());
                Map<Long, Map<String, NoteEntity>> notesMap =
                        noteController.getNotesParEtudiantEtControle(module.getId());
                List<NoteEntity> toutesNotes = new ArrayList<>();
                notesMap.values().forEach(m -> toutesNotes.addAll(m.values()));

                etudiantsCourants = new ArrayList<>(etudiants);

                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    loadingIndicator.setManaged(false);
                    afficherTableauProfesseur(module, promo, etudiants, intitules, notesMap, toutesNotes);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    loadingIndicator.setManaged(false);
                    Label err = new Label("Erreur lors du chargement des notes.");
                    err.getStyleClass().add("text-muted");
                    notesContainer.getChildren().setAll(err);
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // -------------------------------------------------------------------------
    //  Étudiant — chargement modules
    // -------------------------------------------------------------------------

    private void chargerModulesEtudiant(UserEntity u) {
        Thread t = new Thread(() -> {
            List<ModuleEntity> modules;
            try {
                modules = noteController.getModulesEtudiant(u.getId());
            } catch (Exception ex) {
                ex.printStackTrace();
                modules = List.of();
            }
            final List<ModuleEntity> result = modules;
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                loadingIndicator.setManaged(false);
                if (result.isEmpty()) {
                    Label vide = new Label("Aucun module trouvé.");
                    vide.getStyleClass().add("text-muted");
                    notesContainer.getChildren().add(vide);
                    return;
                }
                for (ModuleEntity m : result) {
                    moduleMap.put(m.getNom(), m);
                    comboModuleEtu.getItems().add(m.getNom());
                }
                comboModuleEtu.setOnAction(e -> onModuleEtudiantSelectionne(u));
                comboModuleEtu.getSelectionModel().selectFirst();
                onModuleEtudiantSelectionne(u);
            });
        });
        t.setDaemon(true);
        t.start();
    }

    private void onModuleEtudiantSelectionne(UserEntity u) {
        String nomModule = comboModuleEtu.getValue();
        if (nomModule == null) return;
        ModuleEntity module = moduleMap.get(nomModule);
        if (module == null) return;

        notesContainer.getChildren().clear();
        loadingIndicator.setVisible(true);
        loadingIndicator.setManaged(true);

        Thread t = new Thread(() -> {
            try {
                List<NoteEntity> mesNotes =
                        noteController.getNotesEtudiantModule(u.getId(), module.getId());
                List<NoteEntity> toutesNotes =
                        new NoteDAO().findByModule(module.getId());

                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    loadingIndicator.setManaged(false);
                    afficherTableauEtudiant(module, mesNotes, toutesNotes);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    loadingIndicator.setManaged(false);
                    Label err = new Label("Erreur lors du chargement des notes.");
                    err.getStyleClass().add("text-muted");
                    notesContainer.getChildren().setAll(err);
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // -------------------------------------------------------------------------
    //  UC11 — Tableau professeur
    // -------------------------------------------------------------------------

    private void afficherTableauProfesseur(ModuleEntity module,
                                           PromotionEntity promo,
                                           List<UserEntity> etudiants,
                                           List<String> intitules,
                                           Map<Long, Map<String, NoteEntity>> notesMap,
                                           List<NoteEntity> toutesNotes) {
        notesContainer.getChildren().clear();

        UserEntity u = SessionManager.getInstance().getUtilisateurConnecte();

        Button btnAjouter = new Button("+ Ajouter un contrôle");
        btnAjouter.getStyleClass().add("btn-secondary");
        btnAjouter.setOnAction(e ->
                ouvrirDialogAjouterControle(module, promo, etudiantsCourants, intitules, u));

        HBox toolbar = new HBox(12, btnAjouter);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        notesContainer.getChildren().add(toolbar);

        if (etudiants.isEmpty()) {
            Label vide = new Label("Aucun étudiant inscrit à ce module pour cette promotion.");
            vide.getStyleClass().add("text-muted");
            notesContainer.getChildren().add(vide);
            return;
        }

        if (intitules.isEmpty()) {
            Label vide = new Label("Aucune note enregistrée. Cliquez sur \"+ Ajouter un contrôle\".");
            vide.getStyleClass().add("text-muted");
            notesContainer.getChildren().add(vide);
            return;
        }

        GridPane grid = buildGridBase(intitules, true);

        for (int row = 0; row < etudiants.size(); row++) {
            UserEntity etu = etudiants.get(row);
            Label nomLbl = new Label(etu.getPrenom() + " " + etu.getNom());
            nomLbl.getStyleClass().add("notes-cell-nom");
            nomLbl.setPrefWidth(160);
            grid.add(nomLbl, 0, row + 1);

            Map<String, NoteEntity> notesEtu = notesMap.getOrDefault(etu.getId(), Map.of());
            List<NoteEntity> notesEtuList = new ArrayList<>(notesEtu.values());

            for (int col = 0; col < intitules.size(); col++) {
                String intitule = intitules.get(col);
                NoteEntity note = notesEtu.get(intitule);

                Label cellLbl = new Label(note != null
                        ? String.format("%.1f", note.getValeur()) : "—");
                cellLbl.getStyleClass().add(row % 2 == 0 ? "notes-cell" : "notes-cell-alt");
                cellLbl.setAlignment(Pos.CENTER);
                cellLbl.setPrefWidth(80);
                cellLbl.setStyle("-fx-cursor: hand;");

                final int finalCol = col;
                cellLbl.setOnMouseClicked(ev ->
                        ouvrirDialogSaisirNote(module, etu, intitules.get(finalCol), note, cellLbl, u, promo));
                grid.add(cellLbl, col + 1, row + 1);
            }

            float moy = noteController.calculerMoyenne(notesEtuList);
            Label moyLbl = new Label(moy >= 0 ? String.format("%.2f", moy) : "—");
            moyLbl.getStyleClass().add("notes-cell-moyenne");
            moyLbl.setAlignment(Pos.CENTER);
            moyLbl.setPrefWidth(80);
            grid.add(moyLbl, intitules.size() + 1, row + 1);
        }

        // Ligne moyenne de classe
        int lastRow = etudiants.size() + 1;
        Label moyClasseLbl = new Label("Moy. classe");
        moyClasseLbl.getStyleClass().add("notes-cell-header");
        moyClasseLbl.setPrefWidth(160);
        grid.add(moyClasseLbl, 0, lastRow);

        for (int col = 0; col < intitules.size(); col++) {
            float moy = noteController.calculerMoyenneControle(toutesNotes, intitules.get(col));
            Label moyLbl = new Label(moy >= 0 ? String.format("%.2f", moy) : "—");
            moyLbl.getStyleClass().add("notes-cell-moyenne");
            moyLbl.setAlignment(Pos.CENTER);
            moyLbl.setPrefWidth(80);
            grid.add(moyLbl, col + 1, lastRow);
        }

        // Afficher directement sans ScrollPane
        notesContainer.getChildren().add(grid);
    }

    // -------------------------------------------------------------------------
    //  UC12 — Tableau étudiant
    // -------------------------------------------------------------------------

    private void afficherTableauEtudiant(ModuleEntity module,
                                         List<NoteEntity> mesNotes,
                                         List<NoteEntity> toutesNotes) {
        notesContainer.getChildren().clear();

        if (mesNotes.isEmpty()) {
            Label vide = new Label("Aucune note disponible pour ce module.");
            vide.getStyleClass().add("text-muted");
            notesContainer.getChildren().add(vide);
            return;
        }

        List<String> intitules = mesNotes.stream()
                .map(NoteEntity::getIntitule).distinct().sorted().toList();

        GridPane grid = buildGridBase(intitules, true);

        // Ligne "Ma note"
        Label maNomLbl = new Label("Ma note");
        maNomLbl.getStyleClass().add("notes-cell-nom");
        maNomLbl.setPrefWidth(160);
        grid.add(maNomLbl, 0, 1);

        Map<String, NoteEntity> mesNotesMap = new HashMap<>();
        for (NoteEntity n : mesNotes) mesNotesMap.put(n.getIntitule(), n);

        for (int col = 0; col < intitules.size(); col++) {
            NoteEntity note = mesNotesMap.get(intitules.get(col));
            Label cellLbl = new Label(note != null
                    ? String.format("%.1f", note.getValeur()) : "—");
            cellLbl.getStyleClass().add("notes-cell");
            cellLbl.setAlignment(Pos.CENTER);
            cellLbl.setPrefWidth(80);
            grid.add(cellLbl, col + 1, 1);
        }

        float maMoy = noteController.calculerMoyenne(mesNotes);
        Label maMoyLbl = new Label(maMoy >= 0 ? String.format("%.2f", maMoy) : "—");
        maMoyLbl.getStyleClass().add("notes-cell-moyenne");
        maMoyLbl.setAlignment(Pos.CENTER);
        maMoyLbl.setPrefWidth(80);
        grid.add(maMoyLbl, intitules.size() + 1, 1);

        // Ligne moyenne de classe
        Label moyClasseLbl = new Label("Moy. classe");
        moyClasseLbl.getStyleClass().add("notes-cell-header");
        moyClasseLbl.setPrefWidth(160);
        grid.add(moyClasseLbl, 0, 2);

        for (int col = 0; col < intitules.size(); col++) {
            float moy = noteController.calculerMoyenneControle(toutesNotes, intitules.get(col));
            Label moyLbl = new Label(moy >= 0 ? String.format("%.2f", moy) : "—");
            moyLbl.getStyleClass().add("notes-cell-moyenne");
            moyLbl.setAlignment(Pos.CENTER);
            moyLbl.setPrefWidth(80);
            grid.add(moyLbl, col + 1, 2);
        }

        // Afficher directement sans ScrollPane
        notesContainer.getChildren().add(grid);
    }

    // -------------------------------------------------------------------------
    //  Helpers tableau
    // -------------------------------------------------------------------------

    private GridPane buildGridBase(List<String> intitules, boolean withMoyenne) {
        GridPane grid = new GridPane();
        grid.setHgap(4);
        grid.setVgap(4);
        grid.getStyleClass().add("notes-grid");

        Label hEtu = new Label("Étudiant");
        hEtu.getStyleClass().add("notes-cell-header");
        hEtu.setPrefWidth(160);
        grid.add(hEtu, 0, 0);

        for (int col = 0; col < intitules.size(); col++) {
            Label h = new Label(intitules.get(col));
            h.getStyleClass().add("notes-cell-header");
            h.setAlignment(Pos.CENTER);
            h.setPrefWidth(80);
            grid.add(h, col + 1, 0);
        }

        if (withMoyenne) {
            Label hMoy = new Label("Moyenne");
            hMoy.getStyleClass().add("notes-cell-header");
            hMoy.setAlignment(Pos.CENTER);
            hMoy.setPrefWidth(80);
            grid.add(hMoy, intitules.size() + 1, 0);
        }

        return grid;
    }

    // -------------------------------------------------------------------------
    //  UC13 — Dialog saisie note individuelle
    // -------------------------------------------------------------------------

    private void ouvrirDialogSaisirNote(ModuleEntity module, UserEntity etu,
                                        String intitule, NoteEntity noteExistante,
                                        Label cellLbl, UserEntity prof, PromotionEntity promo) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Note — " + intitule);
        dialog.setHeaderText(etu.getPrenom() + " " + etu.getNom() + " · " + module.getNom());

        ButtonType btnValider = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/projet/M1/css/main.css").toExternalForm());

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new javafx.geometry.Insets(16));

        TextField fieldNote = new TextField(noteExistante != null
                ? String.valueOf(noteExistante.getValeur()) : "");
        fieldNote.setPromptText("Note sur 20");

        TextField fieldCoeff = new TextField(noteExistante != null
                ? String.valueOf(noteExistante.getCoefficient()) : "1.0");
        fieldCoeff.setPromptText("Coefficient");

        form.add(new Label("Note (0–20) *"), 0, 0); form.add(fieldNote,  1, 0);
        form.add(new Label("Coefficient *"),  0, 1); form.add(fieldCoeff, 1, 1);
        form.getChildren().stream().filter(n -> n instanceof Label)
                .forEach(n -> ((Label) n).getStyleClass().add("form-label"));

        dialog.getDialogPane().setContent(form);

        dialog.showAndWait().ifPresent(result -> {
            if (result != btnValider) return;
            try {
                float valeur = Float.parseFloat(fieldNote.getText().trim().replace(",", "."));
                float coeff  = Float.parseFloat(fieldCoeff.getText().trim().replace(",", "."));
                noteController.sauvegarderNote(etu.getId(), module.getId(), intitule, valeur, coeff);
                cellLbl.setText(String.format("%.1f", valeur));
                onModuleProfSelectionne(prof, promo);
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.WARNING,
                        "Valeurs invalides. Utilisez des nombres (ex: 14.5, 2.0).").showAndWait();
            } catch (IllegalArgumentException ex) {
                new Alert(Alert.AlertType.WARNING, ex.getMessage()).showAndWait();
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Impossible d'enregistrer la note.").showAndWait();
            }
        });
    }

    // -------------------------------------------------------------------------
    //  UC13 — Dialog ajout contrôle complet
    // -------------------------------------------------------------------------

    private void ouvrirDialogAjouterControle(ModuleEntity module,
                                             PromotionEntity promo,
                                             List<UserEntity> etudiants,
                                             List<String> intitulesExistants,
                                             UserEntity prof) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nouveau contrôle");
        dialog.setHeaderText("Ajouter un contrôle — " + module.getNom()
                + " · " + promo.getNom());

        ButtonType btnValider = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/projet/M1/css/main.css").toExternalForm());
        dialog.getDialogPane().setPrefWidth(500);

        VBox content = new VBox(16);
        content.setPadding(new javafx.geometry.Insets(16));

        // Intitulé
        HBox intituleRow = new HBox(12);
        intituleRow.setAlignment(Pos.CENTER_LEFT);
        Label lblIntitule = new Label("Intitulé *");
        lblIntitule.getStyleClass().add("form-label");
        lblIntitule.setPrefWidth(100);
        TextField fieldIntitule = new TextField();
        fieldIntitule.setPromptText("Ex: DS1, TP noté, Examen final…");
        HBox.setHgrow(fieldIntitule, Priority.ALWAYS);
        intituleRow.getChildren().addAll(lblIntitule, fieldIntitule);

        // Coefficient
        HBox coeffRow = new HBox(12);
        coeffRow.setAlignment(Pos.CENTER_LEFT);
        Label lblCoeff = new Label("Coefficient *");
        lblCoeff.getStyleClass().add("form-label");
        lblCoeff.setPrefWidth(100);
        TextField fieldCoeff = new TextField("1.0");
        fieldCoeff.setPrefWidth(80);
        coeffRow.getChildren().addAll(lblCoeff, fieldCoeff);

        // Tableau de saisie des notes — affiché directement sans ScrollPane
        Label lblNotes = new Label("Notes des étudiants");
        lblNotes.getStyleClass().add("form-step-title");

        GridPane gridNotes = new GridPane();
        gridNotes.setHgap(8);
        gridNotes.setVgap(8);
        gridNotes.setPadding(new javafx.geometry.Insets(8));

        Label hNom  = new Label("Étudiant");
        hNom.getStyleClass().add("notes-cell-header");
        hNom.setPrefWidth(200);
        Label hNote = new Label("Note (0–20)");
        hNote.getStyleClass().add("notes-cell-header");
        hNote.setPrefWidth(120);
        gridNotes.add(hNom, 0, 0);
        gridNotes.add(hNote, 1, 0);

        List<TextField> fieldsNotes = new ArrayList<>();
        for (int i = 0; i < etudiants.size(); i++) {
            UserEntity etu = etudiants.get(i);
            Label nomLbl = new Label(etu.getPrenom() + " " + etu.getNom());
            nomLbl.getStyleClass().add("notes-cell-nom");
            nomLbl.setPrefWidth(200);

            TextField tfNote = new TextField();
            tfNote.setPromptText("—");
            tfNote.setPrefWidth(120);

            gridNotes.add(nomLbl, 0, i + 1);
            gridNotes.add(tfNote, 1, i + 1);
            fieldsNotes.add(tfNote);
        }

        // Directement gridNotes sans ScrollPane
        content.getChildren().addAll(intituleRow, coeffRow, lblNotes, gridNotes);
        dialog.getDialogPane().setContent(content);

        dialog.showAndWait().ifPresent(result -> {
            if (result != btnValider) return;

            String intitule = fieldIntitule.getText().trim();
            if (intitule.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "L'intitulé ne peut pas être vide.").showAndWait();
                return;
            }
            if (intitulesExistants.contains(intitule)) {
                new Alert(Alert.AlertType.WARNING,
                        "Un contrôle nommé \"" + intitule + "\" existe déjà pour ce module.").showAndWait();
                return;
            }

            float coeff;
            try {
                coeff = Float.parseFloat(fieldCoeff.getText().trim().replace(",", "."));
                if (coeff <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.WARNING,
                        "Coefficient invalide. Utilisez un nombre positif.").showAndWait();
                return;
            }

            boolean erreur = false;
            for (int i = 0; i < etudiants.size(); i++) {
                String valStr = fieldsNotes.get(i).getText().trim();
                if (valStr.isEmpty()) continue;
                try {
                    float valeur = Float.parseFloat(valStr.replace(",", "."));
                    noteController.sauvegarderNote(
                            etudiants.get(i).getId(), module.getId(), intitule, valeur, coeff);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    erreur = true;
                }
            }

            if (erreur) {
                new Alert(Alert.AlertType.WARNING,
                        "Certaines notes n'ont pas pu être enregistrées.").showAndWait();
            }

            onModuleProfSelectionne(prof, promo);
        });
    }
}