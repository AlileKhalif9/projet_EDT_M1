package projet.M1.BDD;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import projet.M1.BDD.entity.Role;
import projet.M1.BDD.entity.UserEntity;

/**
 * Script d'initialisation de la base de données.
 *
 * À lancer UNE SEULE FOIS pour créer les comptes de test :
 *   mvn exec:java -Dexec.mainClass="projet.M1.BDD.DataInitializer"
 *
 * Hibernate crée automatiquement les tables au premier lancement (hbm2ddl.auto=update).
 * Ce script insère ensuite les utilisateurs de test.
 *
 * Comptes créés :
 *   etudiant1   / etudiant123   → ETUDIANT
 *   etudiant2   / etudiant123   → ETUDIANT
 *   profmartin  / prof123       → PROFESSEUR
 *   profdupont  / prof123       → PROFESSEUR
 *   gestionnaire/ gest123       → GESTIONNAIRE_PLANNING
 */
public class DataInitializer {

    public static void main(String[] args) {
        System.out.println("=== Initialisation de la base de données ===");

        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // Vérifier si des utilisateurs existent déjà
            Long count = em.createQuery("SELECT COUNT(u) FROM UserEntity u", Long.class)
                    .getSingleResult();

            if (count > 0) {
                System.out.println("Des utilisateurs existent déjà (" + count + " trouvés). Aucune insertion.");
                tx.rollback();
                return;
            }

            // --- Étudiants ---
            em.persist(creerUser("Dubois",    "Alice",   22, "etudiant1",    "etudiant123", Role.ETUDIANT));
            em.persist(creerUser("Leroy",     "Thomas",  21, "etudiant2",    "etudiant123", Role.ETUDIANT));
            em.persist(creerUser("Bernard",   "Emma",    23, "etudiant3",    "etudiant123", Role.ETUDIANT));

            // --- Professeurs ---
            em.persist(creerUser("Martin",    "Jean",    45, "profmartin",   "prof123",     Role.PROFESSEUR));
            em.persist(creerUser("Dupont",    "Sophie",  38, "profdupont",   "prof123",     Role.PROFESSEUR));

            // --- Gestionnaire (un seul) ---
            em.persist(creerUser("Lambert",   "Pierre",  50, "gestionnaire", "gest123",     Role.GESTIONNAIRE_PLANNING));

            tx.commit();
            System.out.println("Utilisateurs créés avec succès !");
            System.out.println();
            System.out.println("Comptes disponibles :");
            System.out.println("  etudiant1    / etudiant123  (Étudiant)");
            System.out.println("  etudiant2    / etudiant123  (Étudiant)");
            System.out.println("  etudiant3    / etudiant123  (Étudiant)");
            System.out.println("  profmartin   / prof123      (Professeur)");
            System.out.println("  profdupont   / prof123      (Professeur)");
            System.out.println("  gestionnaire / gest123      (Gestionnaire)");

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.err.println("Erreur lors de l'initialisation : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
            JPAUtil.close();
        }
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
}
