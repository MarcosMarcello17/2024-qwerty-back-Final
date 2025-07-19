package api.back;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate; // Importa LocalDate
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grupos")
@CrossOrigin(origins = { "http://localhost:5173/", "http://127.0.0.1:5173", "https://2024-qwerty-front-final.vercel.app/"})
public class GrupoController {

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private GrupoTransaccionesService grupoTransaccionesService;

    @Autowired
    private TransaccionesService transaccionesService;

    @Autowired
    private TransaccionesPendientesService transaccionesPendientesService; // Asegúrate de tener este servicio inyectado

    @PostMapping("/crear")
    public Grupo crearGrupo(@RequestBody Map<String, Object> payload, Authentication authentication) {
        String nombre = (String) payload.get("nombre");
        List<String> miembrosEmails = (List<String>) payload.get("usuarios");
        String creadorEmail = authentication.getName(); // Email del usuario autenticado

        if (miembrosEmails == null) {
            miembrosEmails = new ArrayList<>(); // Asegúrate de que la lista no sea nula
        }

        // Crea el grupo
        User usuarioCreador = userService.findByEmail(creadorEmail);
        Grupo grupo = grupoService.crearGrupo(nombre, usuarioCreador);

        // Crea una transacción pendiente para cada miembro del grupo
        for (String email : miembrosEmails) {
            User usuario = userService.findByEmail(email);
            LocalDate fechaHoy = LocalDate.now();
            TransaccionesPendientes transaccionPendiente = new TransaccionesPendientes(0.0, usuario, grupo.getNombre(), "Grupo", fechaHoy);
            // Establecer el correo del creador como sentByEmail
            transaccionPendiente.setSentByEmail(creadorEmail);
            transaccionPendiente.setGrupoId(grupo.getId());
            transaccionesPendientesService.save(transaccionPendiente);
        }
        return grupo;
    }

    @GetMapping("/mis-grupos")
    public List<Grupo> obtenerGruposDelUsuario(Authentication authentication) {
        String usuarioEmail = authentication.getName(); // Email del usuario autenticado
        return grupoService.obtenerGruposPorUsuario(usuarioEmail);
    }


    @PostMapping("/agregar-usuario")
    public ResponseEntity<String> agregarUsuarioAGrupo(@RequestBody Map<String, Object> payload, Authentication authentication) {
        Object grupoIdObj = payload.get("grupo_id");
        Long grupoId = null;

        if (grupoIdObj instanceof Integer) {
            grupoId = ((Integer) grupoIdObj).longValue();
        } else if (grupoIdObj instanceof Long) {
            grupoId = (Long) grupoIdObj;
        } else {
            return ResponseEntity.badRequest().body("ID de grupo no válido.");
        }
        String usuarioEmail = authentication.getName(); // Obtener el email del usuario autenticado
        
        // Buscar el usuario autenticado y el grupo por ID
        User usuario = userService.findByEmail(usuarioEmail);
        Grupo grupo = grupoService.findById(grupoId);
        
        // Validar si el grupo existe y si el usuario ya está en el grupo
        if (grupo == null) {
            return ResponseEntity.badRequest().body("Grupo no encontrado.");
        }
        if (grupo.getUsuarios().contains(usuario)) {
            return ResponseEntity.badRequest().body("El usuario ya es miembro del grupo.");
        }
        
        // Agregar el usuario al grupo y guardar los cambios
        grupo.getUsuarios().add(usuario);
        grupoService.save(grupo);
        
        return ResponseEntity.ok("Usuario agregado al grupo exitosamente.");
    }

    @PostMapping("/transaccion")
    public ResponseEntity<GrupoTransacciones> crearGrupoTransaccion(@RequestBody Map<String, Object> payload, Authentication authentication) {
        // Intenta convertir el valor a Double (si es String, convierte)
        Double valor;
        Object valorObj = payload.get("valor");
        if (valorObj instanceof String) {
            valor = Double.parseDouble((String) valorObj);
        } else if (valorObj instanceof Number) {
            valor = ((Number) valorObj).doubleValue();
        } else {
            return ResponseEntity.badRequest().body(null); // Valor no válido
        }
        String usuarioEmail = authentication.getName(); // Obtener el email del usuario autenticado
    
        // Extrae los demás datos del JSON
        String motivo = (String) payload.get("motivo");
        LocalDate fecha = LocalDate.parse((String) payload.get("fecha")); // Asegúrate de que la fecha esté en formato ISO
        String categoria = (String) payload.get("categoria");
        String tipoGasto = (String) payload.get("tipoGasto");
    
        // Convierte el valor de grupo a Long, similar a como hiciste antes
        Long grupoId = ((Number) payload.get("grupo")).longValue(); // Obtén el ID del grupo desde el JSON
    
        // Busca el grupo por ID
        Grupo grupo = grupoService.findById(grupoId);
        if (grupo == null) {
            return ResponseEntity.badRequest().body(null); // Grupo no encontrado
        }
    
        // Crea una nueva transacción grupal
        GrupoTransacciones grupoTransaccion = new GrupoTransacciones(valor, motivo, fecha, categoria, tipoGasto, usuarioEmail);
        grupoTransaccion.setGrupo(grupo); // Establece la relación con el grupo
    
        // Agrega la transacción a la lista de transacciones del grupo
        grupo.getTransacciones().add(grupoTransaccion);
    
        // Guarda la transacción
        GrupoTransacciones nuevaTransaccion = grupoTransaccionesService.save(grupoTransaccion);
    
        return ResponseEntity.ok(nuevaTransaccion); // Devuelve la nueva transacción
    }

    @GetMapping("/{grupoId}/transacciones")
    public ResponseEntity<List<GrupoTransacciones>> obtenerTransaccionesPorGrupo(@PathVariable Long grupoId) {
        // Busca el grupo por su ID
        Grupo grupo = grupoService.findById(grupoId);
        if (grupo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList()); // Retorna 404 si el grupo no existe
        }
        // Obtiene las transacciones asociadas al grupo
        List<GrupoTransacciones> transacciones = grupo.getTransacciones();
        return ResponseEntity.ok(transacciones); // Retorna la lista de transacciones
    }

    @PostMapping("/{grupoId}/cerrar")
    public ResponseEntity<String> cerrarGrupo(@PathVariable Long grupoId) {
        // Buscar el grupo por su ID
        Grupo grupo = grupoService.findById(grupoId);
        if (grupo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Grupo no encontrado.");
        }

        List<TransaccionesPendientes> transaccionesPendientes = transaccionesPendientesService.findByGrupoId(grupoId);
        for (TransaccionesPendientes transaccionPendiente : transaccionesPendientes) {
            transaccionesPendientesService.delete(transaccionPendiente.getId());
        }

        // Sumar todas las transacciones del grupo
        double totalTransacciones = grupo.getTransacciones().stream()
        .mapToDouble(GrupoTransacciones::getValor)
        .sum();

        // Calcular el monto a pagar por cada usuario
        int cantidadUsuarios = grupo.getUsuarios().size();
        double montoPorUsuario = totalTransacciones / cantidadUsuarios;

        // Crear una transacción para cada usuario del grupo
        LocalDate fechaHoy = LocalDate.now();
        String categoria = "Gasto Grupal";
        String motivo = grupo.getNombre();
        String medioDePago = "Efectivo";

        for (User usuario : grupo.getUsuarios()) {
            Transacciones nuevaTransaccion = new Transacciones();
            nuevaTransaccion.setValor(montoPorUsuario);
            nuevaTransaccion.setFecha(fechaHoy);
            nuevaTransaccion.setCategoria(categoria);
            nuevaTransaccion.setMotivo(motivo);
            nuevaTransaccion.setTipoGasto(medioDePago);
            nuevaTransaccion.setUser(usuario);
            
            // Guardar la transacción
            transaccionesService.createTransaccion(nuevaTransaccion, usuario.getEmail());
        }
        // Cambiar el estado del grupo a cerrado (false)
        grupo.setEstado(false);
        grupoService.save(grupo); 
        
        return ResponseEntity.ok("El grupo ha sido cerrado exitosamente.");
    }

    @DeleteMapping("/transaccion/{transaccionId}")
    public ResponseEntity<String> eliminarGrupoTransaccion(@PathVariable Long transaccionId) {
        // Busca la transacción por su ID
        GrupoTransacciones transaccion = grupoTransaccionesService.findById(transaccionId);
        if (transaccion == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transacción no encontrada.");
        }

        // Elimina la transacción
        grupoTransaccionesService.delete(transaccionId);
        return ResponseEntity.ok("Transacción eliminada exitosamente.");
    }

    // Endpoint para editar una transacción grupal
    @PutMapping("/transaccion/{transaccionId}")
    public ResponseEntity<GrupoTransacciones> editarGrupoTransaccion(
        @PathVariable Long transaccionId,
        @RequestBody Map<String, Object> payload
    ) {
        // Busca la transacción por su ID
        GrupoTransacciones transaccionExistente = grupoTransaccionesService.findById(transaccionId);
        if (transaccionExistente == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Retorna 404 si la transacción no existe
        }

        // Actualiza los valores de la transacción
        if (payload.containsKey("valor")) {
            Object valorObj = payload.get("valor");
            Double valor;
            if (valorObj instanceof Number) {
                valor = ((Number) valorObj).doubleValue();
            } else if (valorObj instanceof String) {
                valor = Double.parseDouble((String) valorObj);
            } else {
                throw new IllegalArgumentException("Tipo de dato no válido para el campo 'valor'");
            }
            transaccionExistente.setValor(valor);
        }
        if (payload.containsKey("motivo")) {
            transaccionExistente.setMotivo((String) payload.get("motivo"));
        }
        if (payload.containsKey("fecha")) {
            LocalDate fecha = LocalDate.parse((String) payload.get("fecha"));
            transaccionExistente.setFecha(fecha);
        }
        if (payload.containsKey("categoria")) {
            transaccionExistente.setCategoria((String) payload.get("categoria"));
        }
        if (payload.containsKey("tipoGasto")) {
            transaccionExistente.setTipoGasto((String) payload.get("tipoGasto"));
        }

        // Guarda los cambios
        GrupoTransacciones transaccionActualizada = grupoTransaccionesService.save(transaccionExistente);
        return ResponseEntity.ok(transaccionActualizada);
    }

    @DeleteMapping("/{grupoId}")
    public ResponseEntity<String> eliminarGrupo(@PathVariable Long grupoId) {
        // Busca el grupo por su ID
        Grupo grupo = grupoService.findById(grupoId);
        if (grupo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Grupo no encontrado.");
        }

        // Elimina las transacciones del grupo
        List<GrupoTransacciones> transacciones = grupo.getTransacciones();
        for (GrupoTransacciones transaccion : transacciones) {
            grupoTransaccionesService.delete(transaccion.getId());
        }

        // Elimina las transacciones pendientes del grupo
        List<TransaccionesPendientes> transaccionesPendientes = transaccionesPendientesService.findByGrupoId(grupoId);
        for (TransaccionesPendientes transaccionPendiente : transaccionesPendientes) {
            transaccionesPendientesService.delete(transaccionPendiente.getId());
        }

        // Finalmente, elimina el grupo
        grupoService.delete(grupoId);

        return ResponseEntity.ok("Grupo y todas sus transacciones eliminadas exitosamente.");
    }

    @GetMapping("/{grupoId}/usuarios")
    public ResponseEntity<List<User>> obtenerUsuariosDelGrupo(@PathVariable Long grupoId) {
        Grupo grupo = grupoService.findById(grupoId);
        System.out.println("\n\n " + grupo + "\n\n " + grupoId);
        if (grupo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
        List<User> usuarios = grupo.getUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    // Endpoint para agregar un usuario a un grupo
    @PostMapping("/{grupoId}/agregar-usuario")
    public ResponseEntity<String> agregarUsuariosAGrupo(@PathVariable Long grupoId, @RequestBody Map<String, Object> payload, Authentication authentication) {
        List<String> miembrosEmails = (List<String>) payload.get("usuarios");
        String creadorEmail = authentication.getName(); // Email del usuario autenticado
        if (miembrosEmails == null) {
            miembrosEmails = new ArrayList<>(); // Asegúrate de que la lista no sea nula
        }
        Grupo grupo = grupoService.findById(grupoId);
        // Crea una transacción pendiente para cada miembro del grupo
        for (String email : miembrosEmails) {
            User usuario = userService.findByEmail(email);
            LocalDate fechaHoy = LocalDate.now();
            TransaccionesPendientes transaccionPendiente = new TransaccionesPendientes(0.0, usuario, grupo.getNombre(), "Grupo", fechaHoy);
            // Establecer el correo del creador como sentByEmail
            transaccionPendiente.setSentByEmail(creadorEmail);
            transaccionPendiente.setGrupoId(grupo.getId());
            transaccionesPendientesService.save(transaccionPendiente);
        }
        return null;
        
    }

    @GetMapping("/{grupoId}/verificar-usuario")
    public ResponseEntity<String> verificarUsuarioEnGrupoOInvitado(
            @PathVariable Long grupoId,
            @RequestParam String email) {
        // Busca el grupo por su ID
        Grupo grupo = grupoService.findById(grupoId);
        if (grupo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Grupo no encontrado.");
        }

        // Busca el usuario por su email
        User usuario = userService.findByEmail(email);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }

        // Verifica si el usuario ya es miembro del grupo
        if (grupo.getUsuarios().contains(usuario)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El usuario ya es miembro del grupo.");
        }

        // Verifica si el usuario tiene una transacción pendiente asociada al grupo
        List<TransaccionesPendientes> transaccionesPendientes = transaccionesPendientesService.findByGrupoId(grupoId);
        for (TransaccionesPendientes transaccionPendiente : transaccionesPendientes) {
            if((transaccionPendiente.getUser()).getEmail().equals(email)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El usuario ya tiene una invitación pendiente para el grupo.");
            }
        }

        return ResponseEntity.ok("El usuario no está en el grupo ni tiene una invitación pendiente.");
    }

}
