package projet.M1.BDD;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import projet.M1.BDD.entity.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Script d'initialisation de la base de données.
 *
 * À lancer UNE SEULE FOIS :
 *   mvn exec:java -Dexec.mainClass="projet.M1.BDD.DataInitializer"
 *
 * Ce script crée dans l'ordre :
 *   1. Utilisateurs (étudiants, profs, gestionnaire)
 *   2. Salles
 *   3. Promotion M1 Informatique + 2 groupes (M1-A, M1-B)
 *   4. Rattachement étudiants → groupe + promotion
 *   5. Semaine complète de cours (lundi → vendredi, semaine en cours)
 *
 * Comptes créés :
 *   etudiant1    / etudiant123  → Étudiant, groupe M1-A
 *   etudiant2    / etudiant123  → Étudiant, groupe M1-B
 *   etudiant3    / etudiant123  → Étudiant, groupe M1-B
 *   profmartin   / prof123      → Professeur
 *   profdupont   / prof123      → Professeur
 *   gestionnaire / gest123      → Gestionnaire
 */
public class DataInitializer {

    public static void main(String[] args) {
        System.out.println("=== Initialisation de la base de données ===\n");

        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();

        try {
            // Vérifier si déjà initialisé
            Long count = em.createQuery("SELECT COUNT(u) FROM UserEntity u", Long.class)
                    .getSingleResult();
            if (count > 0) {
                System.out.println("Base déjà initialisée (" + count + " utilisateurs trouvés). Aucune action.");
                return;
            }

            // --- Étape 1 : Utilisateurs ---
            System.out.println("Création des utilisateurs...");
            UserEntity etudiant1    = persister(em, creerUser("Dubois",  "Alice",  22, "etudiant1",    "etudiant123", Role.ETUDIANT));
            UserEntity etudiant2    = persister(em, creerUser("Leroy",   "Thomas", 21, "etudiant2",    "etudiant123", Role.ETUDIANT));
            UserEntity etudiant3    = persister(em, creerUser("Bernard", "Emma",   23, "etudiant3",    "etudiant123", Role.ETUDIANT));
            UserEntity profMartin   = persister(em, creerUser("Martin",  "Jean",   45, "profmartin",   "prof123",     Role.PROFESSEUR));
            UserEntity profDupont   = persister(em, creerUser("Dupont",  "Sophie", 38, "profdupont",   "prof123",     Role.PROFESSEUR));
            persister(em, creerUser("Lambert", "Pierre", 50, "gestionnaire", "gest123", Role.GESTIONNAIRE_PLANNING));
            System.out.println("  6 utilisateurs créés.");

            // --- Étape 2 : Salles ---
            System.out.println("Création des salles...");
            SalleEntity salleA   = persister(em, creerSalle("Salle A",  50, List.of("Vidéoprojecteur", "Tableau blanc")));
            SalleEntity salleB   = persister(em, creerSalle("Salle B",  40, List.of("Vidéoprojecteur")));
            SalleEntity lab102   = persister(em, creerSalle("Lab 102",  25, List.of("Ordinateurs", "Vidéoprojecteur", "Tableau blanc")));
            System.out.println("  3 salles créées.");

            // --- Étape 3 : Promotion + Groupes ---
            System.out.println("Création de la promotion et des groupes...");
            PromotionEntity promo = persister(em, creerPromotion("M1 Informatique"));

            GroupeEtudiantEntity groupeA = persister(em, creerGroupe("M1-A"));
            GroupeEtudiantEntity groupeB = persister(em, creerGroupe("M1-B"));

            // --- Étape 4 : Rattachement étudiants → groupe + promotion ---
            System.out.println("Rattachement des étudiants aux groupes...");
            rattacherEtudiant(em, etudiant1, groupeA, promo);
            rattacherEtudiant(em, etudiant2, groupeB, promo);
            rattacherEtudiant(em, etudiant3, groupeB, promo);
            System.out.println("  Étudiants rattachés.");

            // --- Étape 5 : Cours de la semaine ---
            System.out.println("Création des cours de la semaine...");
            LocalDate lundi = LocalDate.now().with(DayOfWeek.MONDAY);
            creerSemaine(em, lundi, profMartin, profDupont, etudiant1, etudiant2, etudiant3, salleA, salleB, lab102);
            System.out.println("  Cours de la semaine créés.");

            System.out.println("\n=== Initialisation terminée avec succès ! ===\n");
            System.out.println("Comptes disponibles :");
            System.out.println("  etudiant1    / etudiant123  (Étudiant — groupe M1-A)");
            System.out.println("  etudiant2    / etudiant123  (Étudiant — groupe M1-B)");
            System.out.println("  etudiant3    / etudiant123  (Étudiant — groupe M1-B)");
            System.out.println("  profmartin   / prof123      (Professeur)");
            System.out.println("  profdupont   / prof123      (Professeur)");
            System.out.println("  gestionnaire / gest123      (Gestionnaire)");

        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
            JPAUtil.close();
        }
    }

    // -------------------------------------------------------------------------
    //  Semaine complète de cours
    // -------------------------------------------------------------------------

    /**
     * Crée une semaine réaliste de cours M1 Informatique.
     * Lundi → Vendredi avec des CM, TD et TP répartis entre les deux groupes.
     */
    private static void creerSemaine(EntityManager em, LocalDate lundi,
                                      UserEntity profMartin, UserEntity profDupont,
                                      UserEntity etudiant1, UserEntity etudiant2, UserEntity etudiant3,
                                      SalleEntity salleA, SalleEntity salleB, SalleEntity lab102) {

        List<UserEntity> tousLesEtudiants = List.of(etudiant1, etudiant2, etudiant3);
        List<UserEntity> groupeA          = List.of(etudiant1);
        List<UserEntity> groupeB          = List.of(etudiant2, etudiant3);

        // LUNDI
        creerCours(em, "Algorithmique",       "CM", lundi,               h(8,0),  h(10,0), salleA,  tousLesEtudiants, List.of(profMartin));
        creerCours(em, "Base de données",     "TD", lundi,               h(10,0), h(12,0), lab102,  groupeA,          List.of(profDupont));
        creerCours(em, "Réseaux",             "CM", lundi,               h(14,0), h(16,0), salleA,  tousLesEtudiants, List.of(profMartin));

        // MARDI
        creerCours(em, "Algorithmique",       "TD", lundi.plusDays(1),   h(8,0),  h(10,0), lab102,  groupeB,          List.of(profMartin));
        creerCours(em, "Programmation Web",   "CM", lundi.plusDays(1),   h(10,0), h(12,0), salleA,  tousLesEtudiants, List.of(profDupont));
        creerCours(em, "Base de données",     "TD", lundi.plusDays(1),   h(14,0), h(16,0), lab102,  groupeB,          List.of(profDupont));

        // MERCREDI
        creerCours(em, "Sécurité Informatique","CM", lundi.plusDays(2),  h(9,0),  h(11,0), salleA,  tousLesEtudiants, List.of(profMartin));
        creerCours(em, "Programmation Web",   "TP", lundi.plusDays(2),   h(14,0), h(16,0), lab102,  groupeA,          List.of(profDupont));

        // JEUDI
        creerCours(em, "Réseaux",             "TD", lundi.plusDays(3),   h(8,0),  h(10,0), salleB,  groupeA,          List.of(profMartin));
        creerCours(em, "Sécurité Informatique","TD", lundi.plusDays(3),  h(10,0), h(12,0), lab102,  groupeB,          List.of(profMartin));
        creerCours(em, "Programmation Web",   "TP", lundi.plusDays(3),   h(14,0), h(17,0), lab102,  groupeB,          List.of(profDupont));

        // VENDREDI
        creerCours(em, "Algorithmique",       "TP", lundi.plusDays(4),   h(9,0),  h(12,0), lab102,  groupeA,          List.of(profMartin));
        creerCours(em, "Base de données",     "CM", lundi.plusDays(4),   h(14,0), h(16,0), salleA,  tousLesEtudiants, List.of(profDupont));
    }

    // -------------------------------------------------------------------------
    //  Méthodes de création d'entités
    // -------------------------------------------------------------------------

    private static void creerCours(EntityManager em, String nom, String type, LocalDate jour,
                                    LocalTime debut, LocalTime fin, SalleEntity salle,
                                    List<UserEntity> etudiants, List<UserEntity> profs) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        HoraireEntity horaire = new HoraireEntity();
        horaire.setJour(jour);
        horaire.setHeureDebut(debut);
        horaire.setHeureFin(fin);
        em.persist(horaire);

        CoursEntity cours = new CoursEntity();
        cours.setNom(nom);
        cours.setTypeCours(type);
        cours.setHoraire(horaire);
        cours.setSalle(salle);
        cours.setList_etudiant(etudiants);
        cours.setList_professeur(profs);
        em.persist(cours);

        tx.commit();
    }

    private static void rattacherEtudiant(EntityManager em, UserEntity etudiant,
                                           GroupeEtudiantEntity groupe, PromotionEntity promo) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        UserEntity managed = em.merge(etudiant);
        managed.setGroupe(groupe);
        managed.setPromotion(promo);
        tx.commit();
    }

    private static UserEntity creerUser(String nom, String prenom, int age,
                                         String login, String motDePasse, Role role) {
        UserEntity u = new UserEntity();
        u.setNom(nom);
        u.setPrenom(prenom);
        u.setAge(age);
        u.setLogin(login);
        u.setMotDePasse(motDePasse);
        u.setRole(role);
        return u;
    }

    private static SalleEntity creerSalle(String nom, int places, List<String> materiel) {
        SalleEntity s = new SalleEntity();
        s.setNom(nom);
        s.setPlace(places);
        s.setListe_materiel(materiel);
        return s;
    }

    private static PromotionEntity creerPromotion(String nom) {
        PromotionEntity p = new PromotionEntity();
        p.setNom(nom);
        return p;
    }

    private static GroupeEtudiantEntity creerGroupe(String nom) {
        GroupeEtudiantEntity g = new GroupeEtudiantEntity();
        g.setNom(nom);
        return g;
    }

    /** Persiste une entité dans une transaction dédiée et la retourne managée. */
    private static <T> T persister(EntityManager em, T entity) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(entity);
        tx.commit();
        return entity;
    }

    /** Raccourci pour créer un LocalTime proprement. */
    private static LocalTime h(int heure, int minutes) {
        return LocalTime.of(heure, minutes);
    }
}
