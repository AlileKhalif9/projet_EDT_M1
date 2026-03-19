package projet.M1.ui;

import projet.M1.model.planning.TypeCours;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Objet léger utilisé uniquement pour l'affichage dans la grille EDT.
 *
 * Pourquoi j'ai créé ça plutôt qu'utiliser Cours directement ?
 * → Pour ne pas toucher à votre classe Cours.java.
 *   La classe Cours contient des listes d'étudiants, etc. dont le front n'a pas besoin.
 *   Ici on a juste les infos nécessaires pour afficher un bloc dans la grille.
 *
 * C'est créé par MockDataService (données fictives) et sera remplacé
 * par un vrai service quand la BDD sera branchée.
 */
public record CoursDisplay(
        String    nom,        // ex: "Algorithmique"
        TypeCours typeCours,  // CM / TD / TP / EXAMEN / ANNULE
        String    nomGroupe,  // ex: "INFO-2024-A"
        String    nomProf,    // ex: "M. Martin"
        String    nomSalle,   // ex: "Salle 101"
        LocalDate jour,       // date du cours
        LocalTime heureDebut,
        LocalTime heureFin
) {}
