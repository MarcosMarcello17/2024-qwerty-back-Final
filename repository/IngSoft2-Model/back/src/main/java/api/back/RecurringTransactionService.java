package api.back;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Service
public class RecurringTransactionService {
    @Autowired
    private RecurringTransactionRepository recurringTransactionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TransaccionesService transaccionesService;

    public RecurringTransaction createRecurringTransaction(RecurringTransaction rt, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        rt.setUser(user);
        
        // Si no se establece la próxima ejecución, establecerla para el próximo mes
        if (rt.getNextExecution() == null) {
            LocalDate nextMonth = LocalDate.now().plusMonths(1);
            rt.setNextExecution(nextMonth);
        }
        
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

    public List<Transacciones> processRecurringTransactions(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        List<RecurringTransaction> recurringTransactions = recurringTransactionRepository.findByUser(user);
        List<Transacciones> createdTransactions = new ArrayList<>();
        
        LocalDate today = LocalDate.now();
        
        for (RecurringTransaction rt : recurringTransactions) {
            if (rt.getNextExecution() != null && !rt.getNextExecution().isAfter(today)) {
                // Verificar si ya existe una transacción para este mes
                if (!hasTransactionForCurrentMonth(rt, user)) {
                    // Crear nueva transacción
                    Transacciones nuevaTransaccion = new Transacciones();
                    nuevaTransaccion.setMotivo(rt.getMotivo());
                    nuevaTransaccion.setValor(rt.getValor());
                    nuevaTransaccion.setCategoria(rt.getCategoria());
                    nuevaTransaccion.setTipoGasto(rt.getTipoGasto());
                    nuevaTransaccion.setFecha(today);
                    
                    Transacciones saved = transaccionesService.createTransaccion(nuevaTransaccion, email);
                    createdTransactions.add(saved);
                    
                    // Actualizar próxima ejecución
                    updateNextExecution(rt);
                }
            }
        }
        
        return createdTransactions;
    }
    
    private boolean hasTransactionForCurrentMonth(RecurringTransaction rt, User user) {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        
        List<Transacciones> existingTransactions = transaccionesService.getTransaccionesByUserId(user.getId());
        
        return existingTransactions.stream().anyMatch(t -> 
            t.getMotivo().equals(rt.getMotivo()) &&
            t.getCategoria().equals(rt.getCategoria()) &&
            t.getValor().equals(rt.getValor()) &&
            t.getFecha().isAfter(startOfMonth.minusDays(1)) &&
            t.getFecha().isBefore(endOfMonth.plusDays(1))
        );
    }
    
    private void updateNextExecution(RecurringTransaction rt) {
        LocalDate nextExecution = rt.getNextExecution();
        if ("MENSUAL".equalsIgnoreCase(rt.getFrecuencia()) || "mensual".equalsIgnoreCase(rt.getFrecuencia())) {
            nextExecution = nextExecution.plusMonths(1);
        } else if ("SEMANAL".equalsIgnoreCase(rt.getFrecuencia()) || "semanal".equalsIgnoreCase(rt.getFrecuencia())) {
            nextExecution = nextExecution.plusWeeks(1);
        }
        rt.setNextExecution(nextExecution);
        recurringTransactionRepository.save(rt);
    }
}
