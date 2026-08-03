package api.back;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PersonalTipoGastoRepository extends JpaRepository<PersonalTipoGasto, Long> {
    List<PersonalTipoGasto> findByUser(User user);
    Optional<PersonalTipoGasto> findByUserAndNombre(User user, String nombre);
}