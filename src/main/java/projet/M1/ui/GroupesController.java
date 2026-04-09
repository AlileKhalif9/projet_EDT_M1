package projet.M1.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import projet.M1.BDD.dao.GroupeDAO;
import projet.M1.BDD.dao.UserDAO;
import projet.M1.BDD.entity.GroupeEtudiantEntity;
import projet.M1.BDD.entity.Role;
import projet.M1.BDD.entity.UserEntity;
import projet.M1.controller.GroupeController;

import java.util.ArrayList;
import java.util.List;

/**
 * UC8/US16+17 — Consulter et gérer les groupes (Gestionnaire).
 * Filtres : nom, taille.
 * Clic → dialog avec membres + modifier nom + ajouter/retirer membres.
 * Bouton "+ Nouveau groupe" pour créer un groupe (front uniquement).
 */
public class GroupesController {

    @FXML private TextField          fieldRecherche;
    @FXML private ComboBox<String>   comboFiltreTaille;
    @FXML private ProgressIndicator  loadingIndicator;
    @FXML private VBox               groupesContainer;
    @FXML private Button             btnNouveauGroupe;

    private final GroupeController groupeController = new GroupeController(new GroupeDAO());
    private final UserDAO userDAO = new UserDAO();

    private List<GroupeEtudiantEntity> tousLesGroupes = new ArrayList<>();
    private List<UserEntity> tousLesEtudiants = List.of();

    @FXML
    public void initialize() {
        comboFiltreTaille.getItems().setAll("Toutes tailles", "Petit (< 10)", "Moyen (10–30)", "Grand (> 30)");
        comboFiltreTaille.setValue("Toutes tailles");

        fieldRecherche.textProperty().addListener((obs, o, n) -> appliquerFiltres());
        comboFiltreTaille.setOnAction(e -> appliquerFiltres());

        Thread t = new Thread(() -> {
            List<GroupeEtudiantEntity> groupes;
            List<UserEntity> etudiants;
            try { groupes = groupeController.getAllGroupes(); } catch (Exception e) { groupes = List.of(); }
            try { etudiants = userDAO.findByRole(Role.ETUDIANT); } catch (Exception e) { etudiants = List.of(); }
            final List<GroupeEtudiantEntity> g = groupes;
            final List<UserEntity> e = etudiants;
            Platform.runLater(() -> {
                tousLesGroupes = new ArrayList<>(g);
                tousLesEtudiants = e;
                loadingIndicator.setVisible(false);
                loadingIndicator.setManaged(false);
                groupesContainer.setVisible(true);
                groupesContainer.setManaged(true);
                afficherGroupes(tousLesGroupes);
            });
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void onNouveauGroupe() {
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("Nouveau groupe");
        inputDialog.setHeaderText(null);
        inputDialog.setContentText("Nom du groupe :");
        inputDialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/projet/M1/css/main.css").toExternalForm());
        inputDialog.showAndWait().ifPresent(nom -> {
            String nomTrim = nom.trim();
            if (nomTrim.isEmpty()) { showAlert("Nom vide", "Veuillez saisir un nom."); return; }
            if (tousLesGroupes.stream().anyMatch(gg -> nomTrim.equalsIgnoreCase(gg.getNom()))) {
                showAlert("Nom déjà utilisé", "Un groupe avec ce nom existe déjà."); return;
            }
            try {
                GroupeEtudiantEntity nouveau = groupeController.creerGroupe(nomTrim);
                nouveau.setList_etudiant(new ArrayList<>());
                tousLesGroupes.add(nouveau);
                appliquerFiltres();
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de créer le groupe : " + e.getMessage());
            }
        });
    }

    private void appliquerFiltres() {
        if (tousLesGroupes.isEmpty()) return;
        String recherche    = fieldRecherche.getText().trim().toLowerCase();
        String filtreTaille = comboFiltreTaille.getValue();
        List<GroupeEtudiantEntity> filtres = tousLesGroupes.stream()
                .filter(g -> {
                    if (!recherche.isEmpty() && (g.getNom() == null || !g.getNom().toLowerCase().contains(recherche))) return false;
                    int nb = nbEtudiants(g);
                    if (filtreTaille != null) switch (filtreTaille) {
                        case "Petit (< 10)"   -> { if (nb >= 10) return false; }
                        case "Moyen (10–30)"  -> { if (nb < 10 || nb > 30) return false; }
                        case "Grand (> 30)"   -> { if (nb <= 30) return false; }
                    }
                    return true;
                }).toList();
        afficherGroupes(filtres);
    }

    private void afficherGroupes(List<GroupeEtudiantEntity> groupes) {
        groupesContainer.getChildren().clear();
        if (groupes.isEmpty()) {
            Label vide = new Label("Aucun groupe trouvé.");
            vide.getStyleClass().add("text-muted");
            groupesContainer.getChildren().add(vide);
            return;
        }
        for (GroupeEtudiantEntity g : groupes) groupesContainer.getChildren().add(buildGroupeCard(g));
    }

    private HBox buildGroupeCard(GroupeEtudiantEntity g) {
        HBox card = new HBox(16);
        card.getStyleClass().add("groupe-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(48);
        card.setOnMouseClicked(e -> ouvrirDetail(g));

        Label nomLabel = new Label(g.getNom() != null ? g.getNom() : "—");
        nomLabel.getStyleClass().add("groupe-card-nom");
        HBox.setHgrow(nomLabel, Priority.ALWAYS);
        nomLabel.setMaxWidth(Double.MAX_VALUE);

        int nb = nbEtudiants(g);
        Label nbLabel = new Label("👥 " + nb + " étudiant" + (nb > 1 ? "s" : ""));
        nbLabel.getStyleClass().add("groupe-card-stats");

        Label typesLabel = new Label(resumeTypes(g));
        typesLabel.getStyleClass().add("groupe-card-stats");

        Label chevron = new Label("›");
        chevron.getStyleClass().add("quick-action-chevron");

        card.getChildren().addAll(nomLabel, nbLabel, typesLabel, chevron);
        return card;
    }

    private void ouvrirDetail(GroupeEtudiantEntity g) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Groupe " + g.getNom());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/projet/M1/css/main.css").toExternalForm());
        dialog.getDialogPane().setPrefWidth(600);

        VBox content = new VBox(16);
        content.getStyleClass().add("page-container");

        Label titre = new Label(g.getNom());
        titre.getStyleClass().add("page-title");

        List<UserEntity> membres = new ArrayList<>(safeList(g));

        Label sousTitre = new Label(membres.size() + " membre" + (membres.size() > 1 ? "s" : "")
                + "  ·  " + resumeTypes(g));
        sousTitre.getStyleClass().add("page-subtitle");

        VBox tableau = new VBox(0);
        tableau.getStyleClass().add("groupe-tableau");

        HBox headerRow = new HBox();
        headerRow.getStyleClass().add("groupe-tableau-header");
        Label hNom   = new Label("Nom");    hNom.setPrefWidth(200);   hNom.getStyleClass().add("groupe-col-header");
        Label hLogin = new Label("Login");  hLogin.setPrefWidth(140); hLogin.getStyleClass().add("groupe-col-header");
        Label hRole  = new Label("Rôle");   hRole.setPrefWidth(100);  hRole.getStyleClass().add("groupe-col-header");
        Label hAct   = new Label("");       hAct.setPrefWidth(74);
        headerRow.getChildren().addAll(hNom, hLogin, hRole, hAct);
        tableau.getChildren().add(headerRow);

        remplirTableau(tableau, membres, sousTitre, g);

        Button btnModifier = new Button("Modifier le nom");
        btnModifier.getStyleClass().add("btn-secondary");
        btnModifier.setOnAction(e -> {
            TextInputDialog inp = new TextInputDialog(g.getNom());
            inp.setTitle("Modifier le groupe"); inp.setHeaderText(null);
            inp.setContentText("Nouveau nom du groupe :");
            inp.getDialogPane().getStylesheets().add(getClass().getResource("/projet/M1/css/main.css").toExternalForm());
            inp.showAndWait().ifPresent(nv -> {
                String nom = nv.trim(); if (nom.isEmpty()) return;
                String ancienNom = g.getNom();
                try {
                    if (g.getId() != null) groupeController.renommerGroupe(g.getId(), nom);
                } catch (Exception ex) {
                    showAlert("Erreur", "Impossible de renommer le groupe : " + ex.getMessage()); return;
                }
                titre.setText(nom); dialog.setTitle("Groupe " + nom);
                groupesContainer.getChildren().stream().filter(n -> n instanceof HBox).map(n -> (HBox) n)
                        .forEach(card -> card.getChildren().stream().filter(n -> n instanceof Label).map(n -> (Label) n)
                                .filter(l -> l.getStyleClass().contains("groupe-card-nom") && l.getText().equals(ancienNom))
                                .findFirst().ifPresent(l -> l.setText(nom)));
                g.setNom(nom);
            });
        });

        Button btnAjouter = new Button("+ Ajouter un étudiant");
        btnAjouter.getStyleClass().add("btn-primary");
        btnAjouter.setOnAction(e -> ouvrirAjoutMembre(membres, tableau, sousTitre, g));

        HBox actions = new HBox(12, btnModifier, btnAjouter);
        actions.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(titre, sousTitre, actions, tableau);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void remplirTableau(VBox tableau, List<UserEntity> membres, Label sousTitre, GroupeEtudiantEntity g) {
        if (tableau.getChildren().size() > 1)
            tableau.getChildren().subList(1, tableau.getChildren().size()).clear();
        sousTitre.setText(membres.size() + " membre" + (membres.size() > 1 ? "s" : "") + "  ·  " + resumeTypes(g));
        if (membres.isEmpty()) {
            Label vide = new Label("Aucun membre dans ce groupe.");
            vide.getStyleClass().add("text-muted"); vide.setStyle("-fx-padding: 12 16 12 16;");
            tableau.getChildren().add(vide);
        } else {
            for (int i = 0; i < membres.size(); i++)
                tableau.getChildren().add(buildMembreRow(membres.get(i), i, membres, tableau, sousTitre, g));
        }
        rafraichirCarte(g, membres.size());
    }

    private HBox buildMembreRow(UserEntity u, int index, List<UserEntity> membres,
                                VBox tableau, Label sousTitre, GroupeEtudiantEntity g) {
        HBox row = new HBox();
        row.getStyleClass().add(index % 2 == 0 ? "groupe-tableau-row" : "groupe-tableau-row-alt");

        HBox cellNom = new HBox(10); cellNom.setPrefWidth(200); cellNom.setAlignment(Pos.CENTER_LEFT);
        Label av = buildAvatar(u);
        String nc = ((u.getPrenom() != null ? u.getPrenom() : "") + " " + (u.getNom() != null ? u.getNom() : "")).trim();
        Label nomLbl = new Label(nc.isEmpty() ? "—" : nc); nomLbl.getStyleClass().add("groupe-etudiant-nom");
        cellNom.getChildren().addAll(av, nomLbl);

        Label loginLbl = new Label(u.getLogin() != null ? u.getLogin() : "—");
        loginLbl.setPrefWidth(140); loginLbl.getStyleClass().add("groupe-etudiant-login");

        Label roleLbl = new Label(roleLabel(u)); roleLbl.setPrefWidth(100); roleLbl.getStyleClass().add("groupe-etudiant-login");

        Button btnRetirer = new Button("Retirer"); btnRetirer.getStyleClass().add("btn-danger"); btnRetirer.setPrefWidth(68);
        btnRetirer.setOnAction(e -> {
            try {
                if (u.getId() != null) groupeController.retirerMembre(u.getId());
            } catch (Exception ex) {
                showAlert("Erreur", "Impossible de retirer le membre : " + ex.getMessage()); return;
            }
            membres.remove(u);
            Platform.runLater(() -> remplirTableau(tableau, membres, sousTitre, g));
        });

        row.getChildren().addAll(cellNom, loginLbl, roleLbl, btnRetirer);
        return row;
    }

    private void ouvrirAjoutMembre(List<UserEntity> membres, VBox tableau, Label sousTitre, GroupeEtudiantEntity g) {
        if (tousLesEtudiants.isEmpty()) { showAlert("Données non disponibles", "Liste des étudiants non chargée."); return; }
        List<UserEntity> disponibles = tousLesEtudiants.stream()
                .filter(u -> membres.stream().noneMatch(m -> m.getId() != null && m.getId().equals(u.getId()))).toList();
        if (disponibles.isEmpty()) { showAlert("Aucun disponible", "Tous les étudiants sont déjà membres."); return; }

        Dialog<UserEntity> picker = new Dialog<>();
        picker.setTitle("Ajouter un étudiant");
        picker.getDialogPane().getStylesheets().add(getClass().getResource("/projet/M1/css/main.css").toExternalForm());
        picker.getDialogPane().setPrefWidth(440);
        picker.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField searchField = new TextField(); searchField.setPromptText("Rechercher…"); searchField.getStyleClass().add("login-field");
        ListView<UserEntity> listView = new ListView<>(); listView.getItems().setAll(disponibles); listView.setPrefHeight(260);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(UserEntity u, boolean empty) {
                super.updateItem(u, empty); if (empty || u == null) { setText(null); return; }
                String nc = ((u.getPrenom() != null ? u.getPrenom() : "") + " " + (u.getNom() != null ? u.getNom() : "")).trim();
                setText(nc + " (" + (u.getLogin() != null ? u.getLogin() : "—") + ")");
            }
        });
        searchField.textProperty().addListener((obs, o, n) -> {
            String q = n.trim().toLowerCase();
            listView.getItems().setAll(disponibles.stream().filter(u -> {
                String nc = ((u.getPrenom() != null ? u.getPrenom() : "") + " " + (u.getNom() != null ? u.getNom() : "")).toLowerCase();
                String login = u.getLogin() != null ? u.getLogin().toLowerCase() : "";
                return q.isEmpty() || nc.contains(q) || login.contains(q);
            }).toList());
        });

        Button okBtn = (Button) picker.getDialogPane().lookupButton(ButtonType.OK); okBtn.setDisable(true);
        listView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> okBtn.setDisable(n == null));

        VBox pickerContent = new VBox(12, searchField, listView); pickerContent.getStyleClass().add("page-container");
        picker.getDialogPane().setContent(pickerContent);
        picker.setResultConverter(bt -> bt == ButtonType.OK ? listView.getSelectionModel().getSelectedItem() : null);
        picker.showAndWait().ifPresent(etudiant -> {
            try {
                if (g.getId() != null && etudiant.getId() != null)
                    groupeController.ajouterMembre(g.getId(), etudiant.getId());
            } catch (Exception ex) {
                showAlert("Erreur", "Impossible d'ajouter le membre : " + ex.getMessage()); return;
            }
            membres.add(etudiant);
            Platform.runLater(() -> remplirTableau(tableau, membres, sousTitre, g));
        });
    }

    private void rafraichirCarte(GroupeEtudiantEntity g, int nb) {
        groupesContainer.getChildren().stream().filter(n -> n instanceof HBox).map(n -> (HBox) n)
                .forEach(card -> {
                    boolean match = card.getChildren().stream().filter(n -> n instanceof Label).map(n -> (Label) n)
                            .anyMatch(l -> l.getStyleClass().contains("groupe-card-nom") && l.getText().equals(g.getNom() != null ? g.getNom() : "—"));
                    if (match) card.getChildren().stream().filter(n -> n instanceof Label).map(n -> (Label) n)
                            .filter(l -> l.getStyleClass().contains("groupe-card-stats") && l.getText().startsWith("👥"))
                            .findFirst().ifPresent(l -> l.setText("👥 " + nb + " étudiant" + (nb > 1 ? "s" : "")));
                });
    }

    private int nbEtudiants(GroupeEtudiantEntity g) {
        try { return g.getList_etudiant() != null ? g.getList_etudiant().size() : 0; } catch (Exception e) { return 0; }
    }
    private List<UserEntity> safeList(GroupeEtudiantEntity g) {
        try { return g.getList_etudiant() != null ? g.getList_etudiant() : List.of(); } catch (Exception e) { return List.of(); }
    }
    private String resumeTypes(GroupeEtudiantEntity g) { return nbEtudiants(g) > 0 ? "Étudiants" : "—"; }
    private Label buildAvatar(UserEntity u) {
        String i = ""; if (u.getPrenom() != null && !u.getPrenom().isEmpty()) i += u.getPrenom().charAt(0);
        if (u.getNom() != null && !u.getNom().isEmpty()) i += u.getNom().charAt(0);
        Label av = new Label(i.toUpperCase()); av.getStyleClass().add("avatar-small"); return av;
    }
    private String roleLabel(UserEntity u) {
        if (u.getRole() == null) return "—";
        return switch (u.getRole()) { case ETUDIANT -> "Étudiant"; case PROFESSEUR -> "Professeur";
            case GESTIONNAIRE_PLANNING -> "Gestionnaire"; case INVITE -> "Invité"; };
    }
    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING); alert.setTitle(titre); alert.setHeaderText(null); alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/projet/M1/css/main.css").toExternalForm());
        alert.showAndWait();
    }
}
