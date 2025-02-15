package api.back;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;


public interface TransaccionesRepository extends JpaRepository<Transacciones, Long> {
    List<Transacciones> findByUserId(Long userId); // Encuentra transacciones por el ID del usuario

    Optional<Transacciones> findByIdAndUserId(Long id, Long userId);

    List<Transacciones> findByUserIdOrderByFechaDesc(Long userId);

    Optional<Transacciones> findByIdAndUserEmail(Long id, String email);

    List<Transacciones> findByUserIdAndCategoriaOrderByFechaDesc(Long userId, String categoria);

    
    // Método para encontrar transacciones por ID de usuario y un rango de fechas (solo año)
    List<Transacciones> findByUserIdAndFechaBetween(Long userId, LocalDate startDate, LocalDate endDate); 

    // Método para encontrar transacciones por ID de usuario, categoría y rango de fechas (solo año)
    List<Transacciones> findByUserIdAndCategoriaAndFechaBetween(Long userId, String categoria, LocalDate startDate, LocalDate endDate); 


}
