package api.back;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public class DeleteUserTests {

    @Mock
    private UserService userService;
    
    @Mock
    private BudgetService budgetService;
    
    @Mock
    private TransaccionesService transaccionesService;
    
    @Mock
    private PersonalCategoriaService personalCategoriaService;
    
    @Mock
    private TransaccionesPendientesService transaccionesPendientesService;
    
    @Mock
    private PersonalTipoGastoService personalTipoGastoService;
    
    @Mock
    private PasswordResetTokenService passwordResetTokenService;
    
    @Mock
    private Authentication authentication;
    
    @InjectMocks
    private AuthController authController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    public void testDeleteUserSuccess() {
        // Setup
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        
        // Mock authentication
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(mockUser);
        
        // Mock empty lists to avoid NPEs
        List<Budget> emptyBudgets = new ArrayList<>();
        when(budgetService.getPresupuestosByUserId(mockUser)).thenReturn(emptyBudgets);
        
        List<Transacciones> emptyTransactions = new ArrayList<>();
        when(transaccionesService.getTransaccionesByUserId(mockUser.getId())).thenReturn(emptyTransactions);
        
        List<TransaccionesPendientes> emptyPendingTransactions = new ArrayList<>();
        when(transaccionesPendientesService.getPendingTransaccionesByUserId(mockUser.getId())).thenReturn(emptyPendingTransactions);
        
        List<PersonalCategoria> emptyCategories = new ArrayList<>();
        when(personalCategoriaService.getPersonalCategoria(mockUser.getEmail())).thenReturn(emptyCategories);
        
        List<PersonalTipoGasto> emptyTipos = new ArrayList<>();
        when(personalTipoGastoService.getPersonalTipoGastos(mockUser.getEmail())).thenReturn(emptyTipos);
        
        List<PasswordResetToken> emptyTokens = new ArrayList<>();
        when(passwordResetTokenService.getTokensByUser(mockUser)).thenReturn(emptyTokens);
        
        // Execute
        ResponseEntity<Void> response = authController.deleteUser(authentication);
        
        // Verify
        assertEquals(204, response.getStatusCode().value());
        verify(userService).deleteUser(mockUser);
    }
    
    @Test
    public void testDeleteUserWithTransactions() {
        // Setup
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        
        // Mock authentication
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(mockUser);
        
        // Mock empty budgets
        List<Budget> emptyBudgets = new ArrayList<>();
        when(budgetService.getPresupuestosByUserId(mockUser)).thenReturn(emptyBudgets);
        
        // Mock transactions
        List<Transacciones> transactions = new ArrayList<>();
        Transacciones transaction = new Transacciones();
        transaction.setId(1L);
        transactions.add(transaction);
        when(transaccionesService.getTransaccionesByUserId(mockUser.getId())).thenReturn(transactions);
        
        // Mock empty pending transactions
        List<TransaccionesPendientes> emptyPendingTransactions = new ArrayList<>();
        when(transaccionesPendientesService.getPendingTransaccionesByUserId(mockUser.getId())).thenReturn(emptyPendingTransactions);
        
        // Mock empty lists for the rest
        when(personalCategoriaService.getPersonalCategoria(mockUser.getEmail())).thenReturn(new ArrayList<>());
        when(personalTipoGastoService.getPersonalTipoGastos(mockUser.getEmail())).thenReturn(new ArrayList<>());
        when(passwordResetTokenService.getTokensByUser(mockUser)).thenReturn(new ArrayList<>());
        
        // Execute
        ResponseEntity<Void> response = authController.deleteUser(authentication);
        
        // Verify
        assertEquals(204, response.getStatusCode().value());
        verify(transaccionesService).deleteTransaccion(transaction.getId(), mockUser.getEmail());
        verify(userService).deleteUser(mockUser);
    }
    
    @Test
    public void testDeleteUserWithTransactionException() {
        // Setup
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        
        // Mock authentication
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(mockUser);
        
        // Mock empty budgets
        when(budgetService.getPresupuestosByUserId(mockUser)).thenReturn(new ArrayList<>());
        
        // Mock empty pending transactions
        List<TransaccionesPendientes> emptyPendingTransactions = new ArrayList<>();
        when(transaccionesPendientesService.getPendingTransaccionesByUserId(mockUser.getId())).thenReturn(emptyPendingTransactions);
        
        // Mock transactions with exception
        List<Transacciones> transactions = new ArrayList<>();
        Transacciones transaction = new Transacciones();
        transaction.setId(1L);
        transactions.add(transaction);
        when(transaccionesService.getTransaccionesByUserId(mockUser.getId())).thenReturn(transactions);
        doThrow(new TransaccionNotFoundException("Transaction not found"))
            .when(transaccionesService).deleteTransaccion(transaction.getId(), mockUser.getEmail());
        
        // Execute
        ResponseEntity<Void> response = authController.deleteUser(authentication);
        
        // Verify
        assertEquals(404, response.getStatusCode().value());
        verify(transaccionesService).deleteTransaccion(transaction.getId(), mockUser.getEmail());
        verify(userService, never()).deleteUser(mockUser);
    }
}
