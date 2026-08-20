package api.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import api.back.model.PersonalTipoGasto;
import api.back.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonalTipoGastoRepository extends JpaRepository<PersonalTipoGasto, Long> {
    List<PersonalTipoGasto> findByUser(User user);

    Optional<PersonalTipoGasto> findByUserAndNombre(User user, String nombre);

    List<PersonalTipoGasto> findByUserOrUserIsNull(User user);
}