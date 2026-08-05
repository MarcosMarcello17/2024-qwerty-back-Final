package api.back.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import api.back.model.Budget;
import api.back.model.User;
import api.back.repository.BudgetRepository;

@Service
public class BudgetService {
    @Autowired
    private BudgetRepository budgetRepository;

    public List<Budget> getPresupuestosByUserId(User user) {
        return budgetRepository.findByUser(user);
    }

    public void deleteBudget(Long id) {
        budgetRepository.deleteById(id);
    }

    // ...

    public Budget updateBudget(Long id, Budget budget) {
        Budget existingBudget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada o no pertenece al usuario"));

        // Validar el presupuesto total
        if (budget.getTotalBudget() < 0) {
            throw new IllegalArgumentException("El presupuesto total no puede ser negativo.");
        }

        // Actualizar los campos del presupuesto existente
        existingBudget.setNameBudget(budget.getNameBudget());
        existingBudget.setBudgetMonth(budget.getBudgetMonth());
        existingBudget.setTotalBudget(budget.getTotalBudget());
        existingBudget.setCategoryBudgets(budget.getCategoryBudgets());

        // Guardar los cambios en la base de datos
        return budgetRepository.save(existingBudget);
    }

    public void save(Budget presupuesto) {
        budgetRepository.save(presupuesto);
    }
}
