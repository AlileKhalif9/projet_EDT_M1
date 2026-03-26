package projet.M1.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import projet.M1.model.utilisateur_systeme.Gestionnaire_Planning;
import projet.M1.model.utilisateur_systeme.Utilisateur;
import projet.M1.session.SessionManager;

import java.time.LocalDate;
import java.util.List;

/** Controller de la page "Demandes de modification" — fichier FXML : modification-request.fxml
 * Formulaire en 3 étapes pour qu'un prof demande une modification de son EDT :
 *   Étape 1 → Quelle date ?
 *   Étape 2 → Quel horaire/salle veut-on changer ?
 *   Étape 3 → Par quoi veut-on le remplacer ?
 *
 * Les listes déroulantes sont définies ici directement (front seulement).
 * Le back-end devra les brancher sur les vraies données quand la BDD sera prête.
 * La liste des demandes existantes (requestsContainer) est laissée vide :
 * c'est au back-end de la remplir. */
public class ModificationRequestController {


    //  Listes des choix prédéfinis à remplacer par des appels back-end


    private static final List<String> CRENEAUX = List.of(
            "8h00 – 10h00", "9h00 – 11h00", "10h00 – 12h00",
            "11h00 – 13h00", "13h00 – 15h00", "14h00 – 16h00",
            "15h00 – 17h00", "16h00 – 18h00"
    );

    private static final List<String> SALLES = List.of(
            "Salle 101", "Salle 102", "Salle 305",
            "Amphi A", "Salle TP1", "Salle TP2"
    );

    private static final List<String> RAISONS = List.of(
            "Conflit avec une réunion de département",
            "Besoin d'équipement de laboratoire",
            "Indisponibilité personnelle",
            "Préférence du groupe d'étudiants",
            "Salle inadaptée (capacité insuffisante)",
            "Salle inadaptée (équipement manquant)",
            "Déplacement professionnel",
            "Autre raison"
    );


    //  Composants FXML


    @FXML private Label  labelTitle;
    @FXML private Label  labelSubtitle;
    @FXML private Button btnNouvellesDemande;
    @FXML private VBox   formPanel;
    @FXML private VBox   requestsContainer;


    @FXML private DatePicker datePicker;


    @FXML private ComboBox<String> comboCreneauActuel;
    @FXML private ComboBox<String> comboSalleActuelle;


    @FXML private ComboBox<String> comboNouvelHoraire;
    @FXML private ComboBox<String> comboNouvelleSalle;
    @FXML private ComboBox<String> comboRaison;

    //  Initialisation


    @FXML
    public void initialize() {
        Utilisateur u = SessionManager.getInstance().getUtilisateurConnecte();
        boolean isGestionnaire = u instanceof Gestionnaire_Planning;

        if (isGestionnaire) {
            labelTitle.setText("Demandes de modification");
            labelSubtitle.setText("Examinez et approuvez les demandes en attente");
            btnNouvellesDemande.setVisible(false);
            btnNouvellesDemande.setManaged(false);
        }

        // Remplir les listes
        comboCreneauActuel.getItems().setAll(CRENEAUX);
        comboSalleActuelle.getItems().setAll(SALLES);
        comboNouvelHoraire.getItems().setAll(CRENEAUX);
        comboNouvelleSalle.getItems().setAll(SALLES);
        comboRaison.getItems().setAll(RAISONS);
        datePicker.setValue(LocalDate.now());

        // La liste des demandes sera branchée par le back-end
        // requestsContainer reste vide pour l'instant
    }


    //  Actions


    /** Affiche ou masque le formulaire. */
    @FXML
    private void onToggleForm() {
        boolean estVisible = formPanel.isVisible();
        formPanel.setVisible(!estVisible);
        formPanel.setManaged(!estVisible);
        btnNouvellesDemande.setText(estVisible ? "+ Nouvelle demande" : "Voir les demandes");
    }

    /** Valide et soumet la demande. */
    @FXML
    private void onSubmit() {
        if (comboCreneauActuel.getValue() == null
                || comboNouvelHoraire.getValue() == null
                || comboRaison.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Champs manquants");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez renseigner au minimum le créneau actuel, le nouvel horaire et la raison.");
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Demande soumise");
        alert.setHeaderText(null);
        alert.setContentText("Votre demande a été soumise avec succès.\nElle sera traitée par le gestionnaire.");
        alert.showAndWait();

        resetForm();
        onAnnulerForm();
    }

    /** Masque le formulaire sans soumettre. */
    @FXML
    private void onAnnulerForm() {
        formPanel.setVisible(false);
        formPanel.setManaged(false);
        btnNouvellesDemande.setText("+ Nouvelle demande");
    }


    //  Helpers


    private void resetForm() {
        datePicker.setValue(LocalDate.now());
        comboCreneauActuel.getSelectionModel().clearSelection();
        comboSalleActuelle.getSelectionModel().clearSelection();
        comboNouvelHoraire.getSelectionModel().clearSelection();
        comboNouvelleSalle.getSelectionModel().clearSelection();
        comboRaison.getSelectionModel().clearSelection();
    }
}
