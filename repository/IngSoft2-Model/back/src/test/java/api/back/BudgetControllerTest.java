package api.back;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class BudgetControllerTest {

    @Mock
    private BudgetService budgetService;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BudgetController budgetController;

    private User testUser;
    private Budget testBudget;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Configuración del usuario de prueba
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        // Configuración del presupuesto de prueba
        Map<String, Integer> categoryBudgets = new HashMap<>();
        categoryBudgets.put("Comida", 30000);
        categoryBudgets.put("Transporte", 10000);

        testBudget = new Budget(50000, categoryBudgets, testUser, "Presupuesto Mensual", "2025-07");
        testBudget.setId(1L);

        // Configuración del mock de autenticación
        when(authentication.getName()).thenReturn(testUser.getEmail());
        when(userService.findByEmail(testUser.getEmail())).thenReturn(testUser);
    }

    @Test
    public void testPostNuevoPresupuesto() {
        // Arrange
        Budget newBudget = new Budget();
        newBudget.setNameBudget("Nuevo Presupuesto");
        newBudget.setTotalBudget(60000);
        
        Map<String, Integer> categories = new HashMap<>();
        categories.put("Entretenimiento", 20000);
        categories.put("Servicios", 40000);
        newBudget.setCategoryBudgets(categories);
        newBudget.setBudgetMonth("2025-08");

        doNothing().when(budgetService).save(any(Budget.class));

        // Act
        ResponseEntity<Void> response = budgetController.postNuevoPresupuesto(newBudget, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(budgetService, times(1)).save(any(Budget.class));
        verify(userService, times(1)).findByEmail("test@example.com");
    }

    @Test
    public void testGetUserPresupuestos() {
        // Arrange
        List<Budget> expectedBudgets = Arrays.asList(testBudget);
        when(budgetService.getPresupuestosByUserId(testUser)).thenReturn(expectedBudgets);

        // Act
        ResponseEntity<List<Budget>> response = budgetController.getUserPresupuestos(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedBudgets.size(), response.getBody().size());
        assertEquals(expectedBudgets, response.getBody());
        verify(budgetService, times(1)).getPresupuestosByUserId(testUser);
    }

    @Test
    public void testDeleteBudget() {
        // Arrange
        Long budgetId = 1L;
        doNothing().when(budgetService).deleteBudget(budgetId);

        // Act
        ResponseEntity<Void> response = budgetController.deleteBudget(budgetId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(budgetService, times(1)).deleteBudget(budgetId);
    }

    @Test
    public void testEditBudget() {
        // Arrange
        Long budgetId = 1L;
        Budget updatedBudget = new Budget();
        updatedBudget.setId(budgetId);
        updatedBudget.setNameBudget("Presupuesto Actualizado");
        updatedBudget.setTotalBudget(70000);
        
        Map<String, Integer> updatedCategories = new HashMap<>();
        updatedCategories.put("Comida", 40000);
        updatedCategories.put("Transporte", 15000);
        updatedCategories.put("Otros", 15000);
        updatedBudget.setCategoryBudgets(updatedCategories);
        updatedBudget.setBudgetMonth("2025-09");
        
        when(budgetService.updateBudget(budgetId, updatedBudget)).thenReturn(updatedBudget);

        // Act
        ResponseEntity<Void> response = budgetController.editBudget(updatedBudget, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(budgetService, times(1)).updateBudget(budgetId, updatedBudget);
    }
}
