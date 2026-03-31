package projet.M1.BDD;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Singleton qui gère l'EntityManagerFactory pour toute l'application.
 *
 * Utilise le persistence unit "timetablePU" défini dans META-INF/persistence.xml.
 * Tous les DAOs passent par getEntityManagerFactory() pour créer leurs EntityManager.
 *
 * À fermer proprement à la fin de l'application via JPAUtil.close().
 */
public class JPAUtil {

    private static final String PERSISTENCE_UNIT = "timetablePU";
    private static EntityManagerFactory emf;

    private JPAUtil() {}

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null || !emf.isOpen()) {
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
        }
        return emf;
    }

    /** À appeler quand l'application se ferme (ex: MainApp.stop()). */
    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
