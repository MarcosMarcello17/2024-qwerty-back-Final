package api.back;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transacciones")
@CrossOrigin(origins = { "http://localhost:5173/", "http://127.0.0.1:5173", "https://2024-qwerty-front-final.vercel.app/"})
public class TransaccionesController {

    private final TransaccionesService transaccionesService;
    private final UserService userService;
    private final TransaccionesPendientesService transaccionesPendientesService;

    @Autowired
    private AutomationService automationService;

    @Autowired
    private JwtUtil jwtUtil;

    public TransaccionesController(TransaccionesService transaccionesService, UserService userService,
            TransaccionesPendientesService transaccionesPendientesService) {
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
    public boolean checkUserValidToken(Authentication authentication,
            @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        boolean valid = !jwtUtil.isTokenExpired(token); // validamos si el token no esta vencido
        return valid;
    }

    @PostMapping
    public Transacciones createTransaccion(@RequestBody Transacciones transaccion, Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        user.setTransaccionesCreadas(user.getTransaccionesCreadas() + 1);
        return transaccionesService.createTransaccion(transaccion, email);
    }

    @PostMapping("/con-distribucion")
    public ResponseEntity<?> createTransaccionConDistribucion(@RequestBody TransaccionConDistribucionRequest request, 
                                                             Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userService.findByEmail(email);
            
            // Crear la transacción original (ingreso)
            Transacciones transaccionOriginal = new Transacciones();
            transaccionOriginal.setValor(request.getValor());
            transaccionOriginal.setMotivo(request.getMotivo());
            transaccionOriginal.setFecha(request.getFecha());
            transaccionOriginal.setCategoria(request.getCategoria());
            transaccionOriginal.setTipoGasto(request.getTipoGasto());
            
            user.setTransaccionesCreadas(user.getTransaccionesCreadas() + 1);
            Transacciones transaccionCreada = transaccionesService.createTransaccion(transaccionOriginal, email);
            
            java.util.List<Transacciones> transaccionesDistribucion = new java.util.ArrayList<>();
            
            // Si es ingreso de dinero y se solicita distribución automática
            if ("Ingreso de Dinero".equals(request.getCategoria()) && request.isDistribuirAutomaticamente()) {
                transaccionesDistribucion = automationService.distribuirIngresoAutomaticamente(
                    request.getValor(),
                    request.getFecha(),
                    email,
                    request.getMotivo()
                );
            }
            
            return ResponseEntity.ok().body(java.util.Map.of(
                "success", true,
                "transaccionOriginal", transaccionCreada,
                "transaccionesDistribucion", transaccionesDistribucion,
                "totalDistribuidas", transaccionesDistribucion.size()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "success", false,
                "error", "Error al crear transacción: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/crearPago/{mail}")
    public Transacciones createPago(@PathVariable String mail, @RequestBody Transacciones transaccion,
            Authentication authentication) {
        String email = authentication.getName();
        Transacciones transaccion2 = new Transacciones(); // Ingreso para quien envia el cobro
        transaccion2.setCategoria("Ingreso de Dinero");
        transaccion2.setFecha(transaccion.getFecha());
        transaccion2.setMotivo(transaccion.getMotivo());
        transaccion2.setTipoGasto("Tarjeta de Debito");
        transaccion2.setUser(userService.findByEmail(mail));
        transaccion2.setValor(transaccion.getValor());
        transaccionesService.createTransaccion(transaccion2, mail);
        TransaccionesPendientes cobroPendiente = new TransaccionesPendientes(transaccion.getValor(),
                userService.findByEmail(mail), transaccion.getMotivo(), "Pago", transaccion.getFecha());
        transaccionesPendientesService.save(cobroPendiente);
        // CREAR TRANSACCION PENDIENTE PARA TRANSACCION2
        return transaccionesService.createTransaccion(transaccion, email); // Transaccion de quien acepta el cobro
    }

    @PostMapping("/enviarPago/{mail}")
    public Transacciones sendPago(@PathVariable String mail, @RequestBody Transacciones transaccion,
            Authentication authentication) {
        // mail es el email de quien recibe el cobro
        String email = authentication.getName(); // Email de quien recibe el gasto
        transaccion.setUser(userService.findByEmail(email));
        Transacciones transaccion2 = new Transacciones();
        transaccion2.setCategoria("Ingreso de Dinero");
        transaccion2.setFecha(transaccion.getFecha());
        transaccion2.setMotivo(transaccion.getMotivo());
        transaccion2.setTipoGasto("Tarjeta de Debito");
        transaccion2.setUser(userService.findByEmail(mail));
        transaccion2.setValor(transaccion.getValor());
        transaccionesService.createTransaccion(transaccion2, mail);
        TransaccionesPendientes pendienteCobro = new TransaccionesPendientes(transaccion.getValor(),
                userService.findByEmail(mail), transaccion.getMotivo(), "Pago", transaccion.getFecha());
        pendienteCobro.setSentByEmail(email);
        transaccionesPendientesService.save(pendienteCobro);
        // CREAR TRANSACCION PENDIENTE PARA TRANSACCION2
        return transaccionesService.createTransaccion(transaccion, email); // Transaccion de quien realiza el pago
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

    /*
     * @GetMapping("/user/filter")
     * public List<Transacciones> getTransaccionesByCategory(
     * 
     * @RequestParam(required = false) String categoria,
     * Authentication authentication) {
     * String email = authentication.getName();
     * User user = userService.findByEmail(email);
     * 
     * if (categoria == null || categoria.equals("Todas")) {
     * // Return all transactions for the user
     * return transaccionesService.getTransaccionesByUserId(user.getId());
     * } else {
     * // Filter transactions by category
     * return transaccionesService.getTransaccionesByUserIdAndCategory(user.getId(),
     * categoria);
     * }
     * }
     */
    
    /*/ @GetMapping("/user/filter")
    public List<Transacciones> getTransaccionesByFilters(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer mes,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);

        // Realiza el filtrado en el nivel del servicio
        List<Transacciones> transacciones = transaccionesService.getTransaccionesFiltradas(user.getId(), categoria,
                anio, mes);

        return transacciones;
    }*/

    @GetMapping("/user/filter")
    public TransaccionesResponse getTransaccionesByFilters(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer mes,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);

        // Obtén transacciones filtradas
        List<Transacciones> transaccionesFiltradas = transaccionesService.getTransaccionesFiltradas(user.getId(), categoria, anio, mes);

        // Obtén todas las transacciones sin filtrar
        List<Transacciones> transaccionesSinFiltrarCat = transaccionesService.getTransaccionesFiltradas(user.getId(), "Todas", anio, mes);

        // Retornar ambas listas en el objeto de respuesta personalizado
        return new TransaccionesResponse(transaccionesFiltradas, transaccionesSinFiltrarCat);
    }

    // Clase para el request de transacción con distribución automática
    public static class TransaccionConDistribucionRequest {
        private Double valor;
        private String motivo;
        private java.time.LocalDate fecha;
        private String categoria;
        private String tipoGasto;
        private boolean distribuirAutomaticamente;

        public Double getValor() {
            return valor;
        }

        public void setValor(Double valor) {
            this.valor = valor;
        }

        public String getMotivo() {
            return motivo;
        }

        public void setMotivo(String motivo) {
            this.motivo = motivo;
        }

        public java.time.LocalDate getFecha() {
            return fecha;
        }

        public void setFecha(java.time.LocalDate fecha) {
            this.fecha = fecha;
        }

        public String getCategoria() {
            return categoria;
        }

        public void setCategoria(String categoria) {
            this.categoria = categoria;
        }

        public String getTipoGasto() {
            return tipoGasto;
        }

        public void setTipoGasto(String tipoGasto) {
            this.tipoGasto = tipoGasto;
        }

        public boolean isDistribuirAutomaticamente() {
            return distribuirAutomaticamente;
        }

        public void setDistribuirAutomaticamente(boolean distribuirAutomaticamente) {
            this.distribuirAutomaticamente = distribuirAutomaticamente;
        }
    }

}