package api.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import api.back.model.TransaccionesPendientes;

import java.util.List;
import java.util.Optional;

public interface TransaccionesPendientesRepository extends JpaRepository<TransaccionesPendientes, Long> {

    // Método para obtener todas las transacciones pendientes por ID de usuario
    List<TransaccionesPendientes> findByUserId(Long userId);

    // Método para encontrar una transacción pendiente por ID y usuario
    Optional<TransaccionesPendientes> findByIdAndUserId(Long id, Long userId);

    List<TransaccionesPendientes> findByGrupoId(Long grupoId);
}
