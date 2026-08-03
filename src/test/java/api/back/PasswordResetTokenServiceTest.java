package api.back;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@SpringBootTest
@ActiveProfiles("test")
public class PasswordResetTokenServiceTest {

    // Retrieve a token successfully when it exists in the repository
    @Test
    public void test_retrieve_token_success() {
        // Arrange
        PasswordResetTokenRepository mockRepository = Mockito.mock(PasswordResetTokenRepository.class);
        PasswordResetTokenService service = new PasswordResetTokenService();
        ReflectionTestUtils.setField(service, "passwordResetTokenRepository", mockRepository);

        String tokenString = "valid-token";
        PasswordResetToken expectedToken = new PasswordResetToken();
        expectedToken.setToken(tokenString);

        Mockito.when(mockRepository.findByToken(tokenString)).thenReturn(Optional.of(expectedToken));

        // Act
        PasswordResetToken actualToken = service.getToken(tokenString);

        // Assert
        Assertions.assertEquals(expectedToken, actualToken);
    }

    // Handle the case where the token does not exist in the repository
    @Test
    public void test_handle_token_not_found() {
        // Arrange
        PasswordResetTokenRepository mockRepository = Mockito.mock(PasswordResetTokenRepository.class);
        PasswordResetTokenService service = new PasswordResetTokenService();
        ReflectionTestUtils.setField(service, "passwordResetTokenRepository", mockRepository);

        String tokenString = "invalid-token";

        Mockito.when(mockRepository.findByToken(tokenString)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            service.getToken(tokenString);
        });

        Assertions.assertEquals("Token no encontrado", exception.getMessage());
    }

    // Token is created successfully for a valid user
    @Test
    public void test_create_token_success() {
        // Arrange
        User user = new User();
        PasswordResetTokenService service = new PasswordResetTokenService();
        PasswordResetTokenRepository mockRepository = Mockito.mock(PasswordResetTokenRepository.class);
        ReflectionTestUtils.setField(service, "passwordResetTokenRepository", mockRepository);

        PasswordResetToken expectedToken = new PasswordResetToken();
        expectedToken.setToken(UUID.randomUUID().toString());
        expectedToken.setUser(user);
        expectedToken.setExpiryDate(LocalDateTime.now().plusHours(1));

        Mockito.when(mockRepository.save(Mockito.any(PasswordResetToken.class))).thenReturn(expectedToken);

        // Act
        PasswordResetToken actualToken = service.createToken(user);

        // Assert
        assertNotNull(actualToken);
        assertEquals(expectedToken.getUser(), actualToken.getUser());
        assertEquals(expectedToken.getExpiryDate(), actualToken.getExpiryDate());
    }
}