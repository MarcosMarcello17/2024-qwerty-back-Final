package api.back;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

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

@SpringBootTest
@ActiveProfiles("test")
public class RecurringTransactionControllerTest {
    
    @Mock
    private RecurringTransactionService recurringTransactionService;
    
    @Mock
    private Authentication authentication;
    
    @InjectMocks
    private RecurringTransactionController recurringTransactionController;
    
    private User testUser;
    private RecurringTransaction testRecurringTransaction;
    private List<RecurringTransaction> testRecurringTransactionList;
    private List<Transacciones> testTransaccionesList;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Configuración del usuario de prueba
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        
        // Configuración de la transacción recurrente de prueba
        testRecurringTransaction = new RecurringTransaction();
        testRecurringTransaction.setId(1L);
        testRecurringTransaction.setMotivo("Netflix Subscription");
        testRecurringTransaction.setValor(15.99);
        testRecurringTransaction.setCategoria("Entretenimiento");
        testRecurringTransaction.setTipoGasto("Tarjeta de credito");
        testRecurringTransaction.setFrecuencia("MENSUAL");
        testRecurringTransaction.setNextExecution(LocalDate.now().plusMonths(1));
        testRecurringTransaction.setUser(testUser);
        
        testRecurringTransactionList = Arrays.asList(testRecurringTransaction);
        
        // Configurar transacción creada para process endpoint
        Transacciones testTransaccion = new Transacciones();
        testTransaccion.setId(1L);
        testTransaccion.setMotivo("Netflix Subscription");
        testTransaccion.setValor(15.99);
        testTransaccion.setCategoria("Entretenimiento");
        testTransaccion.setTipoGasto("Tarjeta de credito");
        testTransaccion.setFecha(LocalDate.now());
        testTransaccion.setUser(testUser);
        
        testTransaccionesList = Arrays.asList(testTransaccion);
        
        // Configurar la autenticación para devolver el email del usuario
        when(authentication.getName()).thenReturn("test@example.com");
    }
    
    @Test
    void testCreateRecurringTransaction() {
        // Configurar el comportamiento del servicio mock
        when(recurringTransactionService.createRecurringTransaction(any(RecurringTransaction.class), anyString()))
                .thenReturn(testRecurringTransaction);
        
        // Realizar la llamada al controlador
        ResponseEntity<RecurringTransaction> response = recurringTransactionController
                .createRecurring(testRecurringTransaction, authentication);
        
        // Verificar los resultados
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Netflix Subscription", response.getBody().getMotivo());
        assertEquals(15.99, response.getBody().getValor());
        
        // Verificar que el método del servicio fue llamado con los parámetros correctos
        verify(recurringTransactionService, times(1))
                .createRecurringTransaction(any(RecurringTransaction.class), eq("test@example.com"));
    }
    
    @Test
    void testGetMyRecurrings() {
        // Configurar el comportamiento del servicio mock
        when(recurringTransactionService.getRecurringTransactions(anyString()))
                .thenReturn(testRecurringTransactionList);
        
        // Realizar la llamada al controlador
        ResponseEntity<List<RecurringTransaction>> response = recurringTransactionController
                .getMyRecurrings(authentication);
        
        // Verificar los resultados
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        
        // Verificar que el método del servicio fue llamado con los parámetros correctos
        verify(recurringTransactionService, times(1))
                .getRecurringTransactions(eq("test@example.com"));
    }
    
    @Test
    void testDeleteRecurring() {
        // Configurar el comportamiento del servicio mock
        doNothing().when(recurringTransactionService)
                .deleteRecurringTransaction(anyLong(), anyString());
        
        // Realizar la llamada al controlador
        ResponseEntity<Void> response = recurringTransactionController
                .deleteRecurring(1L, authentication);
        
        // Verificar los resultados
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        
        // Verificar que el método del servicio fue llamado con los parámetros correctos
        verify(recurringTransactionService, times(1))
                .deleteRecurringTransaction(eq(1L), eq("test@example.com"));
    }
    
    @Test
    void testProcessRecurringTransactions() {
        // Configurar el comportamiento del servicio mock
        when(recurringTransactionService.processRecurringTransactions(anyString()))
                .thenReturn(testTransaccionesList);
        
        // Realizar la llamada al controlador
        ResponseEntity<List<Transacciones>> response = recurringTransactionController
                .processRecurringTransactions(authentication);
        
        // Verificar los resultados
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals("Netflix Subscription", response.getBody().get(0).getMotivo());
        assertEquals(15.99, response.getBody().get(0).getValor());
        
        // Verificar que el método del servicio fue llamado con los parámetros correctos
        verify(recurringTransactionService, times(1))
                .processRecurringTransactions(eq("test@example.com"));
    }
}
