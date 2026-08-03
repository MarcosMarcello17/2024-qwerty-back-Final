package api.back;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransaccionesService {

    private final TransaccionesRepository transaccionesRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public TransaccionesService(TransaccionesRepository transaccionesRepository, UserRepository userRepository,
            UserService userService) {
        this.transaccionesRepository = transaccionesRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public List<Transacciones> getTransaccionesByUserId(Long userId) {
        return transaccionesRepository.findByUserIdOrderByFechaDesc(userId);
    }

    public Transacciones createTransaccion(Transacciones transaccion, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        transaccion.setUser(user);

        // Si no se proporciona una fecha, usamos la fecha actual
        if (transaccion.getFecha() == null) {
            transaccion.setFecha(LocalDate.now());
        }

        return transaccionesRepository.save(transaccion);
    }

    public Optional<Transacciones> getTransaccionById(Long id) {
        return transaccionesRepository.findById(id);
    }

    public void deleteTransaccion(Long id, String email) {
        System.out.println("Intentando eliminar transacción con ID: " + id + " para el usuario: " + email);

        Optional<Transacciones> optionalTransaccion = transaccionesRepository.findByIdAndUserEmail(id, email);
        if (optionalTransaccion.isEmpty()) {
            throw new TransaccionNotFoundException("Transacción no encontrada o no pertenece al usuario");
        }

        Transacciones transaccion = optionalTransaccion.get();
        System.out.println("Transacción encontrada: " + transaccion);

        transaccionesRepository.delete(transaccion);
        System.out.println("Transacción eliminada");
    }

    public Transacciones updateTransaccion(Long id, Transacciones transaccionActualizada, String email) {
        // Obtener el usuario autenticado por email
        User user = userService.findByEmail(email);

        // Buscar la transacción por id y asegurarse de que pertenezca al usuario
        Transacciones transaccion = transaccionesRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada o no pertenece al usuario"));

        // Actualizar los campos de la transacción
        transaccion.setMotivo(transaccionActualizada.getMotivo());
        transaccion.setValor(transaccionActualizada.getValor());
        transaccion.setFecha(transaccionActualizada.getFecha());
        transaccion.setCategoria(transaccionActualizada.getCategoria());
        transaccion.setTipoGasto(transaccionActualizada.getTipoGasto());
        // Guardar los cambios en la base de datos
        return transaccionesRepository.save(transaccion);
    }

    public List<Transacciones> getTransaccionesByUserIdAndCategory(Long userId, String categoria) {
        return transaccionesRepository.findByUserIdAndCategoriaOrderByFechaDesc(userId, categoria);
    }

    public List<Transacciones> getTransaccionesFiltradas(Long userId, String categoria, Integer anio, Integer mes) {
        LocalDate startDate = null;
        LocalDate endDate = null;
        
        // Si la categoría no es null o "Todas" y anio y mes son null
        if (categoria != null && !categoria.equals("Todas") && anio == null && mes == null) {
            return transaccionesRepository.findByUserIdAndCategoriaOrderByFechaDesc(userId, categoria);
        }
        // Si la categoría no es null ni "Todas" y además mes o anio no son null
        else if (categoria != null && !categoria.equals("Todas") && (anio != null || mes != null)) {
            if (anio != null && mes != null) {
                // Si ambos son proporcionados, calcula el rango de fechas
                startDate = LocalDate.of(anio, mes, 1);
                endDate = startDate.plusMonths(1).minusDays(1);
                return transaccionesRepository.findByUserIdAndCategoriaAndFechaBetween(userId, categoria, startDate, endDate);
            } else if (anio != null) {
                // Si solo el año es proporcionado, establece el rango de fechas para todo el año
                startDate = LocalDate.of(anio, 1, 1);
                endDate = LocalDate.of(anio, 12, 31);
                return transaccionesRepository.findByUserIdAndCategoriaAndFechaBetween(userId, categoria, startDate, endDate);
            } else if (mes != null) {
                // Si solo el mes es proporcionado, filtrar todas las transacciones por mes (sin importar el año)
                return transaccionesRepository.findByUserIdAndCategoriaOrderByFechaDesc(userId, categoria)
                    .stream()
                    .filter(transaccion -> transaccion.getFecha().getMonthValue() == mes)
                    .collect(Collectors.toList());
            }
        }
        // Si la categoría es null o "Todas" pero anio o mes no son null
        else if ((categoria == null || categoria.equals("Todas")) && (anio != null || mes != null)) {
            if (anio != null && mes != null) {
                // Si ambos son proporcionados, calcula el rango de fechas
                startDate = LocalDate.of(anio, mes, 1);
                endDate = startDate.plusMonths(1).minusDays(1);
                return transaccionesRepository.findByUserIdAndFechaBetween(userId, startDate, endDate);
            } else if (anio != null) {
                // Si solo el año es proporcionado, establece el rango de fechas para todo el año
                startDate = LocalDate.of(anio, 1, 1);
                endDate = LocalDate.of(anio, 12, 31);
                return transaccionesRepository.findByUserIdAndFechaBetween(userId, startDate, endDate);
            } else if (mes != null) {
                // Si solo el mes es proporcionado, filtrar todas las transacciones por mes (sin importar el año)
                return transaccionesRepository.findByUserIdOrderByFechaDesc(userId)
                    .stream()
                    .filter(transaccion -> transaccion.getFecha().getMonthValue() == mes)
                    .collect(Collectors.toList());
            }
        }
        return transaccionesRepository.findByUserIdOrderByFechaDesc(userId);
    }

    public Transacciones findByIdAndUserId(Long id, Long userId) {
        return transaccionesRepository.findByIdAndUserId(id, userId).orElse(null);
    }

    public Transacciones saveTransaccion(Transacciones transaccion) {
        return transaccionesRepository.save(transaccion);
    }
    

}
