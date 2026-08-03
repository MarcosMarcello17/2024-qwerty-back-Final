package api.back;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/recurrents")
@CrossOrigin(origins = { "http://localhost:5173/", "http://127.0.0.1:5173", "https://2024-qwerty-front-final.vercel.app/"})
public class RecurringTransactionController {
    @Autowired
    private RecurringTransactionService recurringTransactionService;

    @PostMapping
    public ResponseEntity<RecurringTransaction> createRecurring(@RequestBody RecurringTransaction rt, Authentication authentication) {
        String email = authentication.getName();
        RecurringTransaction saved = recurringTransactionService.createRecurringTransaction(rt, email);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<RecurringTransaction>> getMyRecurrings(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(recurringTransactionService.getRecurringTransactions(email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecurring(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        recurringTransactionService.deleteRecurringTransaction(id, email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/process")
    public ResponseEntity<List<Transacciones>> processRecurringTransactions(Authentication authentication) {
        String email = authentication.getName();
        List<Transacciones> createdTransactions = recurringTransactionService.processRecurringTransactions(email);
        return ResponseEntity.ok(createdTransactions);
    }
}
