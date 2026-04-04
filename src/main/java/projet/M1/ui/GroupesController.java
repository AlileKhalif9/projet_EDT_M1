package projet.M1.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import projet.M1.BDD.dao.GroupeDAO;
import projet.M1.BDD.entity.GroupeEtudiantEntity;
import projet.M1.BDD.entity.Role;
import projet.M1.BDD.entity.UserEntity;
import projet.M1.controller.GroupeController;

import java.util.List;

/**
 * UC7/US16 — Consulter les groupes d'utilisateurs (Gestionnaire).
 *
 * Affiche la liste des groupes avec :
 *   - nom du groupe
 *   - taille (nb étudiants)
 *   - types d'utilisateurs contenus (Étudiants, Professeurs, Invités)
 *
 * Filtrage disponible :
 *   - par nom (champ texte)
 *   - par taille (Tous / Petit <10 / Moyen 10-30 / Grand >30)
 *   - par type d'utilisateur (Tous / Étudiants / Professeurs / Invités)
 *
 * Clic sur un groupe → dialog avec la liste des membres.
 */
public class GroupesController {

    @FXML private TextField          fieldRecherche;
    @FXML private ComboBox<String>   comboFiltreTaille;
    @FXML private ProgressIndicator  loadingIndicator;
    @FXML private VBox               groupesContainer;

    private final GroupeController groupeController = new GroupeController(new GroupeDAO());

    private List<GroupeEtudiantEntity> tousLesGroupes = List.of();

    @FXML
    public void initialize() {
        // Filtre par taille
        comboFiltreTaille.getItems().setAll("Toutes tailles", "Petit (< 10)", "Moyen (10–30)", "Grand (> 30)");
        comboFiltreTaille.setValue("Toutes tailles");

        // Listeners filtres
        fieldRecherche.textProperty().addListener((obs, o, n) -> appliquerFiltres());
        comboFiltreTaille.setOnAction(e -> appliquerFiltres());

        // Chargement BDD en arrière-plan
        Thread t = new Thread(() -> {
            List<GroupeEtudiantEntity> groupes;
            try {
                groupes = groupeController.getAllGroupes();
            } catch (Exception e) {
                groupes = List.of();
            }
            final List<GroupeEtudiantEntity> result = groupes;
            Platform.runLater(() -> {
                tousLesGroupes = result;
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

    // -------------------------------------------------------------------------
    //  Filtrage UC7
    // -------------------------------------------------------------------------

    private void appliquerFiltres() {
        if (tousLesGroupes.isEmpty()) return;

        String recherche    = fieldRecherche.getText().trim().toLowerCase();
        String filtreTaille = comboFiltreTaille.getValue();

        List<GroupeEtudiantEntity> filtres = tousLesGroupes.stream()
                .filter(g -> {
                    // Filtre nom
                    if (!recherche.isEmpty()) {
                        if (g.getNom() == null || !g.getNom().toLowerCase().contains(recherche))
                            return false;
                    }
                    // Filtre taille
                    int nb = nbEtudiants(g);
                    if (filtreTaille != null) {
                        switch (filtreTaille) {
                            case "Petit (< 10)"   -> { if (nb >= 10) return false; }
                            case "Moyen (10–30)"  -> { if (nb < 10 || nb > 30) return false; }
                            case "Grand (> 30)"   -> { if (nb <= 30) return false; }
                        }
                    }
                    return true;
                })
                .toList();

        afficherGroupes(filtres);
    }

    // -------------------------------------------------------------------------
    //  Affichage liste
    // -------------------------------------------------------------------------

    private void afficherGroupes(List<GroupeEtudiantEntity> groupes) {
        groupesContainer.getChildren().clear();
        if (groupes.isEmpty()) {
            Label vide = new Label("Aucun groupe trouvé.");
            vide.getStyleClass().add("text-muted");
            groupesContainer.getChildren().add(vide);
            return;
        }
        for (GroupeEtudiantEntity g : groupes) {
            groupesContainer.getChildren().add(buildGroupeCard(g));
        }
    }

    private HBox buildGroupeCard(GroupeEtudiantEntity g) {
        HBox card = new HBox(16);
        card.getStyleClass().add("groupe-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(48);
        card.setOnMouseClicked(e -> ouvrirDetail(g));

        // Nom du groupe
        Label nomLabel = new Label(g.getNom() != null ? g.getNom() : "—");
        nomLabel.getStyleClass().add("groupe-card-nom");
        HBox.setHgrow(nomLabel, Priority.ALWAYS);
        nomLabel.setMaxWidth(Double.MAX_VALUE);

        // Taille
        int nb = nbEtudiants(g);
        Label nbLabel = new Label("👥 " + nb + " étudiant" + (nb > 1 ? "s" : ""));
        nbLabel.getStyleClass().add("groupe-card-stats");

        // Types d'utilisateurs présents dans ce groupe
        Label typesLabel = new Label(resumeTypes(g));
        typesLabel.getStyleClass().add("groupe-card-stats");

        Label chevron = new Label("›");
        chevron.getStyleClass().add("quick-action-chevron");

        card.getChildren().addAll(nomLabel, nbLabel, typesLabel, chevron);
        return card;
    }

    /**
     * Retourne un résumé du contenu du groupe (étudiants uniquement).
     */
    private String resumeTypes(GroupeEtudiantEntity g) {
        int nb = nbEtudiants(g);
        return nb > 0 ? "Étudiants" : "—";
    }

    // -------------------------------------------------------------------------
    //  Dialog détail groupe
    // -------------------------------------------------------------------------

    private void ouvrirDetail(GroupeEtudiantEntity g) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Groupe " + g.getNom());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/projet/M1/css/main.css").toExternalForm());
        dialog.getDialogPane().setPrefWidth(560);

        VBox content = new VBox(16);
        content.getStyleClass().add("page-container");

        Label titre = new Label(g.getNom());
        titre.getStyleClass().add("page-title");

        List<UserEntity> membres = safeList(g);
        Label sousTitre = new Label(membres.size() + " membre" + (membres.size() > 1 ? "s" : "")
                + "  ·  " + resumeTypes(g));
        sousTitre.getStyleClass().add("page-subtitle");

        // Tableau membres
        VBox tableau = new VBox(0);
        tableau.getStyleClass().add("groupe-tableau");

        // Header
        HBox headerRow = new HBox();
        headerRow.getStyleClass().add("groupe-tableau-header");
        Label hNom   = new Label("Nom");    hNom.setPrefWidth(220);   hNom.getStyleClass().add("groupe-col-header");
        Label hLogin = new Label("Login");  hLogin.setPrefWidth(140); hLogin.getStyleClass().add("groupe-col-header");
        Label hRole  = new Label("Rôle");   hRole.setPrefWidth(120);  hRole.getStyleClass().add("groupe-col-header");
        headerRow.getChildren().addAll(hNom, hLogin, hRole);
        tableau.getChildren().add(headerRow);

        if (membres.isEmpty()) {
            Label vide = new Label("Aucun membre dans ce groupe.");
            vide.getStyleClass().add("text-muted");
            vide.setStyle("-fx-padding: 12 16 12 16;");
            tableau.getChildren().add(vide);
        } else {
            for (int i = 0; i < membres.size(); i++) {
                UserEntity u = membres.get(i);
                HBox row = new HBox();
                row.getStyleClass().add(i % 2 == 0 ? "groupe-tableau-row" : "groupe-tableau-row-alt");

                // Cellule nom + avatar
                HBox cellNom = new HBox(10);
                cellNom.setPrefWidth(220);
                cellNom.setAlignment(Pos.CENTER_LEFT);
                Label av = buildAvatar(u);
                String nomComplet = ((u.getPrenom() != null ? u.getPrenom() : "")
                        + " " + (u.getNom() != null ? u.getNom() : "")).trim();
                Label nomLbl = new Label(nomComplet.isEmpty() ? "—" : nomComplet);
                nomLbl.getStyleClass().add("groupe-etudiant-nom");
                cellNom.getChildren().addAll(av, nomLbl);

                Label loginLbl = new Label(u.getLogin() != null ? u.getLogin() : "—");
                loginLbl.setPrefWidth(140);
                loginLbl.getStyleClass().add("groupe-etudiant-login");

                Label roleLbl = new Label(roleLabel(u));
                roleLbl.setPrefWidth(120);
                roleLbl.getStyleClass().add("groupe-etudiant-login");

                row.getChildren().addAll(cellNom, loginLbl, roleLbl);
                tableau.getChildren().add(row);
            }
        }

        // Bouton modifier le nom (US17)
        Button btnModifier = new Button("Modifier le nom");
        btnModifier.getStyleClass().add("btn-secondary");
        btnModifier.setOnAction(e -> {
            TextInputDialog inputDialog = new TextInputDialog(g.getNom());
            inputDialog.setTitle("Modifier le groupe");
            inputDialog.setHeaderText(null);
            inputDialog.setContentText("Nouveau nom du groupe :");
            inputDialog.getDialogPane().getStylesheets().add(
                    getClass().getResource("/projet/M1/css/main.css").toExternalForm());

            inputDialog.showAndWait().ifPresent(nouveauNom -> {
                String nom = nouveauNom.trim();
                if (nom.isEmpty()) return;
                titre.setText(nom);
                dialog.setTitle("Groupe " + nom);
                // Mise à jour visuelle sur la carte dans la liste
                groupesContainer.getChildren().stream()
                        .filter(n -> n instanceof HBox)
                        .map(n -> (HBox) n)
                        .forEach(card -> card.getChildren().stream()
                                .filter(n -> n instanceof Label)
                                .map(n -> (Label) n)
                                .filter(l -> l.getStyleClass().contains("groupe-card-nom")
                                        && l.getText().equals(g.getNom()))
                                .findFirst()
                                .ifPresent(l -> l.setText(nom)));
                g.setNom(nom);
            });
        });

        content.getChildren().addAll(titre, sousTitre, btnModifier, tableau);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    // -------------------------------------------------------------------------
    //  Helpers
    // -------------------------------------------------------------------------

    private int nbEtudiants(GroupeEtudiantEntity g) {
        try {
            return g.getList_etudiant() != null ? g.getList_etudiant().size() : 0;
        } catch (Exception ignored) {
            return 0;
        }
    }

    private List<UserEntity> safeList(GroupeEtudiantEntity g) {
        try {
            return g.getList_etudiant() != null ? g.getList_etudiant() : List.of();
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private Label buildAvatar(UserEntity u) {
        String initiales = "";
        if (u.getPrenom() != null && !u.getPrenom().isEmpty())
            initiales += u.getPrenom().charAt(0);
        if (u.getNom() != null && !u.getNom().isEmpty())
            initiales += u.getNom().charAt(0);
        Label av = new Label(initiales.toUpperCase());
        av.getStyleClass().add("avatar-small");
        return av;
    }

    private String roleLabel(UserEntity u) {
        if (u.getRole() == null) return "—";
        return switch (u.getRole()) {
            case ETUDIANT              -> "Étudiant";
            case PROFESSEUR            -> "Professeur";
            case GESTIONNAIRE_PLANNING -> "Gestionnaire";
            case INVITE                -> "Invité";
        };
    }
}
