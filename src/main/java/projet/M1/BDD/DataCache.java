package projet.M1.BDD;

import projet.M1.BDD.dao.GroupeDAO;
import projet.M1.BDD.dao.SalleDAO;
import projet.M1.BDD.entity.GroupeEtudiantEntity;
import projet.M1.BDD.entity.SalleEntity;

import java.util.List;

/**
 * Cache en mémoire pour les données statiques (salles, groupes).
 * Chargé une seule fois au démarrage — pas de requête réseau à chaque navigation.
 */
public class DataCache {

    private static DataCache instance;

    private List<SalleEntity>          salles  = List.of();
    private List<GroupeEtudiantEntity> groupes = List.of();

    private DataCache() {}

    public static DataCache getInstance() {
        if (instance == null) instance = new DataCache();
        return instance;
    }

    /** À appeler une fois au démarrage dans MainApp. */
    public void preload() {
        try { salles  = new SalleDAO().findAll();  } catch (Exception ignored) {}
        try { groupes = new GroupeDAO().findAll(); } catch (Exception ignored) {}
    }

    public List<SalleEntity>          getSalles()  { return salles;  }
    public List<GroupeEtudiantEntity> getGroupes() { return groupes; }
}
