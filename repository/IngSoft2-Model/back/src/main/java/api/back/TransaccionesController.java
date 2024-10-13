package api.back;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/transacciones")
@CrossOrigin(origins = { "http://localhost:5173/", "http://127.0.0.1:5173" })
public class TransaccionesController {

    private final TransaccionesService transaccionesService;
    private final UserService userService;
    private final TransaccionesPendientesService transaccionesPendientesService;

    @Autowired
    private JwtUtil jwtUtil;

    public TransaccionesController(TransaccionesService transaccionesService, UserService userService, TransaccionesPendientesService transaccionesPendientesService) {
        this.transaccionesService = transaccionesService;
        this.userService = userService;
        this.transaccionesPendientesService = transaccionesPendientesService;
    }

    @GetMapping("/user")
    public List<Transacciones> getTransaccionesByUser(Authentication authentication) {
        String email = authentication.getName(); // Obtenemos el email del usuario autenticado
        User user = userService.findByEmail(email); // Obtenemos el usuario por email
        return transaccionesService.getTransaccionesByUserId(user.getId()); // Llamamos al servicio con el ID del
                                                                            // usuario
    }
    @GetMapping("/userTest")
    public boolean checkUserValidToken(Authentication authentication, @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        boolean valid = !jwtUtil.isTokenExpired(token); // validamos si el token no esta vencido
        return valid; 
    }

    @PostMapping
    public Transacciones createTransaccion(@RequestBody Transacciones transaccion, Authentication authentication) {
        String email = authentication.getName();
        return transaccionesService.createTransaccion(transaccion, email);
    }

    @GetMapping("/{id}")
    public Optional<Transacciones> getTransaccionById(@PathVariable Long id) {
        return transaccionesService.getTransaccionById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaccion(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName(); // Obtener el email del usuario autenticado
            transaccionesService.deleteTransaccion(id, email);
            return ResponseEntity.noContent().build();
        } catch (TransaccionNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public Transacciones updateTransaccion(@PathVariable Long id, @RequestBody Transacciones transaccionActualizada,
            Authentication authentication) {
        String email = authentication.getName(); // Obtenemos el email del usuario autenticado
        return transaccionesService.updateTransaccion(id, transaccionActualizada, email);
    }

    /*@GetMapping("/user/filter")
    public List<Transacciones> getTransaccionesByCategory(
            @RequestParam(required = false) String categoria,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);

        if (categoria == null || categoria.equals("Todas")) {
            // Return all transactions for the user
            return transaccionesService.getTransaccionesByUserId(user.getId());
        } else {
            // Filter transactions by category
            return transaccionesService.getTransaccionesByUserIdAndCategory(user.getId(), categoria);
        }
    }*/
    @GetMapping("/user/filter")
    public List<Transacciones> getTransaccionesByFilters(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer mes,
            Authentication authentication) {
                System.out.println(categoria + "///" + anio + "/////" + mes +"??????????");
        String email = authentication.getName();
        User user = userService.findByEmail(email);

        // Realiza el filtrado en el nivel del servicio
        List<Transacciones> transacciones = transaccionesService.getTransaccionesFiltradas(user.getId(), categoria, anio, mes);
        System.out.println(user.getId() + "       este es el id");
        return transacciones;
    }

    @PostMapping("/cobro")
    public ResponseEntity<TransaccionesPendientes> generarCobro(
            @RequestBody Map<String, Object> payload,
            Authentication authentication) {

        String emailCobrador = authentication.getName(); // Email del usuario autenticado
        String emailDestinatario = (String) payload.get("emailDestinatario");
        Double valor = Double.valueOf(payload.get("valor").toString());
        String motivo = (String) payload.get("motivo");

        // Buscar el usuario destinatario por email
        User destinatario = userService.findByEmail(emailDestinatario);
        if (destinatario == null) {
            return ResponseEntity.badRequest().body(null); // Si no existe el destinatario, devuelve un error.
        }

        // Crear la transacción pendiente
        TransaccionesPendientes nuevaTransaccion = new TransaccionesPendientes(
                valor,
                destinatario,
                motivo,
                null,
                emailCobrador, // Email del cobrador (quien está autenticado)
                LocalDate.now() // Fecha actual
        );

        // Guardar la transacción pendiente
        TransaccionesPendientes transaccionGuardada = transaccionesPendientesService.createTransaccionPendiente(nuevaTransaccion);

        return ResponseEntity.ok(transaccionGuardada); // Devuelve la transacción creada
    }



}