package api.back;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.Principal;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class ChangePasswordTests {
    @Test
    public void test_password_change_success() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest("password", "newPassword");
        Principal principal = Mockito.mock(Principal.class);
        UserService userService = Mockito.mock(UserService.class);
        UserController userController = new UserController(userService);

        Mockito.when(principal.getName()).thenReturn("user@example.com");
        Mockito.when(userService.changePassword("user@example.com", "password", "newPassword"))
                .thenReturn(true);

        // Act
        ResponseEntity<String> response = userController.changePassword(request, principal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password changed successfully.", response.getBody());
    }

    // Current password is incorrect, resulting in a 400 Bad Request response
    @Test
    public void test_password_change_failure() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest("password", "newPassword");
        Principal principal = Mockito.mock(Principal.class);
        UserService userService = Mockito.mock(UserService.class);
        UserController userController = new UserController(userService);

        Mockito.when(principal.getName()).thenReturn("user@example.com");
        Mockito.when(userService.changePassword("user@example.com", "wrongCurrentPassword", "newPassword"))
                .thenReturn(false);

        // Act
        ResponseEntity<String> response = userController.changePassword(request, principal);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Current password is incorrect.", response.getBody());
    }
}