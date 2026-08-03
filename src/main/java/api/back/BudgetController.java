package api.back;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@RestController
@RequestMapping("/api/presupuesto")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<Void> postNuevoPresupuesto(@RequestBody Budget presupuesto,
            Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        presupuesto.setUser(user);
        budgetService.save(presupuesto);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Budget>> getUserPresupuestos(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        List<Budget> presupuestos = budgetService.getPresupuestosByUserId(user);
        return ResponseEntity.ok().body(presupuestos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/editPresupuesto")
    public ResponseEntity<Void> editBudget(@RequestBody Budget budget, Authentication authentication) {
        Long budgetId = budget.getId();
        budgetService.updateBudget(budgetId, budget);

        // Devolver un 200 OK con el presupuesto actualizado
        return ResponseEntity.ok().build();
    }

}
