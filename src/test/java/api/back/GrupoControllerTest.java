package api.back;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public class GrupoControllerTest {

    @Mock
    private GrupoService grupoService;

    @Mock
    private UserService userService;

    @Mock
    private GrupoTransaccionesService grupoTransaccionesService;

    @Mock
    private TransaccionesService transaccionesService;

    @Mock
    private TransaccionesPendientesService transaccionesPendientesService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private GrupoController grupoController;

    private User testUser;
    private Grupo testGrupo;
    private GrupoTransacciones testTransaccion;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        
        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        
        // Create test group - don't add testUser to it by default
        testGrupo = new Grupo();
        testGrupo.setId(1L);
        testGrupo.setNombre("Test Group");
        testGrupo.setEstado(true);
        
        // Create test transaction
        testTransaccion = new GrupoTransacciones(100.0, "Test motivo", LocalDate.now(), "Test categoria", "Test tipo gasto", "test@example.com");
        testTransaccion.setId(1L);
        testTransaccion.setGrupo(testGrupo);
        
        // Mock authentication
        when(authentication.getName()).thenReturn("test@example.com");
        
        // Mock userService
        when(userService.findByEmail("test@example.com")).thenReturn(testUser);
        
        // Mock grupoService
        when(grupoService.findById(1L)).thenReturn(testGrupo);
        when(grupoService.crearGrupo(anyString(), any(User.class))).thenReturn(testGrupo);
        when(grupoService.save(any(Grupo.class))).thenReturn(testGrupo);
    }

    @Test
    public void testCrearGrupo() {
        // Arrange
        Map<String, Object> payload = new HashMap<>();
        payload.put("nombre", "Test Group");
        List<String> miembrosEmails = new ArrayList<>();
        miembrosEmails.add("member1@example.com");
        payload.put("usuarios", miembrosEmails);
        
        User memberUser = new User();
        memberUser.setId(2L);
        memberUser.setEmail("member1@example.com");
        
        when(userService.findByEmail("member1@example.com")).thenReturn(memberUser);
        
        // Act
        Grupo result = grupoController.crearGrupo(payload, authentication);
        
        // Assert
        assertNotNull(result);
        assertEquals("Test Group", result.getNombre());
        verify(transaccionesPendientesService).save(any(TransaccionesPendientes.class));
        verify(grupoService).crearGrupo(eq("Test Group"), eq(testUser));
    }

    @Test
    public void testObtenerGruposDelUsuario() {
        // Arrange
        List<Grupo> grupos = Arrays.asList(testGrupo);
        when(grupoService.obtenerGruposPorUsuario("test@example.com")).thenReturn(grupos);
        
        // Act
        List<Grupo> result = grupoController.obtenerGruposDelUsuario(authentication);
        
        // Assert
        assertEquals(1, result.size());
        assertEquals(testGrupo, result.get(0));
        verify(grupoService).obtenerGruposPorUsuario("test@example.com");
    }

    @Test
    public void testAgregarUsuarioAGrupo() {
        // Arrange
        Map<String, Object> payload = new HashMap<>();
        payload.put("grupo_id", 1L);
        
        // Create a fresh test grupo instance to ensure usuarios is an empty list
        Grupo freshGrupo = new Grupo();
        freshGrupo.setId(1L);
        freshGrupo.setEstado(true);
        freshGrupo.setNombre("Test Group");
        
        // Override the grupoService mock for this test
        when(grupoService.findById(1L)).thenReturn(freshGrupo);
        
        // Act
        ResponseEntity<String> response = grupoController.agregarUsuarioAGrupo(payload, authentication);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Usuario agregado al grupo exitosamente.", response.getBody());
        verify(grupoService).save(freshGrupo);
    }

    @Test
    public void testAgregarUsuarioAGrupoYaExistente() {
        // Arrange
        Map<String, Object> payload = new HashMap<>();
        payload.put("grupo_id", 1L);
        
        // Simulate user already in group
        testGrupo.getUsuarios().add(testUser);
        
        // Act
        ResponseEntity<String> response = grupoController.agregarUsuarioAGrupo(payload, authentication);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("El usuario ya es miembro del grupo.", response.getBody());
        verify(grupoService, never()).save(any(Grupo.class));
    }

    @Test
    public void testCrearGrupoTransaccion() {
        // Arrange
        Map<String, Object> payload = new HashMap<>();
        payload.put("valor", 100.0);
        payload.put("motivo", "Test motivo");
        payload.put("fecha", LocalDate.now().toString());
        payload.put("categoria", "Test categoria");
        payload.put("tipoGasto", "Test tipo gasto");
        payload.put("grupo", 1L);
        
        when(grupoTransaccionesService.save(any(GrupoTransacciones.class))).thenReturn(testTransaccion);
        
        // Act
        ResponseEntity<GrupoTransacciones> response = grupoController.crearGrupoTransaccion(payload, authentication);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100.0, response.getBody().getValor());
        verify(grupoTransaccionesService).save(any(GrupoTransacciones.class));
    }

    @Test
    public void testObtenerTransaccionesPorGrupo() {
        // Arrange
        List<GrupoTransacciones> transacciones = Arrays.asList(testTransaccion);
        testGrupo.setTransacciones(transacciones);
        
        // Act
        ResponseEntity<List<GrupoTransacciones>> response = grupoController.obtenerTransaccionesPorGrupo(1L);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testTransaccion, response.getBody().get(0));
    }

    @Test
    public void testObtenerTransaccionesPorGrupoNoExistente() {
        // Arrange
        when(grupoService.findById(999L)).thenReturn(null);
        
        // Act
        ResponseEntity<List<GrupoTransacciones>> response = grupoController.obtenerTransaccionesPorGrupo(999L);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    public void testCerrarGrupo() {
        // Arrange
        List<TransaccionesPendientes> pendientes = new ArrayList<>();
        TransaccionesPendientes pendiente = new TransaccionesPendientes();
        pendiente.setId(1L);
        pendientes.add(pendiente);
        
        when(transaccionesPendientesService.findByGrupoId(1L)).thenReturn(pendientes);
        
        List<User> usuarios = Arrays.asList(testUser);
        testGrupo.setUsuarios(usuarios);
        
        List<GrupoTransacciones> transacciones = Arrays.asList(testTransaccion);
        testGrupo.setTransacciones(transacciones);
        
        // Act
        ResponseEntity<String> response = grupoController.cerrarGrupo(1L);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("El grupo ha sido cerrado exitosamente.", response.getBody());
        verify(transaccionesPendientesService).delete(1L);
        verify(transaccionesService).createTransaccion(any(Transacciones.class), eq("test@example.com"));
        verify(grupoService).save(testGrupo);
        assertFalse(testGrupo.isEstado());
    }

    @Test
    public void testEliminarGrupoTransaccion() {
        // Arrange
        when(grupoTransaccionesService.findById(1L)).thenReturn(testTransaccion);
        
        // Act
        ResponseEntity<String> response = grupoController.eliminarGrupoTransaccion(1L);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Transacción eliminada exitosamente.", response.getBody());
        verify(grupoTransaccionesService).delete(1L);
    }

    @Test
    public void testEliminarGrupoTransaccionNoExistente() {
        // Arrange
        when(grupoTransaccionesService.findById(999L)).thenReturn(null);
        
        // Act
        ResponseEntity<String> response = grupoController.eliminarGrupoTransaccion(999L);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Transacción no encontrada.", response.getBody());
        verify(grupoTransaccionesService, never()).delete(anyLong());
    }

    @Test
    public void testEditarGrupoTransaccion() {
        // Arrange
        Map<String, Object> payload = new HashMap<>();
        payload.put("valor", 200.0);
        payload.put("motivo", "Motivo actualizado");
        payload.put("fecha", LocalDate.now().toString());
        payload.put("categoria", "Categoria actualizada");
        payload.put("tipoGasto", "Tipo gasto actualizado");
        
        when(grupoTransaccionesService.findById(1L)).thenReturn(testTransaccion);
        when(grupoTransaccionesService.save(any(GrupoTransacciones.class))).thenReturn(testTransaccion);
        
        // Act
        ResponseEntity<GrupoTransacciones> response = grupoController.editarGrupoTransaccion(1L, payload);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(grupoTransaccionesService).save(testTransaccion);
    }

    @Test
    public void testEditarGrupoTransaccionNoExistente() {
        // Arrange
        Map<String, Object> payload = new HashMap<>();
        payload.put("valor", 200.0);
        
        when(grupoTransaccionesService.findById(999L)).thenReturn(null);
        
        // Act
        ResponseEntity<GrupoTransacciones> response = grupoController.editarGrupoTransaccion(999L, payload);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(grupoTransaccionesService, never()).save(any(GrupoTransacciones.class));
    }

    @Test
    public void testEliminarGrupo() {
        // Arrange
        List<GrupoTransacciones> transacciones = Arrays.asList(testTransaccion);
        testGrupo.setTransacciones(transacciones);
        
        List<TransaccionesPendientes> pendientes = new ArrayList<>();
        TransaccionesPendientes pendiente = new TransaccionesPendientes();
        pendiente.setId(1L);
        pendientes.add(pendiente);
        
        when(transaccionesPendientesService.findByGrupoId(1L)).thenReturn(pendientes);
        
        // Act
        ResponseEntity<String> response = grupoController.eliminarGrupo(1L);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Grupo y todas sus transacciones eliminadas exitosamente.", response.getBody());
        verify(grupoTransaccionesService).delete(1L);
        verify(transaccionesPendientesService).delete(1L);
        verify(grupoService).delete(1L);
    }

    @Test
    public void testObtenerUsuariosDelGrupo() {
        // Arrange
        List<User> usuarios = Arrays.asList(testUser);
        testGrupo.setUsuarios(usuarios);
        
        // Act
        ResponseEntity<List<User>> response = grupoController.obtenerUsuariosDelGrupo(1L);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testUser, response.getBody().get(0));
    }

    @Test
    public void testAgregarUsuariosAGrupo() {
        // Arrange
        Map<String, Object> payload = new HashMap<>();
        List<String> miembrosEmails = new ArrayList<>();
        miembrosEmails.add("member1@example.com");
        payload.put("usuarios", miembrosEmails);
        
        User memberUser = new User();
        memberUser.setId(2L);
        memberUser.setEmail("member1@example.com");
        
        when(userService.findByEmail("member1@example.com")).thenReturn(memberUser);
        
        // Act
        ResponseEntity<String> response = grupoController.agregarUsuariosAGrupo(1L, payload, authentication);
        
        // Assert
        verify(transaccionesPendientesService).save(any(TransaccionesPendientes.class));
        assertNull(response); // Because the method returns null
    }

    @Test
    public void testVerificarUsuarioEnGrupoOInvitado_UsuarioYaEsMiembro() {
        // Arrange
        testGrupo.getUsuarios().add(testUser);
        
        // Act
        ResponseEntity<String> response = grupoController.verificarUsuarioEnGrupoOInvitado(1L, "test@example.com");
        
        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("El usuario ya es miembro del grupo.", response.getBody());
    }

    @Test
    public void testVerificarUsuarioEnGrupoOInvitado_UsuarioConInvitacionPendiente() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@example.com");
        
        List<TransaccionesPendientes> pendientes = new ArrayList<>();
        TransaccionesPendientes pendiente = new TransaccionesPendientes();
        pendiente.setUser(otherUser);
        pendientes.add(pendiente);
        
        when(userService.findByEmail("other@example.com")).thenReturn(otherUser);
        when(transaccionesPendientesService.findByGrupoId(1L)).thenReturn(pendientes);
        
        // Act
        ResponseEntity<String> response = grupoController.verificarUsuarioEnGrupoOInvitado(1L, "other@example.com");
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("El usuario ya tiene una invitación pendiente para el grupo.", response.getBody());
    }

    @Test
    public void testVerificarUsuarioEnGrupoOInvitado_UsuarioLibre() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@example.com");
        
        when(userService.findByEmail("other@example.com")).thenReturn(otherUser);
        when(transaccionesPendientesService.findByGrupoId(1L)).thenReturn(new ArrayList<>());
        
        // Act
        ResponseEntity<String> response = grupoController.verificarUsuarioEnGrupoOInvitado(1L, "other@example.com");
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("El usuario no está en el grupo ni tiene una invitación pendiente.", response.getBody());
    }
}
