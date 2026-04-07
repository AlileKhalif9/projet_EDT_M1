package projet.M1.ui;

/**
 * Liste des pages de l'appli. Chaque valeur = un fichier FXML dans resources/fxml/.
 */
public enum View {
    DASHBOARD            ("dashboard"),
    TIMETABLE            ("timetable"),
    MODIFICATION_REQUEST ("modification-request"),
    GROUPES              ("groupes"),
    SALLES               ("salles"),
    NOTES                ("notes");

    private final String fxmlName;

    View(String fxmlName) { this.fxmlName = fxmlName; }

    public String getFxmlName() { return fxmlName; }
}