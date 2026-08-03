package api.back;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public class PersonalTipoGastoControllerTest {

    @Mock
    private PersonalTipoGastoService personalTipoGastoService;

    @Mock
    private TransaccionesController transaccionesController;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PersonalTipoGastoController personalTipoGastoController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(authentication.getName()).thenReturn("test@example.com");
    }

    @Test
    public void testGetPersonalTipoGastos() {
        // Arrange
        PersonalTipoGasto tipoGasto1 = new PersonalTipoGasto();
        tipoGasto1.setNombre("Supermercado");
        
        PersonalTipoGasto tipoGasto2 = new PersonalTipoGasto();
        tipoGasto2.setNombre("Transporte");
        
        List<PersonalTipoGasto> tipoGastosList = Arrays.asList(tipoGasto1, tipoGasto2);
        
        when(personalTipoGastoService.getPersonalTipoGastos("test@example.com")).thenReturn(tipoGastosList);
        
        // Act
        List<PersonalTipoGasto> result = personalTipoGastoController.getPersonalTipoGastos(authentication);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Supermercado", result.get(0).getNombre());
        assertEquals("Transporte", result.get(1).getNombre());
        
        verify(personalTipoGastoService).getPersonalTipoGastos("test@example.com");
    }
    
    @Test
    public void testAddPersonalTipoGasto() {
        // Arrange
        String nombre = "\"Entretenimiento\"";
        
        PersonalTipoGasto tipoGasto = new PersonalTipoGasto();
        tipoGasto.setNombre("Entretenimiento");
        
        when(personalTipoGastoService.addPersonalTipoGasto(anyString(), anyString())).thenReturn(tipoGasto);
        
        // Act
        PersonalTipoGasto result = personalTipoGastoController.addPersonalTipoGasto(nombre, authentication);
        
        // Assert
        assertNotNull(result);
        assertEquals("Entretenimiento", result.getNombre());
        
        verify(personalTipoGastoService).addPersonalTipoGasto("test@example.com", "Entretenimiento");
    }
    
    @Test
    public void testUpdatePersonalTipoGasto() {
        // Arrange
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("nombreActual", "Supermercado");
        requestBody.put("nombreNuevo", "Alimentos");
        
        PersonalTipoGasto tipoGasto = new PersonalTipoGasto();
        tipoGasto.setNombre("Alimentos");
        
        Transacciones transaccion1 = new Transacciones();
        transaccion1.setId(1L);
        transaccion1.setTipoGasto("Supermercado");
        
        Transacciones transaccion2 = new Transacciones();
        transaccion2.setId(2L);
        transaccion2.setTipoGasto("Transporte");
        
        List<Transacciones> transacciones = Arrays.asList(transaccion1, transaccion2);
        
        when(transaccionesController.getTransaccionesByUser(authentication)).thenReturn(transacciones);
        when(personalTipoGastoService.updatePersonalTipoGasto(anyString(), anyString(), anyString())).thenReturn(tipoGasto);
        when(transaccionesController.updateTransaccion(anyLong(), any(Transacciones.class), any(Authentication.class)))
            .thenReturn(new Transacciones());
        
        // Act
        PersonalTipoGasto result = personalTipoGastoController.updatePersonalTipoGasto(requestBody, authentication);
        
        // Assert
        assertNotNull(result);
        assertEquals("Alimentos", result.getNombre());
        
        verify(personalTipoGastoService).updatePersonalTipoGasto("test@example.com", "Supermercado", "Alimentos");
        verify(transaccionesController).updateTransaccion(eq(1L), any(Transacciones.class), eq(authentication));
        verify(transaccionesController, never()).updateTransaccion(eq(2L), any(Transacciones.class), eq(authentication));
    }
    
    @Test
    public void testDeletePersonalTipoGasto() {
        // Arrange
        String nombre = "\"Entretenimiento\"";
        
        Transacciones transaccion1 = new Transacciones();
        transaccion1.setId(1L);
        transaccion1.setTipoGasto("Entretenimiento");
        
        Transacciones transaccion2 = new Transacciones();
        transaccion2.setId(2L);
        transaccion2.setTipoGasto("Transporte");
        
        List<Transacciones> transacciones = Arrays.asList(transaccion1, transaccion2);
        
        when(transaccionesController.getTransaccionesByUser(authentication)).thenReturn(transacciones);
        doNothing().when(personalTipoGastoService).deletePersonalTipoGastoByName(anyString(), anyString());
        when(transaccionesController.updateTransaccion(anyLong(), any(Transacciones.class), any(Authentication.class)))
            .thenReturn(new Transacciones());
        
        // Act
        ResponseEntity<Void> response = personalTipoGastoController.deletePersonalTipoGasto(nombre, authentication);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        
        verify(personalTipoGastoService).deletePersonalTipoGastoByName("test@example.com", "Entretenimiento");
        verify(transaccionesController).updateTransaccion(eq(1L), argThat(transaccion -> 
            "Otros".equals(transaccion.getTipoGasto())), eq(authentication));
        verify(transaccionesController, never()).updateTransaccion(eq(2L), any(Transacciones.class), eq(authentication));
    }
    
    @Test
    public void testDeletePersonalTipoGasto_NoMatchingTransactions() {
        // Arrange
        String nombre = "\"Hobbies\"";
        
        Transacciones transaccion1 = new Transacciones();
        transaccion1.setId(1L);
        transaccion1.setTipoGasto("Entretenimiento");
        
        Transacciones transaccion2 = new Transacciones();
        transaccion2.setId(2L);
        transaccion2.setTipoGasto("Transporte");
        
        List<Transacciones> transacciones = Arrays.asList(transaccion1, transaccion2);
        
        when(transaccionesController.getTransaccionesByUser(authentication)).thenReturn(transacciones);
        doNothing().when(personalTipoGastoService).deletePersonalTipoGastoByName(anyString(), anyString());
        
        // Act
        ResponseEntity<Void> response = personalTipoGastoController.deletePersonalTipoGasto(nombre, authentication);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        
        verify(personalTipoGastoService).deletePersonalTipoGastoByName("test@example.com", "Hobbies");
        verify(transaccionesController, never()).updateTransaccion(anyLong(), any(Transacciones.class), any(Authentication.class));
    }
    
    @Test
    public void testDeletePersonalTipoGasto_WithNullTipoGasto() {
        // Arrange
        String nombre = "\"Entretenimiento\"";
        
        Transacciones transaccion1 = new Transacciones();
        transaccion1.setId(1L);
        transaccion1.setTipoGasto(null);
        
        List<Transacciones> transacciones = Arrays.asList(transaccion1);
        
        when(transaccionesController.getTransaccionesByUser(authentication)).thenReturn(transacciones);
        doNothing().when(personalTipoGastoService).deletePersonalTipoGastoByName(anyString(), anyString());
        
        // Act
        ResponseEntity<Void> response = personalTipoGastoController.deletePersonalTipoGasto(nombre, authentication);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        
        verify(personalTipoGastoService).deletePersonalTipoGastoByName("test@example.com", "Entretenimiento");
        verify(transaccionesController, never()).updateTransaccion(anyLong(), any(Transacciones.class), any(Authentication.class));
    }
}
