package api.back.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import api.back.model.Suscripciones;
import api.back.model.User;
import api.back.repository.SuscripcionesRepository;
import api.back.repository.UserRepository;

@Service
public class SuscripcionesService {
    @Autowired
    private static UserRepository userRepository;

    @Autowired
    private static SuscripcionesRepository SuscripcionesRepository;

    public Suscripciones createSuscripcion(Suscripciones suscripcion, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        suscripcion.setUser(user);
        
        return SuscripcionesRepository.save(suscripcion);
 
    }
}
