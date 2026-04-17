package projet.M1.controller;

import projet.M1.BDD.dao.ModuleDAO;
import projet.M1.BDD.dao.NoteDAO;
import projet.M1.BDD.entity.ModuleEntity;
import projet.M1.BDD.entity.NoteEntity;
import projet.M1.BDD.entity.UserEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Back-end : gestion des notes.
 */
public class NoteController {

    private final NoteDAO noteDAO;
    private final ModuleDAO moduleDAO;

    public NoteController(NoteDAO noteDAO, ModuleDAO moduleDAO) {
        this.noteDAO = noteDAO;
        this.moduleDAO = moduleDAO;
    }

    /** Promotions auxquelles le professeur est rattaché. */
    public List<projet.M1.BDD.entity.PromotionEntity> getPromotionsProfesseur(Long profId) {
        return moduleDAO.findPromotionsByProfesseur(profId);
    }

    /** Modules enseignés par le prof pour une promotion donnée. */
    public List<ModuleEntity> getModulesProfesseurEtPromotion(Long profId, Long promotionId) {
        return moduleDAO.findByProfesseurAndPromotion(profId, promotionId);
    }

    /** Étudiants d'une promotion inscrits à un module donné. */
    public List<UserEntity> getEtudiantsDuModuleEtPromotion(Long moduleId, Long promotionId) {
        return moduleDAO.findEtudiantsByModuleAndPromotion(moduleId, promotionId);
    }

    /** Modules enseignés par le professeur connecté. */
    public List<ModuleEntity> getModulesProfesseur(Long profId) {
        return moduleDAO.findByProfesseur(profId);
    }

    /**
     * Toutes les notes d'un module, groupées par étudiant puis par intitulé.
     * Structure : Map<etudiantId, Map<intitule, NoteEntity>>
     */
    public Map<Long, Map<String, NoteEntity>> getNotesParEtudiantEtControle(Long moduleId) {
        List<NoteEntity> notes = noteDAO.findByModule(moduleId);
        return notes.stream().collect(
                Collectors.groupingBy(
                        n -> n.getEtudiant().getId(),
                        Collectors.toMap(NoteEntity::getIntitule, n -> n, (a, b) -> a)
                )
        );
    }

    /** Liste des étudiants inscrits à un module (pour les lignes du tableau). */
    public List<UserEntity> getEtudiantsDuModule(Long moduleId) {
        return moduleDAO.findEtudiantsByModule(moduleId);
    }

    /**
     * Liste distincte des intitulés de contrôles pour un module (pour les colonnes du tableau).
     */
    public List<String> getIntitulesControles(Long moduleId) {
        return noteDAO.findByModule(moduleId).stream()
                .map(NoteEntity::getIntitule)
                .distinct()
                .sorted()
                .toList();
    }


    /** Modules suivis par l'étudiant connecté. */
    public List<ModuleEntity> getModulesEtudiant(Long etudiantId) {
        return moduleDAO.findByEtudiant(etudiantId);
    }

    /** Notes d'un étudiant pour un module donné. */
    public List<NoteEntity> getNotesEtudiantModule(Long etudiantId, Long moduleId) {
        return noteDAO.findByEtudiantAndModule(etudiantId, moduleId);
    }

    /** Toutes les notes d'un module (tous étudiants) : pour calculer la moyenne de classe. */
    public List<NoteEntity> getNotesModule(Long moduleId) {
        return noteDAO.findByModule(moduleId);
    }

    /** Toutes les notes d'un étudiant, tous modules confondus. */
    public List<NoteEntity> getToutesNotesEtudiant(Long etudiantId) {
        return noteDAO.findByEtudiant(etudiantId);
    }

    /**
     * Crée ou met à jour une note pour un étudiant, un module et un intitulé donnés.
     * Si la note existe déjà, elle est mise à jour.
     */
    public NoteEntity sauvegarderNote(Long etudiantId, Long moduleId,
                                      String intitule, float valeur, float coefficient) {
        if (intitule == null || intitule.isBlank())
            throw new IllegalArgumentException("L'intitulé du contrôle ne peut pas être vide.");
        if (valeur < 0 || valeur > 20)
            throw new IllegalArgumentException("La note doit être comprise entre 0 et 20.");
        if (coefficient <= 0)
            throw new IllegalArgumentException("Le coefficient doit être strictement positif.");
        return noteDAO.sauvegarderNote(etudiantId, moduleId, intitule, valeur, coefficient);
    }

    /**
     * Supprime tous les notes d'un contrôle pour un module.
     */
    public void supprimerControle(Long moduleId, String intitule) {
        noteDAO.supprimerControle(moduleId, intitule);
    }

    /**
     * Moyenne pondérée d'un étudiant pour une liste de notes.
     * Retourne -1 si la liste est vide.
     */
    public float calculerMoyenne(List<NoteEntity> notes) {
        if (notes.isEmpty()) return -1f;
        float somme = 0f;
        float totalCoeff = 0f;
        for (NoteEntity n : notes) {
            somme += n.getValeur() * n.getCoefficient();
            totalCoeff += n.getCoefficient();
        }
        return totalCoeff == 0 ? -1f : somme / totalCoeff;
    }

    /**
     * Moyenne de la classe pour un contrôle donné (même intitulé).
     * Retourne -1 si aucune note pour cet intitulé.
     */
    public float calculerMoyenneControle(List<NoteEntity> toutesLesNotes, String intitule) {
        List<NoteEntity> notesControle = toutesLesNotes.stream()
                .filter(n -> intitule.equals(n.getIntitule()))
                .toList();
        if (notesControle.isEmpty()) return -1f;
        float somme = notesControle.stream().map(NoteEntity::getValeur)
                .reduce(0f, Float::sum);
        return somme / notesControle.size();
    }
}