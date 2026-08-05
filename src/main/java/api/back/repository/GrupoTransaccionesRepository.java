package api.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import api.back.model.GrupoTransacciones;

public interface GrupoTransaccionesRepository extends JpaRepository<GrupoTransacciones, Long> {
}
