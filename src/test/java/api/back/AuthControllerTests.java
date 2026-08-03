package api.back;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class AuthControllerTests {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthController authController;
    @Autowired
    private UserService userService;

    @Test
    public void testRegistrarNuevoUsuario() {
        User user = new User();
        user.setEmail("unique@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        ResponseEntity<String> response = authController.register(user);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        userService.deleteUser(user);
    }

    @Test
    public void testRegistrarUsuarioConEmailExistente() {
        User existingUser = new User();
        existingUser.setEmail("used@example.com");
        existingUser.setPassword("password");
        authController.register(existingUser);

        User newUser = new User();
        newUser.setEmail("used@example.com");
        newUser.setPassword("password");
        ResponseEntity<String> response = authController.register(newUser);

        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Assertions.assertEquals("El e-mail ya fue utilizado. Intente iniciar sesion", response.getBody());
    }

    @Test
    public void testCargoUsuarioPorEmail() {
        String email = "test@example.com";
        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setPassword("123456");
        authController.register(mockUser);
        UserDetails userDetails = userService.loadUserByUsername(email);
        Assertions.assertNotNull(userDetails);
        Assertions.assertEquals(email, userDetails.getUsername());
    }

    @Test
    public void testSiBuscoEmailQueNoExisteTiraError() {
        String email = "nonexistent@example.com";
        Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername(email);
        });
    }

    @Test
    public void testUsuarioLogueadoCorrectamente() {
        String email = "valid@example.com";
        String password = "validPassword";
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        AuthController authController = new AuthController(null, null,
                authenticationManager, jwtUtil, null, null,  null, null, null, null, null);

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(jwtUtil.generateToken(email)).thenReturn("mockedToken");

        String token = authController.login(email, password);
        assertEquals("mockedToken", token);
    }

    @Test
    public void testInicioProcesoDeRestablecimientoDeContrasena() {
        String email = "valid@example.com";
        UserService userService = mock(UserService.class);
        AuthController authController = new AuthController(null, null, null, null,
                userService, null, null, null, null, null, null);

        ResponseEntity<String> response = authController.forgotPassword(email);

        verify(userService).initiatePasswordReset(email);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Correo de restablecimiento enviado.", response.getBody());
    }

    // Reset password with valid token and new password returns success message
    @Test
    public void test_reset_password_with_valid_token() {
        UserService userService = Mockito.mock(UserService.class);
        AuthController authController = new AuthController(null, null, null, null,
                userService, null, null, null, null, null, null);

        String validToken = "validToken";
        String newPassword = "newPassword";

        Mockito.when(userService.resetPassword(validToken,
                newPassword)).thenReturn(true);

        ResponseEntity<String> response = authController.resetPassword(validToken,
                newPassword);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Contraseña restablecida exitosamente.",
                response.getBody());
    }

    // Reset password with expired token returns error message
    @Test
    public void test_reset_password_with_expired_token() {
        UserService userService = Mockito.mock(UserService.class);
        AuthController authController = new AuthController(null, null, null, null, userService, null, null, null, null,
                null, null);

        String expiredToken = "expiredToken";
        String newPassword = "newPassword";

        Mockito.when(userService.resetPassword(expiredToken, newPassword)).thenReturn(false);

        ResponseEntity<String> response = authController.resetPassword(expiredToken, newPassword);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Token expirado o no válido.", response.getBody());
    }
}