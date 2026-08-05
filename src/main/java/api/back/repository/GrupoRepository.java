package api.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import api.back.model.Grupo;

import java.util.List;

public interface GrupoRepository extends JpaRepository<Grupo, Long> {
    List<Grupo> findByUsuariosEmail(String email); 
}
