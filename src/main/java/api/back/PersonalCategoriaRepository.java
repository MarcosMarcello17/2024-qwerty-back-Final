package api.back;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PersonalCategoriaRepository extends JpaRepository<PersonalCategoria, Long> {
    List<PersonalCategoria> findByUser(User user);
}
