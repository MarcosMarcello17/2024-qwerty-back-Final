package api.back;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/suscripciones")
@CrossOrigin(origins = { "http://localhost:5173/", "http://127.0.0.1:5173", "https://2024-qwerty-front-final.vercel.app/"})
public class SuscripcionesController {

    @Autowired
    private static SuscripcionesSerivce suscripcionesSerivce;

    @PostMapping("/crearSuscripcion")
    public Suscripciones createSuscripcion(Authentication authentication, @RequestBody Suscripciones suscripcion) {
        String email = authentication.getName();
        return suscripcionesSerivce.createSuscripcion(suscripcion, email);
    }
    
}
