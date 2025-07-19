package api.back;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TransaccionesPendientesService {

    private final TransaccionesPendientesRepository transaccionesPendientesRepository;

    public TransaccionesPendientesService(TransaccionesPendientesRepository transaccionesPendientesRepository) {
        this.transaccionesPendientesRepository = transaccionesPendientesRepository;
    }

    public TransaccionesPendientes save(TransaccionesPendientes transaccionPendiente) {
        return transaccionesPendientesRepository.save(transaccionPendiente);
    }

    public List<TransaccionesPendientes> getPendingTransaccionesSentByUser(String email){
        return transaccionesPendientesRepository.findBySentByEmail(email);
    }

    public List<TransaccionesPendientes> getPendingTransaccionesByUserId(Long userId) {
        // Retorna todas las transacciones pendientes para el usuario dado
        return transaccionesPendientesRepository.findByUserId(userId);
    }

    public void deletePendingTransaccion(Long id, Long userId) {
        Optional<TransaccionesPendientes> transaccion = transaccionesPendientesRepository.findByIdAndUserId(id, userId);
        if (transaccion.isPresent()) {
            transaccionesPendientesRepository.delete(transaccion.get());
        } else {
            throw new TransaccionNotFoundException("Transacción pendiente no encontrada para el usuario");
        }
    }

    public List<TransaccionesPendientes> findByGrupoId(Long grupoId) {
        return transaccionesPendientesRepository.findByGrupoId(grupoId);
    }

    // Elimina una transacción pendiente por su ID
    public void delete(Long id) {
        transaccionesPendientesRepository.deleteById(id);
    }


}
