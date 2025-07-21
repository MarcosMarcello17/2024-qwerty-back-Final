package api.back;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public class PersonalCategoriaControllerTest {

    @Mock
    private PersonalCategoriaService personalCategoriaService;

    @Mock
    private TransaccionesController transaccionesController;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PersonalCategoriaController personalCategoriaController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(authentication.getName()).thenReturn("test@example.com");
    }

    @Test
    public void testGetPersonalCategoria() {
        // Arrange
        PersonalCategoria categoria1 = new PersonalCategoria();
        categoria1.setNombre("Categoria 1");
        categoria1.setIconPath("icon1.png");

        PersonalCategoria categoria2 = new PersonalCategoria();
        categoria2.setNombre("Categoria 2");
        categoria2.setIconPath("icon2.png");

        List<PersonalCategoria> categoriasList = Arrays.asList(categoria1, categoria2);

        when(personalCategoriaService.getPersonalCategoria("test@example.com")).thenReturn(categoriasList);

        // Act
        List<CategoriaRequest> result = personalCategoriaController.getPersonalCategoria(authentication);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Categoria 1", result.get(0).getNombre());
        assertEquals("icon1.png", result.get(0).getIconPath());
        assertEquals("Categoria 2", result.get(1).getNombre());
        assertEquals("icon2.png", result.get(1).getIconPath());
        
        verify(personalCategoriaService).getPersonalCategoria("test@example.com");
    }

    @Test
    public void testAddPersonalCategoria_Success() {
        // Arrange
        CategoriaRequest newCategoria = new CategoriaRequest("Nueva Categoria", "nuevo_icono.png");
        
        when(personalCategoriaService.checkIfNotExist(anyString(), any(CategoriaRequest.class))).thenReturn(true);
        when(personalCategoriaService.addPersonalCategoria(anyString(), anyString(), anyString()))
            .thenReturn(new PersonalCategoria());

        // Act
        ResponseEntity<CategoriaRequest> response = personalCategoriaController.addPersonalCategoria(newCategoria, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Nueva Categoria", response.getBody().getNombre());
        assertEquals("nuevo_icono.png", response.getBody().getIconPath());
        
        verify(personalCategoriaService).checkIfNotExist("test@example.com", newCategoria);
        verify(personalCategoriaService).addPersonalCategoria("test@example.com", "Nueva Categoria", "nuevo_icono.png");
    }
    
    @Test
    public void testAddPersonalCategoria_CategoryAlreadyExists() {
        // Arrange
        CategoriaRequest newCategoria = new CategoriaRequest("Categoria Existente", "icono.png");
        
        when(personalCategoriaService.checkIfNotExist(anyString(), any(CategoriaRequest.class))).thenReturn(false);

        // Act
        ResponseEntity<CategoriaRequest> response = personalCategoriaController.addPersonalCategoria(newCategoria, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(personalCategoriaService).checkIfNotExist("test@example.com", newCategoria);
        verify(personalCategoriaService, never()).addPersonalCategoria(anyString(), anyString(), anyString());
    }
    
    @Test
    public void testDeletePersonalCategoria_Success() {
        // Arrange
        CategoriaRequest categoria = new CategoriaRequest("Categoria a Eliminar", "icono.png");
        List<Transacciones> transacciones = Arrays.asList();
        
        when(transaccionesController.getTransaccionesByUser(authentication)).thenReturn(transacciones);
        doNothing().when(personalCategoriaService).findAndDeleteCategoria(anyString(), anyString(), anyString());

        // Act
        ResponseEntity<Void> response = personalCategoriaController.deletePersonalCategoria(categoria, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        verify(transaccionesController).getTransaccionesByUser(authentication);
        verify(personalCategoriaService).findAndDeleteCategoria("test@example.com", "Categoria a Eliminar", "icono.png");
    }
    
    @Test
    public void testDeletePersonalCategoria_UpdatesTransactionsCategories() {
        // Arrange
        CategoriaRequest categoria = new CategoriaRequest("Categoria a Eliminar", "icono.png");
        
        Transacciones transaccion1 = new Transacciones();
        transaccion1.setId(1L);
        transaccion1.setCategoria("Categoria a Eliminar");
        
        Transacciones transaccion2 = new Transacciones();
        transaccion2.setId(2L);
        transaccion2.setCategoria("Otra Categoria");
        
        List<Transacciones> transacciones = Arrays.asList(transaccion1, transaccion2);
        
        when(transaccionesController.getTransaccionesByUser(authentication)).thenReturn(transacciones);
        doNothing().when(personalCategoriaService).findAndDeleteCategoria(anyString(), anyString(), anyString());
        when(transaccionesController.updateTransaccion(anyLong(), any(Transacciones.class), any(Authentication.class)))
            .thenReturn(new Transacciones());

        // Act
        ResponseEntity<Void> response = personalCategoriaController.deletePersonalCategoria(categoria, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        verify(transaccionesController).getTransaccionesByUser(authentication);
        verify(transaccionesController).updateTransaccion(eq(1L), any(Transacciones.class), eq(authentication));
        verify(transaccionesController, never()).updateTransaccion(eq(2L), any(Transacciones.class), eq(authentication));
        verify(personalCategoriaService).findAndDeleteCategoria("test@example.com", "Categoria a Eliminar", "icono.png");
    }
    
    @Test
    public void testDeletePersonalCategoria_TransactionNotFound() {
        // Arrange
        CategoriaRequest categoria = new CategoriaRequest("Categoria a Eliminar", "icono.png");
        
        when(transaccionesController.getTransaccionesByUser(authentication)).thenThrow(new TransaccionNotFoundException("Transaccion no encontrada"));

        // Act
        ResponseEntity<Void> response = personalCategoriaController.deletePersonalCategoria(categoria, authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        verify(transaccionesController).getTransaccionesByUser(authentication);
        verify(personalCategoriaService, never()).findAndDeleteCategoria(anyString(), anyString(), anyString());
    }
    
    @Test
    public void testEditPersonalCategoria_Success() {
        // Arrange
        String oldNombre = "Categoria Antigua";
        CategoriaRequest newCategoria = new CategoriaRequest("Categoria Nueva", "nuevo_icono.png");
        
        PersonalCategoria categoriaExistente = new PersonalCategoria();
        categoriaExistente.setNombre(oldNombre);
        categoriaExistente.setIconPath("icono_viejo.png");
        
        List<PersonalCategoria> categorias = Arrays.asList(categoriaExistente);
        
        Transacciones transaccion = new Transacciones();
        transaccion.setId(1L);
        transaccion.setCategoria(oldNombre);
        
        List<Transacciones> transacciones = Arrays.asList(transaccion);
        
        when(personalCategoriaService.checkIfNotExist(anyString(), any(CategoriaRequest.class))).thenReturn(true);
        when(personalCategoriaService.getPersonalCategoria(anyString())).thenReturn(categorias);
        doNothing().when(personalCategoriaService).save(any(PersonalCategoria.class));
        when(transaccionesController.getTransaccionesByUser(authentication)).thenReturn(transacciones);
        when(transaccionesController.updateTransaccion(anyLong(), any(Transacciones.class), any(Authentication.class)))
            .thenReturn(new Transacciones());

        // Act
        ResponseEntity<Void> response = personalCategoriaController.editPersonalCategoria(oldNombre, newCategoria, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        verify(personalCategoriaService).checkIfNotExist("test@example.com", newCategoria);
        verify(personalCategoriaService).getPersonalCategoria("test@example.com");
        verify(personalCategoriaService).save(categoriaExistente);
        verify(transaccionesController).getTransaccionesByUser(authentication);
        verify(transaccionesController).updateTransaccion(eq(1L), any(Transacciones.class), eq(authentication));
        
        // Verify the categoria was updated correctly
        assertEquals("Categoria Nueva", categoriaExistente.getNombre());
        assertEquals("nuevo_icono.png", categoriaExistente.getIconPath());
    }
    
    @Test
    public void testEditPersonalCategoria_CategoryNameAlreadyExists() {
        // Arrange
        String oldNombre = "Categoria Antigua";
        CategoriaRequest newCategoria = new CategoriaRequest("Categoria Nueva", "nuevo_icono.png");
        
        // Mock the behavior
        when(personalCategoriaService.getPersonalCategoria(anyString())).thenReturn(Arrays.asList());
        when(personalCategoriaService.checkIfNotExist(anyString(), any(CategoriaRequest.class))).thenReturn(false);

        // Act
        ResponseEntity<Void> response = personalCategoriaController.editPersonalCategoria(oldNombre, newCategoria, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        verify(personalCategoriaService).getPersonalCategoria("test@example.com");
        verify(personalCategoriaService).checkIfNotExist("test@example.com", newCategoria);
        verify(personalCategoriaService, never()).save(any(PersonalCategoria.class));
    }
    
    @Test
    public void testEditPersonalCategoria_CategoryNotFound() {
        // Arrange
        String oldNombre = "Categoria No Existente";
        CategoriaRequest newCategoria = new CategoriaRequest("Categoria Nueva", "nuevo_icono.png");
        
        PersonalCategoria categoriaExistente = new PersonalCategoria();
        categoriaExistente.setNombre("Otra Categoria");
        
        List<PersonalCategoria> categorias = Arrays.asList(categoriaExistente);
        
        when(personalCategoriaService.checkIfNotExist(anyString(), any(CategoriaRequest.class))).thenReturn(true);
        when(personalCategoriaService.getPersonalCategoria(anyString())).thenReturn(categorias);

        // Act
        ResponseEntity<Void> response = personalCategoriaController.editPersonalCategoria(oldNombre, newCategoria, authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        verify(personalCategoriaService).checkIfNotExist("test@example.com", newCategoria);
        verify(personalCategoriaService).getPersonalCategoria("test@example.com");
        verify(personalCategoriaService, never()).save(any(PersonalCategoria.class));
    }
    
    @Test
    public void testEditPersonalCategoria_InternalServerError() {
        // Arrange
        String oldNombre = "Categoria Antigua";
        CategoriaRequest newCategoria = new CategoriaRequest("Categoria Nueva", "nuevo_icono.png");
        
        when(personalCategoriaService.checkIfNotExist(anyString(), any(CategoriaRequest.class))).thenThrow(new RuntimeException("Error interno"));

        // Act
        ResponseEntity<Void> response = personalCategoriaController.editPersonalCategoria(oldNombre, newCategoria, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
