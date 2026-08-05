package api.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import api.back.model.PasswordResetToken;
import api.back.model.User;

import java.util.Optional;
import java.util.List;


public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByToken(String token);
    List<PasswordResetToken> findByUser(User user);  
}