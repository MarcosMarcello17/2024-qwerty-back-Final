package api.back;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class AutomationControllerTest {

    @Mock
    private AutomationService automationService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AutomationController automationController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(authentication.getName()).thenReturn("test@example.com");
    }

    @Test
    public void testDistribuirIngresoExitoso() {
        // Arrange
        AutomationController.DistribucionRequest request = new AutomationController.DistribucionRequest();
        request.setMontoIngreso(1000.0);
        request.setFechaIngreso(LocalDate.now());
        request.setMotivoOriginal("Salario mensual");

        List<Transacciones> transaccionesCreadas = new ArrayList<>();
        Transacciones t1 = new Transacciones(500.0, "Distribución automática - Comida", LocalDate.now(), "Comida", "Gasto");
        Transacciones t2 = new Transacciones(500.0, "Distribución automática - Transporte", LocalDate.now(), "Transporte", "Gasto");
        transaccionesCreadas.add(t1);
        transaccionesCreadas.add(t2);

        when(automationService.distribuirIngresoAutomaticamente(
                eq(1000.0),
                any(LocalDate.class),
                eq("test@example.com"),
                eq("Salario mensual")
        )).thenReturn(transaccionesCreadas);

        // Act
        ResponseEntity<?> response = automationController.distribuirIngreso(request, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        
        assertTrue((Boolean) responseBody.get("success"));
        assertEquals("Distribución realizada exitosamente", responseBody.get("message"));
        assertEquals(2, responseBody.get("transaccionesCreadas"));
    }

    @Test
    public void testDistribuirIngresoError() {
        // Arrange
        AutomationController.DistribucionRequest request = new AutomationController.DistribucionRequest();
        request.setMontoIngreso(1000.0);
        request.setFechaIngreso(LocalDate.now());
        request.setMotivoOriginal("Salario mensual");

        when(automationService.distribuirIngresoAutomaticamente(
                any(Double.class),
                any(LocalDate.class),
                anyString(),
                anyString()
        )).thenThrow(new RuntimeException("Error de prueba"));

        // Act
        ResponseEntity<?> response = automationController.distribuirIngreso(request, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        
        assertFalse((Boolean) responseBody.get("success"));
        assertTrue(responseBody.get("error").toString().contains("Error de prueba"));
    }

    @Test
    public void testDistribuirIngresoExistenteExitoso() {
        // Arrange
        Long transaccionId = 123L;

        List<Transacciones> transaccionesCreadas = new ArrayList<>();
        Transacciones t1 = new Transacciones(500.0, "Distribución automática - Comida", LocalDate.now(), "Comida", "Gasto");
        Transacciones t2 = new Transacciones(500.0, "Distribución automática - Transporte", LocalDate.now(), "Transporte", "Gasto");
        transaccionesCreadas.add(t1);
        transaccionesCreadas.add(t2);

        when(automationService.distribuirIngresoExistente(
                eq(123L),
                eq("test@example.com")
        )).thenReturn(transaccionesCreadas);

        // Act
        ResponseEntity<?> response = automationController.distribuirIngresoExistente(transaccionId, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        
        assertTrue((Boolean) responseBody.get("success"));
        assertEquals("Distribución realizada exitosamente", responseBody.get("message"));
        assertEquals(2, responseBody.get("transaccionesCreadas"));
    }

    @Test
    public void testDistribuirIngresoExistenteError() {
        // Arrange
        Long transaccionId = 123L;

        when(automationService.distribuirIngresoExistente(
                eq(123L),
                anyString()
        )).thenThrow(new RuntimeException("Transacción no encontrada"));

        // Act
        ResponseEntity<?> response = automationController.distribuirIngresoExistente(transaccionId, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("Transacción no encontrada", responseBody.get("error"));
    }

    @Test
    public void testPuedeDistribuirTrue() {
        // Arrange
        String fecha = "2025-07-21";
        when(automationService.puedeDistribuirAutomaticamente(
                eq("test@example.com"),
                eq(LocalDate.parse(fecha))
        )).thenReturn(true);

        // Act
        ResponseEntity<?> response = automationController.puedeDistribuir(fecha, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        
        assertTrue((Boolean) responseBody.get("puedeDistribuir"));
    }

    @Test
    public void testPuedeDistribuirFalse() {
        // Arrange
        String fecha = "2025-07-21";
        when(automationService.puedeDistribuirAutomaticamente(
                eq("test@example.com"),
                eq(LocalDate.parse(fecha))
        )).thenReturn(false);

        // Act
        ResponseEntity<?> response = automationController.puedeDistribuir(fecha, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        
        assertFalse((Boolean) responseBody.get("puedeDistribuir"));
    }

    @Test
    public void testPuedeDistribuirError() {
        // Arrange
        String fecha = "2025-07-21";
        when(automationService.puedeDistribuirAutomaticamente(
                anyString(),
                any(LocalDate.class)
        )).thenThrow(new RuntimeException("Error de prueba"));

        // Act
        ResponseEntity<?> response = automationController.puedeDistribuir(fecha, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        
        assertTrue(responseBody.get("error").toString().contains("Error de prueba"));
    }

    @Test
    public void testPrevisualizarDistribucionExitoso() {
        // Arrange
        AutomationController.DistribucionRequest request = new AutomationController.DistribucionRequest();
        request.setMontoIngreso(1000.0);
        request.setFechaIngreso(LocalDate.now());

        AutomationService.DistributionPreview preview = mock(AutomationService.DistributionPreview.class);
        when(preview.getError()).thenReturn(null);

        when(automationService.obtenerPrevisualizacionDistribucion(
                eq(1000.0),
                eq("test@example.com"),
                any(LocalDate.class)
        )).thenReturn(preview);

        // Act
        ResponseEntity<?> response = automationController.previsualizarDistribucion(request, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        
        assertTrue((Boolean) responseBody.get("success"));
        assertEquals(preview, responseBody.get("preview"));
    }

    @Test
    public void testPrevisualizarDistribucionConError() {
        // Arrange
        AutomationController.DistribucionRequest request = new AutomationController.DistribucionRequest();
        request.setMontoIngreso(1000.0);
        request.setFechaIngreso(LocalDate.now());

        AutomationService.DistributionPreview preview = mock(AutomationService.DistributionPreview.class);
        when(preview.getError()).thenReturn("No hay presupuestos para este mes");

        when(automationService.obtenerPrevisualizacionDistribucion(
                any(Double.class),
                anyString(),
                any(LocalDate.class)
        )).thenReturn(preview);

        // Act
        ResponseEntity<?> response = automationController.previsualizarDistribucion(request, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("No hay presupuestos para este mes", responseBody.get("error"));
    }

    @Test
    public void testPrevisualizarDistribucionException() {
        // Arrange
        AutomationController.DistribucionRequest request = new AutomationController.DistribucionRequest();
        request.setMontoIngreso(1000.0);
        request.setFechaIngreso(LocalDate.now());

        when(automationService.obtenerPrevisualizacionDistribucion(
                any(Double.class),
                anyString(),
                any(LocalDate.class)
        )).thenThrow(new RuntimeException("Error inesperado"));

        // Act
        ResponseEntity<?> response = automationController.previsualizarDistribucion(request, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        
        assertFalse((Boolean) responseBody.get("success"));
        assertTrue(responseBody.get("error").toString().contains("Error inesperado"));
    }
}
