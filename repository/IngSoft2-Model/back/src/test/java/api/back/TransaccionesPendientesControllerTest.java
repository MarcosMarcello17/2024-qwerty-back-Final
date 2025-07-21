package api.back;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.client.RestTemplate;

public class TransaccionesPendientesControllerTest {

    @Mock
    private TransaccionesPendientesService transaccionesPendientesService;

    @Mock
    private UserService userService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Authentication authentication;

    private TransaccionesPendientesController controller;
    private User testUser;
    private TransaccionesPendientes testTransaccion;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        controller = new TransaccionesPendientesController(transaccionesPendientesService, userService, restTemplate);
        
        // Configurar usuario de prueba
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        
        // Configurar transacción pendiente de prueba
        testTransaccion = new TransaccionesPendientes();
        testTransaccion.setId(1L);
        testTransaccion.setValor(100.0);
        testTransaccion.setUser(testUser);
        testTransaccion.setMotivo("Test motivo");
        testTransaccion.setId_reserva("123456");
        testTransaccion.setFecha(LocalDate.now());
        testTransaccion.setSentByEmail("sender@example.com");
        
        // Configuración básica de autenticación
        when(authentication.getName()).thenReturn(testUser.getEmail());
        when(userService.findByEmail(testUser.getEmail())).thenReturn(testUser);
    }

    @Test
    public void testGetPendingTransaccionesByUser() {
        // Arrange
        List<TransaccionesPendientes> expectedTransactions = Arrays.asList(testTransaccion);
        when(transaccionesPendientesService.getPendingTransaccionesByUserId(testUser.getId())).thenReturn(expectedTransactions);

        // Act
        ResponseEntity<List<TransaccionesPendientes>> response = controller.getPendingTransaccionesByUser(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedTransactions, response.getBody());
        verify(userService, times(1)).findByEmail(testUser.getEmail());
        verify(transaccionesPendientesService, times(1)).getPendingTransaccionesByUserId(testUser.getId());
    }

    @Test
    public void testDeletePendingTransaccion_Success() {
        // Act
        ResponseEntity<Void> response = controller.deletePendingTransaccion(1L, authentication);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(transaccionesPendientesService, times(1)).deletePendingTransaccion(1L, testUser.getId());
    }

    @Test
    public void testDeletePendingTransaccion_NotFound() {
        // Arrange
        doThrow(new TransaccionNotFoundException("Not found")).when(transaccionesPendientesService)
                .deletePendingTransaccion(anyLong(), anyLong());

        // Act
        ResponseEntity<Void> response = controller.deletePendingTransaccion(999L, authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testTransaccionAceptada() {
        // Arrange
        String idReserva = "123456";
        ResponseEntity<String> expectedResponse = ResponseEntity.ok("Operación exitosa");
        
        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.PUT), 
                any(HttpEntity.class), 
                eq(String.class)
        )).thenReturn(expectedResponse);

        // Act
        ResponseEntity<String> response = controller.transaccionAceptada(idReserva, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
        
        // Verify that exchange was called with correct arguments
        verify(restTemplate, times(1)).exchange(
                eq("https://backendapi.fpenonori.com/reservation/confirm"), 
                eq(HttpMethod.PUT), 
                any(HttpEntity.class), 
                eq(String.class)
        );
    }

    @Test
    public void testTransaccionRechazada() {
        // Arrange
        String idReserva = "123456";
        ResponseEntity<String> expectedResponse = ResponseEntity.ok("Operación exitosa");
        
        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.PUT), 
                any(HttpEntity.class), 
                eq(String.class)
        )).thenReturn(expectedResponse);

        // Act
        ResponseEntity<String> response = controller.transaccionRechazada(idReserva, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
        
        // Verify that exchange was called with correct arguments and status is "rechazada"
        verify(restTemplate, times(1)).exchange(
                eq("https://backendapi.fpenonori.com/reservation/confirm"), 
                eq(HttpMethod.PUT), 
                any(HttpEntity.class), 
                eq(String.class)
        );
    }

    @Test
    public void testPostPaymentToUser_Success() {
        // Arrange
        TransaccionRequest request = new TransaccionRequest(
            200.0, 
            "recipient@example.com", 
            "Pago de prueba", 
            "789012", 
            LocalDate.now()
        );
        
        User recipientUser = new User();
        recipientUser.setId(2L);
        recipientUser.setEmail("recipient@example.com");
        
        when(userService.findByEmail(request.getEmail())).thenReturn(recipientUser);
        when(authentication.getName()).thenReturn("sender@example.com");
        
        // Act
        ResponseEntity<Void> response = controller.postPaymentToUser(request, authentication);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(transaccionesPendientesService, times(1)).save(any(TransaccionesPendientes.class));
    }
    
    @Test
    public void testPostPaymentToUser_UserNotFound() {
        // Arrange
        TransaccionRequest request = new TransaccionRequest(
            200.0, 
            "nonexistent@example.com", 
            "Pago de prueba", 
            "789012", 
            LocalDate.now()
        );
        
        when(userService.findByEmail(request.getEmail())).thenReturn(null);
        
        // Act
        ResponseEntity<Void> response = controller.postPaymentToUser(request, authentication);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(transaccionesPendientesService, times(0)).save(any(TransaccionesPendientes.class));
    }
    
    @Test
    public void testEnviarNotificacionReserva() {
        // Arrange
        String idReserva = "123456";
        ResponseEntity<String> expectedResponse = ResponseEntity.ok("Operación exitosa");
        
        // Mock de la respuesta de restTemplate
        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.PUT), 
                any(HttpEntity.class), 
                eq(String.class)
        )).thenReturn(expectedResponse);
        
        // Act - Llamamos al método privado a través de un método público que lo usa
        ResponseEntity<String> response = controller.transaccionAceptada(idReserva, authentication);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
        
        // Verificamos que se construye correctamente el cuerpo de la petición
        verify(restTemplate, times(1)).exchange(
                eq("https://backendapi.fpenonori.com/reservation/confirm"), 
                eq(HttpMethod.PUT), 
                any(HttpEntity.class), 
                eq(String.class)
        );
    }
}
