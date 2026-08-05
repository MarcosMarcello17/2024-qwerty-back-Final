package api.back.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import api.back.model.RecurringTransaction;
import api.back.model.User;

import java.util.List;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {
    List<RecurringTransaction> findByUser(User user);
}
