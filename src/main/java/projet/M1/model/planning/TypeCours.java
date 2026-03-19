package projet.M1.model.planning;

/**
 *je n'ai rien modifié dans le package model/planning.
 *
 * J'en avais besoin pour colorer les cours dans la grille EDT
 * (CM en bleu, TD en vert, TP en violet…).
 * Si vous voulez l'intégrer dans Cours.java plus tard, n'hésitez pas.
 */
public enum TypeCours {

    CM    ("CM",    "#3B82F6", "#EFF6FF"), // bleu
    TD    ("TD",    "#10B981", "#ECFDF5"), // vert
    TP    ("TP",    "#8B5CF6", "#F5F3FF"), // violet
    EXAMEN("Exam", "#F59E0B", "#FFFBEB"), // orange
    ANNULE("Ann.", "#9CA3AF", "#F3F4F6"); // gris

    private final String libelle;
    private final String couleurBordure; // couleur principale du bloc
    private final String couleurFond;   // couleur de fond (version claire)

    TypeCours(String libelle, String couleurBordure, String couleurFond) {
        this.libelle        = libelle;
        this.couleurBordure = couleurBordure;
        this.couleurFond    = couleurFond;
    }

    public String getLibelle()        { return libelle; }
    public String getCouleurBordure() { return couleurBordure; }
    public String getCouleurFond()    { return couleurFond; }
}
