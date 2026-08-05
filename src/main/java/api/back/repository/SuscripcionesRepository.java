package api.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import api.back.model.Suscripciones;

public interface SuscripcionesRepository extends JpaRepository<Suscripciones, Long> {
    
}
