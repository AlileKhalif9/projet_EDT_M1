package projet.M1.BDD.repository;

import projet.M1.BDD.entity.ModuleEntity;
import projet.M1.BDD.entity.NoteEntity;
import projet.M1.BDD.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<NoteEntity, Long> {

    // Notes d'un étudiant
    List<NoteEntity> findByEtudiant(UserEntity etudiant);

    // Notes d'un module
    List<NoteEntity> findByModule(ModuleEntity module);

    // Notes d'un étudiant pour un module précis
    List<NoteEntity> findByEtudiantAndModule(UserEntity etudiant, ModuleEntity module);
}