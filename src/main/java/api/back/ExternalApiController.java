package api.back;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class ExternalApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private TransaccionesPendientesService transaccionesPendientesService;

    // Endpoint para recibir la transacción pendiente en formato JSON
    @PostMapping("/sendTransaccion")
    public ResponseEntity<String> postPendingTransaccion(@RequestBody TransaccionRequest request) {
        User usuario = userService.findByEmail(request.getEmail());
        if (usuario != null) {
            if (request.getValor() >= 0) {
                TransaccionesPendientes transaccionPendiente = new TransaccionesPendientes(
                        request.getValor(),
                        usuario,
                        request.getMotivo(),
                        request.getId_reserva(),
                        request.getFecha() != null ? request.getFecha() : LocalDate.now());
                // Guardar la transacción
                transaccionesPendientesService.save(transaccionPendiente);
                userService.pendingTransactionNotification(usuario.getEmail());
                return ResponseEntity.ok().body("Transacción pendiente registrada correctamente.");
            } else {
                return ResponseEntity.badRequest().body("Error: Valor negativo");
            }
            // Crear una transacción pendiente
        } else {
            return ResponseEntity.badRequest().body("Usuario no registrado.");
        }
    }

    @GetMapping("/exists/{email}")
    public ResponseEntity<Boolean> checkUserExists(@PathVariable String email) {
        boolean exists = (userService.findByEmail(email) != null);
        return ResponseEntity.ok(exists);
    }

}
