package api.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import api.back.model.PersonalCategoria;
import api.back.model.User;

import java.util.List;

@Repository
public interface PersonalCategoriaRepository extends JpaRepository<PersonalCategoria, Long> {
    List<PersonalCategoria> findByUser(User user);

    List<PersonalCategoria> findByUserOrUserIsNull(User user);
}
