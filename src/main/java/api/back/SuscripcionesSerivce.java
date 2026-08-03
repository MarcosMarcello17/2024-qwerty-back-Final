package api.back;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SuscripcionesSerivce {
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
