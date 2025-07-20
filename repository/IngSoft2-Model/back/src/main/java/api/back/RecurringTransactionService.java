package api.back;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RecurringTransactionService {
    @Autowired
    private RecurringTransactionRepository recurringTransactionRepository;
    @Autowired
    private UserRepository userRepository;

    public RecurringTransaction createRecurringTransaction(RecurringTransaction rt, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        rt.setUser(user);
        return recurringTransactionRepository.save(rt);
    }

    public List<RecurringTransaction> getRecurringTransactions(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return recurringTransactionRepository.findByUser(user);
    }

    public void deleteRecurringTransaction(Long id, String email) {
        RecurringTransaction rt = recurringTransactionRepository.findById(id).orElseThrow(() -> new RuntimeException("No encontrada"));
        if (!rt.getUser().getEmail().equals(email)) throw new RuntimeException("No autorizado");
        recurringTransactionRepository.deleteById(id);
    }
}
