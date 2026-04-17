package projet.M1.ui;

import projet.M1.BDD.entity.CoursEntity;
import projet.M1.BDD.entity.UserEntity;
import projet.M1.model.planning.TypeCours;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Objet léger utilisé uniquement par la couche UI pour afficher un cours.
 *
 * On le construit à partir d'un CoursEntity via fromEntity().
 * Cela évite de passer des entités JPA directement aux controllers JavaFX.
 */
public record CoursDisplay(
        Long id,
        String nom,
        TypeCours typeCours,
        String nomGroupe,
        String nomProf,
        String nomSalle,
        LocalDate jour,
        LocalTime heureDebut,
        LocalTime heureFin
) {

    /**
     * Convertit un CoursEntity (BDD) en CoursDisplay (UI).
     */
    public static CoursDisplay fromEntity(CoursEntity c) {
        if (c == null || c.getHoraire() == null) return null;

        // Nom du groupe : on prend le groupe du premier étudiant inscrit
        String groupe = null;
        if (c.getList_etudiant() != null && !c.getList_etudiant().isEmpty()) {
            UserEntity e = c.getList_etudiant().get(0);
            if (e.getGroupe() != null) groupe = e.getGroupe().getNom();
        }

        // Nom du professeur : premier de la liste
        String prof = null;
        if (c.getList_professeur() != null && !c.getList_professeur().isEmpty()) {
            UserEntity p = c.getList_professeur().get(0);
            prof = p.getPrenom() + " " + p.getNom();
        }

        return new CoursDisplay(
                c.getId(),
                c.getNom(),
                TypeCours.fromString(c.getTypeCours()),
                groupe,
                prof,
                c.getSalle() != null ? c.getSalle().getNom() : null,
                c.getHoraire().getJour(),
                c.getHoraire().getHeureDebut(),
                c.getHoraire().getHeureFin()
        );
    }
}
