package projet.M1.service;

import projet.M1.model.academique.Groupe_Etudiant;
import projet.M1.model.planning.Salle;
import projet.M1.model.planning.TypeCours;
import projet.M1.ui.CoursDisplay;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TEMPORAIRE — fournit des données fictives en attendant la vraie BDD.
 *
 * Ce service est appelé par les controllers front (TimetableController,
 * DashboardController, RoomSelectionController).
 *
 * Il contient :
 *   - 9 cours fictifs répartis sur la semaine (lundi → vendredi)
 *   - 6 salles fictives
 *   - 3 groupes fictifs
 *
 * Quand la BDD sera prête : remplacez le contenu de chaque méthode
 * par vos vrais appels DAO. Les controllers front n'ont pas besoin de changer.
 *
 * Note : je retourne des CoursDisplay (objet front) et non des Cours
 * pour ne pas avoir à toucher votre classe Cours.java.
 */
public class MockDataService {

    private static MockDataService instance;

    private MockDataService() {}

    public static MockDataService getInstance() {
        if (instance == null) instance = new MockDataService();
        return instance;
    }

    // Liste des salles — à remplacer par un appel DAO
    public List<Salle> getAllSalles() {
        return List.of(
                new Salle("Salle 101",  30, List.of("Projecteur", "Tableau blanc", "WiFi")),
                new Salle("Salle 102",  25, List.of("Projecteur", "WiFi")),
                new Salle("Amphi A",   200, List.of("Projecteur", "Micro", "Climatisation", "WiFi")),
                new Salle("Salle TP1",  20, List.of("Ordinateurs", "Projecteur", "WiFi")),
                new Salle("Salle TP2",  20, List.of("Ordinateurs", "WiFi")),
                new Salle("Salle 305",  40, List.of("Projecteur", "Tableau blanc", "WiFi", "Climatisation"))
        );
    }

    // Liste des groupes — à remplacer par un appel DAO
    public List<Groupe_Etudiant> getAllGroupes() {
        return List.of(
                new Groupe_Etudiant("INFO-2024-A", new ArrayList<>()),
                new Groupe_Etudiant("INFO-2024-B", new ArrayList<>()),
                new Groupe_Etudiant("MATH-2024-A", new ArrayList<>())
        );
    }

    // EDT de l'utilisateur connecté (US2) — à remplacer par un appel DAO
    public List<CoursDisplay> getCoursEtudiant(LocalDate semaine) {
        return buildSample(semaine, "INFO-2024-A", "M. Martin");
    }

    // EDT d'un professeur donné (US3) — à remplacer par un appel DAO
    public List<CoursDisplay> getCoursProfesseur(String nomProf, LocalDate semaine) {
        return buildSample(semaine, "INFO-2024-A", nomProf);
    }

    // EDT d'un groupe (US5 — prof qui consulte sa classe) — à remplacer par un appel DAO
    public List<CoursDisplay> getCoursGroupe(String nomGroupe, LocalDate semaine) {
        return buildSample(semaine, nomGroupe, "M. Martin");
    }

    // EDT d'une salle (US4) — à remplacer par un appel DAO
    public List<CoursDisplay> getCoursSalle(String nomSalle, LocalDate semaine) {
        return buildSample(semaine, "INFO-2024-A", "M. Martin").stream()
                .filter(c -> c.nomSalle().equals(nomSalle))
                .toList();
    }

    // Génère les cours fictifs pour une semaine donnée
    private List<CoursDisplay> buildSample(LocalDate lundi, String groupe, String prof) {
        List<CoursDisplay> list = new ArrayList<>();

        list.add(c("Algorithmique",      TypeCours.CM, groupe, prof, lundi,             8,  0, 10, 0, "Amphi A"));
        list.add(c("Base de données",    TypeCours.TD, groupe, prof, lundi,            10,  0, 12, 0, "Salle 101"));
        list.add(c("Programmation Java", TypeCours.TP, groupe, prof, lundi.plusDays(1), 9,  0, 11, 0, "Salle TP1"));
        list.add(c("Réseaux",            TypeCours.CM, groupe, prof, lundi.plusDays(1), 14, 0, 16, 0, "Amphi A"));
        list.add(c("Mathématiques",      TypeCours.TD, groupe, prof, lundi.plusDays(2), 8,  0, 10, 0, "Salle 101"));
        list.add(c("Algorithmique",      TypeCours.TP, groupe, prof, lundi.plusDays(2), 13, 0, 15, 0, "Salle TP1"));
        list.add(c("Base de données",    TypeCours.CM, groupe, prof, lundi.plusDays(3), 9,  0, 11, 0, "Amphi A"));
        list.add(c("Génie logiciel",     TypeCours.TD, groupe, prof, lundi.plusDays(3), 14, 0, 16, 0, "Salle 305"));
        list.add(c("Programmation Java", TypeCours.CM, groupe, prof, lundi.plusDays(4), 10, 0, 12, 0, "Amphi A"));

        return list;
    }

    private CoursDisplay c(String nom, TypeCours type, String groupe, String prof,
                            LocalDate jour, int hd, int md, int hf, int mf, String salle) {
        return new CoursDisplay(nom, type, groupe, prof, salle,
                jour, LocalTime.of(hd, md), LocalTime.of(hf, mf));
    }
}
