package api.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import api.back.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);  // Método para buscar usuarios por email
}
