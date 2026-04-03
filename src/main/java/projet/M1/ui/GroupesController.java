package projet.M1.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import projet.M1.BDD.dao.GroupeDAO;
import projet.M1.BDD.entity.GroupeEtudiantEntity;
import projet.M1.BDD.entity.UserEntity;
import projet.M1.controller.GroupeController;

import java.util.List;

/**
 * US16 — Consulter les groupes (Gestionnaire).
 * Affiche la liste des groupes avec leur nombre d'étudiants.
 * Clic sur un groupe → dialog avec la liste des membres.
 */
public class GroupesController {

    @FXML private TextField fieldRecherche;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private VBox groupesContainer;

    private final GroupeController groupeController = new GroupeController(new GroupeDAO());

    private List<GroupeEtudiantEntity> tousLesGroupes = List.of();

    @FXML
    public void initialize() {
        fieldRecherche.textProperty().addListener((obs, oldVal, newVal) -> {
            if (tousLesGroupes.isEmpty()) return;
            String query = newVal.trim().toLowerCase();
            if (query.isEmpty()) {
                afficherGroupes(tousLesGroupes);
                return;
            }
            List<GroupeEtudiantEntity> filtres = tousLesGroupes.stream()
                    .filter(g -> g.getNom() != null && g.getNom().toLowerCase().contains(query))
                    .toList();
            afficherGroupes(filtres);
        });

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

        Label nomLabel = new Label(g.getNom() != null ? g.getNom() : "—");
        nomLabel.getStyleClass().add("groupe-card-nom");
        HBox.setHgrow(nomLabel, Priority.ALWAYS);
        nomLabel.setMaxWidth(Double.MAX_VALUE);

        int nbEtudiants = 0;
        try { nbEtudiants = g.getList_etudiant() != null ? g.getList_etudiant().size() : 0; }
        catch (Exception ignored) {}
        Label nbLabel = new Label("👥 " + nbEtudiants + " étudiant" + (nbEtudiants > 1 ? "s" : ""));
        nbLabel.getStyleClass().add("groupe-card-stats");

        Label chevron = new Label("›");
        chevron.getStyleClass().add("quick-action-chevron");

        card.getChildren().addAll(nomLabel, nbLabel, chevron);
        return card;
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

    private void ouvrirDetail(GroupeEtudiantEntity g) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Groupe " + g.getNom());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/projet/M1/css/main.css").toExternalForm());
        dialog.getDialogPane().setPrefWidth(560);

        VBox content = new VBox(16);
        content.getStyleClass().add("page-container");

        // Titre
        Label titre = new Label(g.getNom());
        titre.getStyleClass().add("page-title");

        List<UserEntity> etudiants = List.of();
        try { etudiants = g.getList_etudiant() != null ? g.getList_etudiant() : List.of(); }
        catch (Exception ignored) {}
        final List<UserEntity> etudiantsFinal = etudiants;
        Label sousTitre = new Label(etudiantsFinal.size() + " étudiant" + (etudiantsFinal.size() > 1 ? "s" : ""));
        sousTitre.getStyleClass().add("page-subtitle");

        // Tableau étudiants
        VBox tableau = new VBox(0);
        tableau.getStyleClass().add("groupe-tableau");

        // Header tableau
        HBox headerRow = new HBox();
        headerRow.getStyleClass().add("groupe-tableau-header");
        Label hNom    = new Label("Nom");       hNom.setPrefWidth(200);    hNom.getStyleClass().add("groupe-col-header");
        Label hLogin  = new Label("Login");     hLogin.setPrefWidth(140);  hLogin.getStyleClass().add("groupe-col-header");
        headerRow.getChildren().addAll(hNom, hLogin);
        tableau.getChildren().add(headerRow);

        if (etudiantsFinal.isEmpty()) {
            Label vide = new Label("Aucun étudiant dans ce groupe.");
            vide.getStyleClass().add("text-muted");
            vide.setStyle("-fx-padding: 12 16 12 16;");
            tableau.getChildren().add(vide);
        } else {
            for (int i = 0; i < etudiantsFinal.size(); i++) {
                UserEntity u = etudiantsFinal.get(i);
                HBox row = new HBox();
                row.getStyleClass().add(i % 2 == 0 ? "groupe-tableau-row" : "groupe-tableau-row-alt");

                HBox cellNom = new HBox(10);
                cellNom.setPrefWidth(200);
                cellNom.setAlignment(Pos.CENTER_LEFT);
                Label av = buildAvatar(u);
                String nomComplet = (u.getPrenom() != null ? u.getPrenom() : "")
                        + " " + (u.getNom() != null ? u.getNom() : "");
                Label nomLbl = new Label(nomComplet.trim());
                nomLbl.getStyleClass().add("groupe-etudiant-nom");
                cellNom.getChildren().addAll(av, nomLbl);

                Label loginLbl = new Label(u.getLogin() != null ? u.getLogin() : "—");
                loginLbl.setPrefWidth(140);
                loginLbl.getStyleClass().add("groupe-etudiant-login");

                row.getChildren().addAll(cellNom, loginLbl);
                tableau.getChildren().add(row);
            }
        }

        // US17 — Bouton modifier le nom du groupe
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
                // Mise à jour visuelle dans le dialog
                titre.setText(nom);
                dialog.setTitle("Groupe " + nom);
                // Mise à jour visuelle sur la carte dans la liste
                groupesContainer.getChildren().stream()
                        .filter(n -> n instanceof VBox)
                        .map(n -> (VBox) n)
                        .forEach(card -> {
                            card.getChildren().stream()
                                    .filter(n -> n instanceof HBox)
                                    .map(n -> (HBox) n)
                                    .flatMap(hbox -> hbox.getChildren().stream())
                                    .filter(n -> n instanceof Label)
                                    .map(n -> (Label) n)
                                    .filter(l -> l.getStyleClass().contains("groupe-card-nom")
                                            && l.getText().equals(g.getNom()))
                                    .findFirst()
                                    .ifPresent(l -> l.setText(nom));
                        });
                g.setNom(nom);
            });
        });

        content.getChildren().addAll(titre, sousTitre, btnModifier, tableau);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }
}
