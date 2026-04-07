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
import projet.M1.BDD.entity.Role;
import projet.M1.BDD.entity.UserEntity;
import projet.M1.controller.NoteController;
import projet.M1.session.SessionManager;

import java.util.*;

/**
 * UC11 — Professeur : consulter les notes de ses étudiants.
 * UC12 — Étudiant   : consulter ses propres notes.
 * UC13 — Professeur : ajouter / modifier les notes.
 *
 * La vue s'adapte automatiquement selon le rôle de l'utilisateur connecté.
 */
public class NotesController {

    @FXML private Label             labelTitle;
    @FXML private Label             labelSubtitle;
    @FXML private ComboBox<String>  comboModule;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private VBox              notesContainer;

    private final NoteController noteController =
            new NoteController(new NoteDAO(), new ModuleDAO());

    /** Map nom module → entity, pour retrouver l'id à la sélection. */
    private final Map<String, ModuleEntity> moduleMap = new LinkedHashMap<>();

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
            labelSubtitle.setText("Consultez et gérez les notes par module");
        } else {
            labelTitle.setText("Mes notes");
            labelSubtitle.setText("Consultez vos notes et vos moyennes par module");
        }

        comboModule.setPromptText("Choisir un module…");
        comboModule.setOnAction(e -> onModuleSelectionne(u));

        // Chargement des modules en arrière-plan
        Thread t = new Thread(() -> {
            List<ModuleEntity> modules;
            try {
                modules = isProf
                        ? noteController.getModulesProfesseur(u.getId())
                        : noteController.getModulesEtudiant(u.getId());
            } catch (Exception ex) {
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
                    comboModule.getItems().add(m.getNom());
                }
                // Sélectionner automatiquement le premier module
                comboModule.getSelectionModel().selectFirst();
            });
        });
        t.setDaemon(true);
        t.start();
    }

    // -------------------------------------------------------------------------
    //  Changement de module
    // -------------------------------------------------------------------------

    private void onModuleSelectionne(UserEntity u) {
        String nomModule = comboModule.getValue();
        if (nomModule == null) return;
        ModuleEntity module = moduleMap.get(nomModule);
        if (module == null) return;

        notesContainer.getChildren().clear();
        loadingIndicator.setVisible(true);
        loadingIndicator.setManaged(true);

        boolean isProf = u.getRole() == Role.PROFESSEUR;

        Thread t = new Thread(() -> {
            try {
                if (isProf) {
                    List<UserEntity>               etudiants  = noteController.getEtudiantsDuModule(module.getId());
                    List<String>                   intitules  = noteController.getIntitulesControles(module.getId());
                    Map<Long, Map<String, NoteEntity>> notesMap = noteController.getNotesParEtudiantEtControle(module.getId());
                    List<NoteEntity>               toutesNotes = new ArrayList<>();
                    notesMap.values().forEach(m -> toutesNotes.addAll(m.values()));

                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        loadingIndicator.setManaged(false);
                        afficherTableauProfesseur(module, etudiants, intitules, notesMap, toutesNotes);
                    });
                } else {
                    List<NoteEntity> notes = noteController.getNotesEtudiantModule(u.getId(), module.getId());
                    List<NoteEntity> toutesNotes = noteController.getToutesNotesEtudiant(u.getId());
                    // Moyenne de classe = toutes les notes de ce module
                    List<NoteEntity> notesModule = new ArrayList<>();
                    try {
                        notesModule.addAll(new NoteDAO().findByModule(module.getId()));
                    } catch (Exception ignored) {}
                    final List<NoteEntity> notesModuleFinal = notesModule;

                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        loadingIndicator.setManaged(false);
                        afficherTableauEtudiant(module, notes, notesModuleFinal);
                    });
                }
            } catch (Exception ex) {
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
    //  UC11 — Tableau professeur (vue complète + gestion UC13)
    // -------------------------------------------------------------------------

    private void afficherTableauProfesseur(ModuleEntity module,
                                           List<UserEntity> etudiants,
                                           List<String> intitules,
                                           Map<Long, Map<String, NoteEntity>> notesMap,
                                           List<NoteEntity> toutesNotes) {
        notesContainer.getChildren().clear();

        // Bouton "Ajouter un contrôle"
        Button btnAjouter = new Button("+ Ajouter un contrôle");
        btnAjouter.getStyleClass().add("btn-secondary");
        btnAjouter.setOnAction(e -> ouvrirDialogAjouterControle(module, etudiants, intitules));

        HBox toolbar = new HBox(12, btnAjouter);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        if (etudiants.isEmpty()) {
            Label vide = new Label("Aucun étudiant inscrit à ce module.");
            vide.getStyleClass().add("text-muted");
            notesContainer.getChildren().addAll(toolbar, vide);
            return;
        }

        if (intitules.isEmpty()) {
            Label vide = new Label("Aucune note enregistrée pour ce module. Ajoutez un contrôle.");
            vide.getStyleClass().add("text-muted");
            notesContainer.getChildren().addAll(toolbar, vide);
            return;
        }

        // Tableau à double entrée
        GridPane grid = buildGridBase(intitules, true);

        // Ligne par étudiant
        for (int row = 0; row < etudiants.size(); row++) {
            UserEntity etu = etudiants.get(row);
            String nomComplet = etu.getPrenom() + " " + etu.getNom();

            Label nomLbl = new Label(nomComplet);
            nomLbl.getStyleClass().add("notes-cell-nom");
            nomLbl.setPrefWidth(160);
            grid.add(nomLbl, 0, row + 1);

            Map<String, NoteEntity> notesEtu = notesMap.getOrDefault(etu.getId(), Map.of());
            List<NoteEntity> notesEtuList = new ArrayList<>(notesEtu.values());

            for (int col = 0; col < intitules.size(); col++) {
                String intitule = intitules.get(col);
                NoteEntity note  = notesEtu.get(intitule);

                Label cellLbl = new Label(note != null ? String.format("%.1f", note.getValeur()) : "—");
                cellLbl.getStyleClass().add(row % 2 == 0 ? "notes-cell" : "notes-cell-alt");
                cellLbl.setAlignment(Pos.CENTER);
                cellLbl.setPrefWidth(80);

                // Clic pour modifier la note
                final int finalCol = col;
                cellLbl.setStyle(cellLbl.getStyle() + "-fx-cursor: hand;");
                cellLbl.setOnMouseClicked(ev ->
                        ouvrirDialogSaisirNote(module, etu, intitules.get(finalCol),
                                note, cellLbl));
                grid.add(cellLbl, col + 1, row + 1);
            }

            // Colonne moyenne étudiant
            float moy = noteController.calculerMoyenne(notesEtuList);
            Label moyLbl = new Label(moy >= 0 ? String.format("%.2f", moy) : "—");
            moyLbl.getStyleClass().add("notes-cell-moyenne");
            moyLbl.setAlignment(Pos.CENTER);
            moyLbl.setPrefWidth(80);
            grid.add(moyLbl, intitules.size() + 1, row + 1);
        }

        // Ligne moyenne de classe par contrôle
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
        grid.add(new Label(""), intitules.size() + 1, lastRow);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(false);
        scroll.setFitToHeight(false);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        notesContainer.getChildren().addAll(toolbar, scroll);
    }

    // -------------------------------------------------------------------------
    //  UC12 — Tableau étudiant (ses notes + moyenne classe)
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

        GridPane grid = buildGridBase(intitules, false);

        // Ligne "Ma note"
        Label maNomLbl = new Label("Ma note");
        maNomLbl.getStyleClass().add("notes-cell-nom");
        maNomLbl.setPrefWidth(160);
        grid.add(maNomLbl, 0, 1);

        Map<String, NoteEntity> mesNotesMap = new HashMap<>();
        for (NoteEntity n : mesNotes) mesNotesMap.put(n.getIntitule(), n);

        for (int col = 0; col < intitules.size(); col++) {
            NoteEntity note = mesNotesMap.get(intitules.get(col));
            Label cellLbl = new Label(note != null ? String.format("%.1f", note.getValeur()) : "—");
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

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(false);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        notesContainer.getChildren().add(scroll);
    }

    // -------------------------------------------------------------------------
    //  Helpers tableau
    // -------------------------------------------------------------------------

    /**
     * Construit le GridPane avec la ligne d'en-tête (intitulés + Moyenne).
     * @param withMoyenne true pour le tableau prof (colonne Moyenne par étudiant)
     */
    private GridPane buildGridBase(List<String> intitules, boolean withMoyenne) {
        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);
        grid.getStyleClass().add("notes-grid");

        // En-tête colonne 0 : Étudiant/—
        Label hEtu = new Label("Étudiant");
        hEtu.getStyleClass().add("notes-cell-header");
        hEtu.setPrefWidth(160);
        grid.add(hEtu, 0, 0);

        // En-têtes contrôles
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
    //  UC13 — Dialogs saisie / ajout contrôle
    // -------------------------------------------------------------------------

    /**
     * Dialog pour saisir ou modifier la note d'un étudiant pour un contrôle.
     * Met à jour la cellule visuellement et persiste en BDD.
     */
    private void ouvrirDialogSaisirNote(ModuleEntity module, UserEntity etu,
                                        String intitule, NoteEntity noteExistante,
                                        Label cellLbl) {
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

        TextField fieldNote  = new TextField(noteExistante != null
                ? String.valueOf(noteExistante.getValeur()) : "");
        fieldNote.setPromptText("Note sur 20");

        TextField fieldCoeff = new TextField(noteExistante != null
                ? String.valueOf(noteExistante.getCoefficient()) : "1.0");
        fieldCoeff.setPromptText("Coefficient");

        form.add(new Label("Note (0–20) *"),  0, 0); form.add(fieldNote,  1, 0);
        form.add(new Label("Coefficient *"),   0, 1); form.add(fieldCoeff, 1, 1);
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
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.WARNING,
                        "Valeurs invalides. Utilisez des nombres (ex: 14.5, 2.0).").showAndWait();
            } catch (IllegalArgumentException ex) {
                new Alert(Alert.AlertType.WARNING, ex.getMessage()).showAndWait();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR,
                        "Impossible d'enregistrer la note.").showAndWait();
            }
        });
    }

    /**
     * Dialog pour créer un nouveau contrôle avec les notes de tous les étudiants.
     * Après validation, recharge le tableau complet.
     */
    private void ouvrirDialogAjouterControle(ModuleEntity module,
                                             List<UserEntity> etudiants,
                                             List<String> intitulesExistants) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nouveau contrôle");
        dialog.setHeaderText("Ajouter un contrôle pour " + module.getNom());

        ButtonType btnValider = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/projet/M1/css/main.css").toExternalForm());
        dialog.getDialogPane().setPrefWidth(500);

        VBox content = new VBox(16);
        content.setPadding(new javafx.geometry.Insets(16));

        // Intitulé du contrôle
        HBox intituleRow = new HBox(12);
        intituleRow.setAlignment(Pos.CENTER_LEFT);
        Label lblIntitule = new Label("Intitulé *");
        lblIntitule.getStyleClass().add("form-label");
        lblIntitule.setPrefWidth(100);
        TextField fieldIntitule = new TextField();
        fieldIntitule.setPromptText("Ex: DS1, TP noté, Examen final…");
        HBox.setHgrow(fieldIntitule, Priority.ALWAYS);
        intituleRow.getChildren().addAll(lblIntitule, fieldIntitule);

        // Coefficient commun
        HBox coeffRow = new HBox(12);
        coeffRow.setAlignment(Pos.CENTER_LEFT);
        Label lblCoeff = new Label("Coefficient *");
        lblCoeff.getStyleClass().add("form-label");
        lblCoeff.setPrefWidth(100);
        TextField fieldCoeff = new TextField("1.0");
        fieldCoeff.setPrefWidth(80);
        coeffRow.getChildren().addAll(lblCoeff, fieldCoeff);

        // Tableau de saisie des notes
        Label lblNotes = new Label("Notes des étudiants");
        lblNotes.getStyleClass().add("form-step-title");

        GridPane gridNotes = new GridPane();
        gridNotes.setHgap(8);
        gridNotes.setVgap(6);

        Label hNom  = new Label("Étudiant");    hNom.getStyleClass().add("notes-cell-header");  hNom.setPrefWidth(180);
        Label hNote = new Label("Note (0–20)"); hNote.getStyleClass().add("notes-cell-header"); hNote.setPrefWidth(100);
        gridNotes.add(hNom, 0, 0);
        gridNotes.add(hNote, 1, 0);

        List<TextField> fieldsNotes = new ArrayList<>();
        for (int i = 0; i < etudiants.size(); i++) {
            UserEntity etu = etudiants.get(i);
            Label nomLbl = new Label(etu.getPrenom() + " " + etu.getNom());
            nomLbl.getStyleClass().add("notes-cell-nom");
            nomLbl.setPrefWidth(180);
            TextField tfNote = new TextField();
            tfNote.setPromptText("—");
            tfNote.setPrefWidth(100);
            gridNotes.add(nomLbl, 0, i + 1);
            gridNotes.add(tfNote, 1, i + 1);
            fieldsNotes.add(tfNote);
        }

        ScrollPane scrollNotes = new ScrollPane(gridNotes);
        scrollNotes.setFitToWidth(true);
        scrollNotes.setPrefHeight(Math.min(etudiants.size() * 40 + 40, 280));
        scrollNotes.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        content.getChildren().addAll(intituleRow, coeffRow, lblNotes, scrollNotes);
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

            // Sauvegarder les notes saisies
            boolean erreur = false;
            for (int i = 0; i < etudiants.size(); i++) {
                String valStr = fieldsNotes.get(i).getText().trim();
                if (valStr.isEmpty()) continue; // pas de note pour cet étudiant
                try {
                    float valeur = Float.parseFloat(valStr.replace(",", "."));
                    noteController.sauvegarderNote(
                            etudiants.get(i).getId(), module.getId(), intitule, valeur, coeff);
                } catch (NumberFormatException ex) {
                    erreur = true;
                } catch (Exception ex) {
                    erreur = true;
                }
            }

            if (erreur) {
                new Alert(Alert.AlertType.WARNING,
                        "Certaines notes n'ont pas pu être enregistrées (valeurs invalides).").showAndWait();
            }

            // Recharger le tableau
            UserEntity u = SessionManager.getInstance().getUtilisateurConnecte();
            onModuleSelectionne(u);
        });
    }
}