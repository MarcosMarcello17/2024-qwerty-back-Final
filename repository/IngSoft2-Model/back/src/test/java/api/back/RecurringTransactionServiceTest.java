package api.back;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class RecurringTransactionServiceTest {

    @Mock
    private RecurringTransactionRepository recurringTransactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransaccionesService transaccionesService;

    @InjectMocks
    private RecurringTransactionService recurringTransactionService;

    private User testUser;
    private RecurringTransaction testRecurringTransaction;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testRecurringTransaction = new RecurringTransaction();
        testRecurringTransaction.setId(1L);
        testRecurringTransaction.setMotivo("Netflix Subscription");
        testRecurringTransaction.setValor(15.99);
        testRecurringTransaction.setCategoria("Entretenimiento");
        testRecurringTransaction.setTipoGasto("Tarjeta de credito");
        testRecurringTransaction.setFrecuencia("mensual");
        testRecurringTransaction.setNextExecution(LocalDate.now().minusDays(1)); // Vencida
        testRecurringTransaction.setUser(testUser);
    }

    @Test
    void test_processRecurringTransactions_createsTransactionWhenDue() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(recurringTransactionRepository.findByUser(testUser))
            .thenReturn(Arrays.asList(testRecurringTransaction));
        when(transaccionesService.getTransaccionesByUserId(testUser.getId()))
            .thenReturn(Arrays.asList()); // No hay transacciones existentes
        
        Transacciones newTransaction = new Transacciones();
        newTransaction.setId(1L);
        when(transaccionesService.createTransaccion(any(Transacciones.class), anyString()))
            .thenReturn(newTransaction);

        // Act
        List<Transacciones> result = recurringTransactionService.processRecurringTransactions("test@example.com");

        // Assert
        assertEquals(1, result.size());
        verify(transaccionesService, times(1)).createTransaccion(any(Transacciones.class), anyString());
        verify(recurringTransactionRepository, times(1)).save(testRecurringTransaction);
        assertTrue(testRecurringTransaction.getNextExecution().isAfter(LocalDate.now()));
    }

    @Test
    void test_processRecurringTransactions_doesNotCreateWhenTransactionExistsForCurrentMonth() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(recurringTransactionRepository.findByUser(testUser))
            .thenReturn(Arrays.asList(testRecurringTransaction));
        
        // Simular que ya existe una transacción para este mes
        Transacciones existingTransaction = new Transacciones();
        existingTransaction.setMotivo("Netflix Subscription");
        existingTransaction.setCategoria("Entretenimiento");
        existingTransaction.setValor(15.99);
        existingTransaction.setFecha(LocalDate.now());
        
        when(transaccionesService.getTransaccionesByUserId(testUser.getId()))
            .thenReturn(Arrays.asList(existingTransaction));

        // Act
        List<Transacciones> result = recurringTransactionService.processRecurringTransactions("test@example.com");

        // Assert
        assertEquals(0, result.size());
        verify(transaccionesService, never()).createTransaccion(any(Transacciones.class), anyString());
    }

    @Test
    void test_processRecurringTransactions_doesNotCreateWhenNotDue() {
        // Arrange
        testRecurringTransaction.setNextExecution(LocalDate.now().plusDays(5)); // Futuro
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(recurringTransactionRepository.findByUser(testUser))
            .thenReturn(Arrays.asList(testRecurringTransaction));

        // Act
        List<Transacciones> result = recurringTransactionService.processRecurringTransactions("test@example.com");

        // Assert
        assertEquals(0, result.size());
        verify(transaccionesService, never()).createTransaccion(any(Transacciones.class), anyString());
    }

    @Test
    void test_createRecurringTransaction_setsNextExecutionWhenNull() {
        // Arrange
        testRecurringTransaction.setNextExecution(null);
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(recurringTransactionRepository.save(any(RecurringTransaction.class)))
            .thenReturn(testRecurringTransaction);

        // Act
        RecurringTransaction result = recurringTransactionService.createRecurringTransaction(
            testRecurringTransaction, "test@example.com");

        // Assert
        assertNotNull(result.getNextExecution());
        assertTrue(result.getNextExecution().isAfter(LocalDate.now()));
        verify(recurringTransactionRepository, times(1)).save(testRecurringTransaction);
    }
}
