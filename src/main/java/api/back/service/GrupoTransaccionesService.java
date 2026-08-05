package api.back.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import api.back.model.GrupoTransacciones;
import api.back.repository.GrupoTransaccionesRepository;

@Service
public class GrupoTransaccionesService {

    @Autowired
    private GrupoTransaccionesRepository grupoTransaccionesRepository;

    public GrupoTransacciones save(GrupoTransacciones grupoTransacciones) {
        return grupoTransaccionesRepository.save(grupoTransacciones);
    }

    public GrupoTransacciones findById(Long id) {
        Optional<GrupoTransacciones> transaccion = grupoTransaccionesRepository.findById(id);
        return transaccion.orElse(null); // Retorna la transacción si existe, o null si no
    }

    public void delete(Long id) {
        grupoTransaccionesRepository.deleteById(id);
    }
}
