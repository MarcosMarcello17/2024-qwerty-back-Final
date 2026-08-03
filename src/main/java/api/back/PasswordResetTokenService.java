package api.back;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Service
public class PasswordResetTokenService {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    public PasswordResetToken getToken(String token) {
        return passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token no encontrado")); // Maneja el caso en que el token no
                                                                                 // está presente
    }

    public PasswordResetToken createToken(User user) {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusHours(1)); // Token válido por 1 hora
        return passwordResetTokenRepository.save(token);
    }

    public List<PasswordResetToken> getTokensByUser(User user) {
        return passwordResetTokenRepository.findByUser(user);
    }

    public void deleteToken(Long id) {
        passwordResetTokenRepository.deleteById(id);
    }
}
