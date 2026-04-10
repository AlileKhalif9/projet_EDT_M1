package projet.M1.BDD.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import projet.M1.BDD.JPAUtil;
import projet.M1.BDD.entity.CoursEntity;
import projet.M1.BDD.entity.GroupeEtudiantEntity;
import projet.M1.BDD.entity.HoraireEntity;
import projet.M1.BDD.entity.SalleEntity;
import projet.M1.BDD.entity.UserEntity;
import projet.M1.BDD.entity.ModuleEntity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * DAO pour les cours.
 */
public class CoursDAO {

    public List<CoursEntity> findByEtudiantAndSemaine(UserEntity etudiant, LocalDate semaine) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            LocalDate lundi    = semaine.with(DayOfWeek.MONDAY);
            LocalDate vendredi = semaine.with(DayOfWeek.FRIDAY);
            return em.createQuery(
                            "SELECT c FROM CoursEntity c JOIN c.list_etudiant e " +
                                    "WHERE e = :etudiant AND c.horaire.jour BETWEEN :lundi AND :vendredi " +
                                    "ORDER BY c.horaire.jour, c.horaire.heureDebut",
                            CoursEntity.class)
                    .setParameter("etudiant", em.merge(etudiant))
                    .setParameter("lundi",    lundi)
                    .setParameter("vendredi", vendredi)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<CoursEntity> findByProfesseurAndSemaine(UserEntity professeur, LocalDate semaine) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            LocalDate lundi    = semaine.with(DayOfWeek.MONDAY);
            LocalDate vendredi = semaine.with(DayOfWeek.FRIDAY);
            return em.createQuery(
                            "SELECT c FROM CoursEntity c JOIN c.list_professeur p " +
                                    "WHERE p = :professeur AND c.horaire.jour BETWEEN :lundi AND :vendredi " +
                                    "ORDER BY c.horaire.jour, c.horaire.heureDebut",
                            CoursEntity.class)
                    .setParameter("professeur", em.merge(professeur))
                    .setParameter("lundi",      lundi)
                    .setParameter("vendredi",   vendredi)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<CoursEntity> findByGroupeAndSemaine(String nomGroupe, LocalDate semaine) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            LocalDate lundi    = semaine.with(DayOfWeek.MONDAY);
            LocalDate vendredi = semaine.with(DayOfWeek.FRIDAY);
            return em.createQuery(
                            "SELECT c FROM CoursEntity c JOIN c.list_etudiant e " +
                                    "WHERE e.groupe.nom = :groupe AND c.horaire.jour BETWEEN :lundi AND :vendredi " +
                                    "ORDER BY c.horaire.jour, c.horaire.heureDebut",
                            CoursEntity.class)
                    .setParameter("groupe",   nomGroupe)
                    .setParameter("lundi",    lundi)
                    .setParameter("vendredi", vendredi)
                    .getResultList().stream().distinct().toList();
        } finally {
            em.close();
        }
    }

    public List<CoursEntity> findBySalleAndSemaine(SalleEntity salle, LocalDate semaine) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            LocalDate lundi    = semaine.with(DayOfWeek.MONDAY);
            LocalDate vendredi = semaine.with(DayOfWeek.FRIDAY);
            return em.createQuery(
                            "SELECT c FROM CoursEntity c " +
                                    "WHERE c.salle.id = :salleId AND c.horaire.jour BETWEEN :lundi AND :vendredi " +
                                    "ORDER BY c.horaire.jour, c.horaire.heureDebut",
                            CoursEntity.class)
                    .setParameter("salleId",  salle.getId())
                    .setParameter("lundi",    lundi)
                    .setParameter("vendredi", vendredi)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tous les cours d'une semaine, tous groupes et profs confondus.
     * Utilisé par le gestionnaire dans l'onglet "Mon EDT" pour voir l'intégralité
     * des cours de la semaine, y compris ceux qu'il vient d'ajouter.
     */
    public List<CoursEntity> findAllBySemaine(LocalDate semaine) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            LocalDate lundi    = semaine.with(DayOfWeek.MONDAY);
            LocalDate vendredi = semaine.with(DayOfWeek.FRIDAY);
            return em.createQuery(
                            "SELECT c FROM CoursEntity c " +
                                    "WHERE c.horaire.jour BETWEEN :lundi AND :vendredi " +
                                    "ORDER BY c.horaire.jour, c.horaire.heureDebut",
                            CoursEntity.class)
                    .setParameter("lundi",    lundi)
                    .setParameter("vendredi", vendredi)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void applyModification(Long coursId, HoraireEntity newHoraire, SalleEntity newSalle) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CoursEntity cours = em.find(CoursEntity.class, coursId);
            if (cours != null) {
                if (newHoraire != null)
                    cours.setHoraire(em.find(HoraireEntity.class, newHoraire.getId()));
                if (newSalle != null)
                    cours.setSalle(em.find(SalleEntity.class, newSalle.getId()));
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * US13 — Annule un cours : passe son typeCours à "ANNULE" en BDD.
     * Retourne le typeCours précédent pour permettre une réactivation ultérieure.
     */
    public String annulerCours(Long coursId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CoursEntity cours = em.find(CoursEntity.class, coursId);
            String ancienType = (cours != null && cours.getTypeCours() != null)
                    ? cours.getTypeCours() : "CM";
            if (cours != null) cours.setTypeCours("ANNULE");
            tx.commit();
            return ancienType;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Réactive un cours annulé : restaure son typeCours d'origine en BDD.
     */
    public void reactiverCours(Long coursId, String typeCours) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CoursEntity cours = em.find(CoursEntity.class, coursId);
            if (cours != null) cours.setTypeCours(typeCours);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * US14 — Crée un nouveau cours en BDD.
     * Le profId est obligatoire — le prof est rattaché à list_professeur du cours.
     */
    public CoursEntity ajouterCours(String nom, String typeCours,
                                    java.time.LocalDate jour,
                                    java.time.LocalTime heureDebut,
                                    java.time.LocalTime heureFin,
                                    String nomSalle,
                                    String nomGroupe,
                                    Long profId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            HoraireEntity horaire = new HoraireEntity();
            horaire.setJour(jour);
            horaire.setHeureDebut(heureDebut);
            horaire.setHeureFin(heureFin);
            em.persist(horaire);

            SalleEntity salle = null;
            if (nomSalle != null && !nomSalle.isBlank()) {
                List<SalleEntity> salles = em.createQuery(
                                "SELECT s FROM SalleEntity s WHERE s.nom = :nom", SalleEntity.class)
                        .setParameter("nom", nomSalle).getResultList();
                if (!salles.isEmpty()) salle = salles.get(0);
            }

            List<UserEntity> etudiants = new java.util.ArrayList<>();
            if (nomGroupe != null && !nomGroupe.isBlank()) {
                List<GroupeEtudiantEntity> groupes = em.createQuery(
                                "SELECT g FROM GroupeEtudiantEntity g WHERE g.nom = :nom",
                                GroupeEtudiantEntity.class)
                        .setParameter("nom", nomGroupe).getResultList();
                if (!groupes.isEmpty() && groupes.get(0).getList_etudiant() != null)
                    etudiants.addAll(groupes.get(0).getList_etudiant());
            }

            // Rattacher le professeur au cours
            UserEntity profEntity = null;
            List<UserEntity> profs = List.of();
            if (profId != null) {
                profEntity = em.find(UserEntity.class, profId);
                if (profEntity != null) profs = List.of(profEntity);
            }

            CoursEntity cours = new CoursEntity();
            cours.setNom(nom);
            cours.setTypeCours(typeCours);
            cours.setHoraire(horaire);
            cours.setSalle(salle);
            cours.setList_etudiant(etudiants);
            cours.setList_professeur(profs);
            em.persist(cours);

            // Rattacher aussi le professeur au module correspondant (professeur_module)
            // pour que les notes du prof soient accessibles dans NotesController.
            if (profEntity != null) {
                List<ModuleEntity> modules = em.createQuery(
                                "SELECT m FROM ModuleEntity m WHERE m.nom = :nom",
                                ModuleEntity.class)
                        .setParameter("nom", nom).getResultList();
                for (ModuleEntity module : modules) {
                    List<UserEntity> profsModule = module.getList_professeur();
                    if (profsModule == null) profsModule = new java.util.ArrayList<>();
                    final UserEntity pFinal = profEntity;
                    boolean dejaPresent = profsModule.stream()
                            .anyMatch(p -> p.getId().equals(pFinal.getId()));
                    if (!dejaPresent) {
                        profsModule = new java.util.ArrayList<>(profsModule);
                        profsModule.add(profEntity);
                        module.setList_professeur(profsModule);
                    }
                }
            }

            tx.commit();
            return cours;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * US15 — Modifie un cours existant en BDD.
     */
    public CoursEntity modifierCours(Long coursId, String nom, String typeCours,
                                     java.time.LocalDate jour,
                                     java.time.LocalTime heureDebut,
                                     java.time.LocalTime heureFin,
                                     String nomSalle,
                                     String nomGroupe) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            CoursEntity cours = em.find(CoursEntity.class, coursId);
            if (cours == null) throw new IllegalArgumentException("Cours introuvable : " + coursId);

            cours.setNom(nom);
            cours.setTypeCours(typeCours);

            HoraireEntity horaire = cours.getHoraire();
            if (horaire == null) { horaire = new HoraireEntity(); em.persist(horaire); }
            horaire.setJour(jour);
            horaire.setHeureDebut(heureDebut);
            horaire.setHeureFin(heureFin);
            cours.setHoraire(horaire);

            if (nomSalle != null && !nomSalle.isBlank()) {
                List<SalleEntity> salles = em.createQuery(
                                "SELECT s FROM SalleEntity s WHERE s.nom = :nom", SalleEntity.class)
                        .setParameter("nom", nomSalle).getResultList();
                cours.setSalle(salles.isEmpty() ? null : salles.get(0));
            } else {
                cours.setSalle(null);
            }

            if (nomGroupe != null && !nomGroupe.isBlank()) {
                List<GroupeEtudiantEntity> groupes = em.createQuery(
                                "SELECT g FROM GroupeEtudiantEntity g WHERE g.nom = :nom",
                                GroupeEtudiantEntity.class)
                        .setParameter("nom", nomGroupe).getResultList();
                if (!groupes.isEmpty() && groupes.get(0).getList_etudiant() != null)
                    cours.setList_etudiant(groupes.get(0).getList_etudiant());
            }

            tx.commit();
            return cours;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}