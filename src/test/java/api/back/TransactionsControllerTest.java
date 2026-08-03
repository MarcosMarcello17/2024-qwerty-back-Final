package api.back;
/*
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class TransactionsControllerTest {
    @Test
    public void testObtenerTransaccionesDeUsuario() {
        // Arrange
        Authentication authentication = Mockito.mock(Authentication.class);
        UserService userService = Mockito.mock(UserService.class);
        TransaccionesService transaccionesService = Mockito.mock(TransaccionesService.class);
        TransaccionesController controller = new TransaccionesController(transaccionesService, userService);

        String email = "user@example.com";
        User user = new User();
        user.setId(1L);
        List<Transacciones> expectedTransactions = Arrays.asList(new Transacciones(), new Transacciones());

        Mockito.when(authentication.getName()).thenReturn(email);
        Mockito.when(userService.findByEmail(email)).thenReturn(user);
        Mockito.when(transaccionesService.getTransaccionesByUserId(user.getId())).thenReturn(expectedTransactions);

        // Act
        List<Transacciones> actualTransactions = controller.getTransaccionesByUser(authentication);

        // Assert
        Assertions.assertEquals(expectedTransactions, actualTransactions);
    }

    // Successfully creates a transaction when valid data is provided
    @Test
    public void test_create_transaccion_with_valid_data() {
        // Arrange
        TransaccionesService transaccionesService = mock(TransaccionesService.class);
        UserService userService = mock(UserService.class);
        TransaccionesController controller = new TransaccionesController(transaccionesService, userService);
        Authentication authentication = mock(Authentication.class);
        Transacciones transaccion = new Transacciones();
        String email = "test@example.com";

        when(authentication.getName()).thenReturn(email);
        when(transaccionesService.createTransaccion(transaccion, email)).thenReturn(transaccion);

        // Act
        Transacciones result = controller.createTransaccion(transaccion, authentication);

        // Assert
        assertNotNull(result);
        assertEquals(transaccion, result);
        verify(transaccionesService).createTransaccion(transaccion, email);
    }

    // Successfully deletes a transaction when a valid ID and authenticated user are
    // provided
    @Test
    public void test_delete_transaccion_success() {
        // Arrange
        Long validId = 1L;
        Authentication authentication = Mockito.mock(Authentication.class);
        TransaccionesService transaccionesService = Mockito.mock(TransaccionesService.class);
        UserService userService = Mockito.mock(UserService.class);
        TransaccionesController controller = new TransaccionesController(transaccionesService, userService);

        Mockito.when(authentication.getName()).thenReturn("user@example.com");

        // Act
        ResponseEntity<Void> response = controller.deleteTransaccion(validId, authentication);

        // Assert
        Mockito.verify(transaccionesService).deleteTransaccion(validId, "user@example.com");
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    // Attempts to delete a transaction that does not exist, resulting in a not
    // found response
    @Test
    public void test_delete_transaccion_not_found() {
        // Arrange
        Long invalidId = 999L;
        Authentication authentication = Mockito.mock(Authentication.class);
        TransaccionesService transaccionesService = Mockito.mock(TransaccionesService.class);
        UserService userService = Mockito.mock(UserService.class);
        TransaccionesController controller = new TransaccionesController(transaccionesService, userService);

        Mockito.when(authentication.getName()).thenReturn("user@example.com");
        Mockito.doThrow(new TransaccionNotFoundException("Error")).when(transaccionesService)
                .deleteTransaccion(invalidId, "user@example.com");

        // Act
        ResponseEntity<Void> response = controller.deleteTransaccion(invalidId, authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
 */