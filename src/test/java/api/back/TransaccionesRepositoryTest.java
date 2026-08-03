package api.back;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Asegura que se use el perfil 'test'
public class TransaccionesRepositoryTest {

    @Autowired
    private TransaccionesRepository transaccionesRepository;

    @Test
    public void testGuardarTransaccion() {
        // Crear una nueva entidad Transacciones
        Transacciones transaccion = new Transacciones(500.00, "Compras", LocalDate.now(), "Impuestos", null);

        // Guardar la entidad en la base de datos
        Transacciones transaccionGuardada = transaccionesRepository.save(transaccion);

        // Verificar que se ha guardado correctamente y tiene un ID asignado
        assertThat(transaccionGuardada.getId()).isNotNull();
        assertThat(transaccionGuardada.getValor()).isEqualTo(500);
        assertThat(transaccionGuardada.getMotivo()).isEqualTo("Compras");
    }

    @Test
    public void testEncontrarTransaccionPorId() {
        // Crear y guardar una entidad Transacciones
        Transacciones transaccion = new Transacciones(500.00, "Compras", LocalDate.now(), "Impuestos", null);
        Transacciones transaccionGuardada = transaccionesRepository.save(transaccion);

        // Buscar la entidad por ID
        Optional<Transacciones> transaccionEncontrada = transaccionesRepository.findById(transaccionGuardada.getId());

        // Verificar que la entidad encontrada coincide con la guardada
        assertThat(transaccionEncontrada).isPresent();
        assertThat(transaccionEncontrada.get().getValor()).isEqualTo(500);
        assertThat(transaccionEncontrada.get().getMotivo()).isEqualTo("Compras");
    }

    @Test
    public void testEliminarTransaccion() {
        // Crear y guardar una entidad Transacciones
        Transacciones transaccion = new Transacciones(500.00, "Compras", LocalDate.now(), "Impuestos", null);
        Transacciones transaccionGuardada = transaccionesRepository.save(transaccion);

        // Eliminar la entidad
        transaccionesRepository.deleteById(transaccionGuardada.getId());

        // Verificar que la entidad ha sido eliminada
        Optional<Transacciones> transaccionEliminada = transaccionesRepository.findById(transaccionGuardada.getId());
        assertThat(transaccionEliminada).isNotPresent();
    }
}