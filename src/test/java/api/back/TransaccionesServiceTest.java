package api.back;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class TransaccionesServiceTest {

    // Retrieve transactions by user ID
    @Test
    public void test_get_transacciones_by_user_id() {
        TransaccionesRepository transaccionesRepository = mock(TransaccionesRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = mock(UserService.class);
        TransaccionesService transaccionesService = new TransaccionesService(transaccionesRepository, userRepository,
                userService);

        Long userId = 1L;
        List<Transacciones> expectedTransacciones = List.of(new Transacciones(), new Transacciones());
        when(transaccionesRepository.findByUserIdOrderByFechaDesc(userId)).thenReturn(expectedTransacciones);

        List<Transacciones> actualTransacciones = transaccionesService.getTransaccionesByUserId(userId);

        assertEquals(expectedTransacciones, actualTransacciones);
        verify(transaccionesRepository, times(1)).findByUserIdOrderByFechaDesc(userId);
    }

    // Create a transaction with a null date
    @Test
    public void test_create_transaccion_with_null_date() {
        TransaccionesRepository transaccionesRepository = mock(TransaccionesRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = mock(UserService.class);
        TransaccionesService transaccionesService = new TransaccionesService(transaccionesRepository, userRepository,
                userService);

        String email = "test@example.com";
        api.back.User user = new api.back.User();
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Transacciones transaccion = new Transacciones();
        transaccion.setFecha(null);

        when(transaccionesRepository.save(any(Transacciones.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transacciones createdTransaccion = transaccionesService.createTransaccion(transaccion, email);

        assertNotNull(createdTransaccion.getFecha());
        assertEquals(user, createdTransaccion.getUser());
        verify(transaccionesRepository, times(1)).save(transaccion);
    }

    // Successfully deletes a transaction when valid ID and email are provided
    @Test
    public void test_delete_transaccion_success() {
        // Arrange
        Long id = 1L;
        String email = "test@example.com";
        Transacciones transaccion = new Transacciones();
        api.back.User user = new api.back.User();
        user.setEmail(email);
        transaccion.setId(id);
        transaccion.setUser(user);

        TransaccionesRepository transaccionesRepository = mock(TransaccionesRepository.class);
        when(transaccionesRepository.findByIdAndUserEmail(id, email)).thenReturn(Optional.of(transaccion));

        TransaccionesService transaccionesService = new TransaccionesService(transaccionesRepository, null, null);

        // Act
        transaccionesService.deleteTransaccion(id, email);

        // Assert
        verify(transaccionesRepository, times(1)).delete(transaccion);
    }
}