package api.back;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class ExternalApiControllerTest {

    private ExternalApiController externalApiController;
    private UserService userService;
    private TransaccionesPendientesService transaccionesPendientesService;

    @BeforeEach
    public void setUp() {
        userService = mock(UserService.class);
        transaccionesPendientesService = mock(TransaccionesPendientesService.class);
        externalApiController = new ExternalApiController();
        
        // Using reflection to set the mocked dependencies
        java.lang.reflect.Field userServiceField;
        java.lang.reflect.Field transaccionesPendientesServiceField;
        
        try {
            userServiceField = ExternalApiController.class.getDeclaredField("userService");
            userServiceField.setAccessible(true);
            userServiceField.set(externalApiController, userService);
            
            transaccionesPendientesServiceField = ExternalApiController.class.getDeclaredField("transaccionesPendientesService");
            transaccionesPendientesServiceField.setAccessible(true);
            transaccionesPendientesServiceField.set(externalApiController, transaccionesPendientesService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPostPendingTransaccion_Success() {
        // Arrange
        String email = "test@example.com";
        double valor = 100.0;
        String motivo = "Test transaction";
        String idReserva = "RES-001";
        LocalDate fecha = LocalDate.now();

        User user = new User();
        user.setEmail(email);
        user.setId(1L);

        TransaccionRequest request = new TransaccionRequest();
        request.setEmail(email);
        request.setValor(valor);
        request.setMotivo(motivo);
        request.setId_reserva(idReserva);
        request.setFecha(fecha);

        when(userService.findByEmail(email)).thenReturn(user);

        // Act
        ResponseEntity<String> response = externalApiController.postPendingTransaccion(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Transacción pendiente registrada correctamente.", response.getBody());
        
        // Verify that the service methods were called
        verify(transaccionesPendientesService).save(any(TransaccionesPendientes.class));
        verify(userService).pendingTransactionNotification(email);
    }

    @Test
    public void testPostPendingTransaccion_UserNotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        TransaccionRequest request = new TransaccionRequest();
        request.setEmail(email);
        request.setValor(100.0);
        request.setMotivo("Test transaction");

        when(userService.findByEmail(email)).thenReturn(null);

        // Act
        ResponseEntity<String> response = externalApiController.postPendingTransaccion(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Usuario no registrado.", response.getBody());
        
        // Verify that save and notification were not called
        verify(transaccionesPendientesService, never()).save(any(TransaccionesPendientes.class));
        verify(userService, never()).pendingTransactionNotification(anyString());
    }

    @Test
    public void testPostPendingTransaccion_NegativeValue() {
        // Arrange
        String email = "test@example.com";
        double valor = -50.0; // Negative value
        User user = new User();
        user.setEmail(email);
        
        TransaccionRequest request = new TransaccionRequest();
        request.setEmail(email);
        request.setValor(valor);
        request.setMotivo("Test transaction");

        when(userService.findByEmail(email)).thenReturn(user);

        // Act
        ResponseEntity<String> response = externalApiController.postPendingTransaccion(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error: Valor negativo", response.getBody());
        
        // Verify that save and notification were not called
        verify(transaccionesPendientesService, never()).save(any(TransaccionesPendientes.class));
        verify(userService, never()).pendingTransactionNotification(anyString());
    }

    @Test
    public void testPostPendingTransaccion_NullDate() {
        // Arrange
        String email = "test@example.com";
        double valor = 100.0;
        String motivo = "Test transaction";
        String idReserva = "RES-001";
        
        User user = new User();
        user.setEmail(email);
        user.setId(1L);

        TransaccionRequest request = new TransaccionRequest();
        request.setEmail(email);
        request.setValor(valor);
        request.setMotivo(motivo);
        request.setId_reserva(idReserva);
        request.setFecha(null); // Null date, should default to LocalDate.now()

        when(userService.findByEmail(email)).thenReturn(user);

        // Act
        ResponseEntity<String> response = externalApiController.postPendingTransaccion(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Transacción pendiente registrada correctamente.", response.getBody());
        
        // Verify that save and notification were called
        verify(transaccionesPendientesService).save(any(TransaccionesPendientes.class));
        verify(userService).pendingTransactionNotification(email);
    }

    @Test
    public void testCheckUserExists_UserExists() {
        // Arrange
        String email = "existing@example.com";
        User user = new User();
        user.setEmail(email);
        
        when(userService.findByEmail(email)).thenReturn(user);

        // Act
        ResponseEntity<Boolean> response = externalApiController.checkUserExists(email);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
    }

    @Test
    public void testCheckUserExists_UserDoesNotExist() {
        // Arrange
        String email = "nonexistent@example.com";
        
        when(userService.findByEmail(email)).thenReturn(null);

        // Act
        ResponseEntity<Boolean> response = externalApiController.checkUserExists(email);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(false, response.getBody());
    }
}
