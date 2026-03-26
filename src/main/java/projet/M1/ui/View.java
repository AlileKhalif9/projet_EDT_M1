package projet.M1.ui;

/**
 * Liste des pages de l'appli. Chaque valeur = un fichier FXML dans resources/fxml/.
 *
 * Pour ajouter une page : ajouter une valeur ici + créer le FXML + le controller.
 */
public enum View {
    DASHBOARD            ("dashboard"),
    TIMETABLE            ("timetable"),
    ROOM_SELECTION       ("room-selection"),
    MODIFICATION_REQUEST ("modification-request");

    private final String fxmlName;

    View(String fxmlName) { this.fxmlName = fxmlName; }

    public String getFxmlName() { return fxmlName; }
}
