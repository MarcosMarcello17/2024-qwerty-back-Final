package api.back;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = { "http://localhost:5173/", "http://127.0.0.1:5173", "https://2024-qwerty-front-final.vercel.app/"})
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final TransaccionesService transaccionesService;
    private final UserService userService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final PersonalTipoGastoService personalTipoGastoService;
    private final PersonalCategoriaService personalCategoriaService;
    private final BudgetService budgetService;
    private final TransaccionesController transaccionesController;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, JwtUtil jwtUtil,
            UserService userService, TransaccionesService transaccionesService,
            PasswordResetTokenService passwordResetTokenService, PersonalTipoGastoService personalTipoGastoService,
            BudgetService budgetService, PersonalCategoriaService personalCategoriaService,
            TransaccionesController transaccionesController) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.personalCategoriaService = personalCategoriaService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.transaccionesController = transaccionesController;
        this.userService = userService;
        this.transaccionesService = transaccionesService;
        this.passwordResetTokenService = passwordResetTokenService;
        this.personalTipoGastoService = personalTipoGastoService;
        this.budgetService = budgetService;
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            List<Budget> presupuestos = budgetService.getPresupuestosByUserId(user);
            for (Budget presupuesto : presupuestos) {
                budgetService.deleteBudget(presupuesto.getId());
            }
            List<Transacciones> transacciones = transaccionesService.getTransaccionesByUserId(user.getId());
            for (Transacciones transaction : transacciones) {
                transaccionesService.deleteTransaccion(transaction.getId(), user.getEmail());
            }
            List<PersonalCategoria> categorias = personalCategoriaService.getPersonalCategoria(user.getEmail());
            for (PersonalCategoria categoria : categorias) {
                List<Transacciones> transaccionesUser = transaccionesController.getTransaccionesByUser(authentication);
                for (Transacciones transaccion : transaccionesUser) {
                    if (transaccion.getCategoria().equals(categoria.getNombre())) {
                        transaccion.setCategoria("Otros");
                        transaccionesController.updateTransaccion(transaccion.getId(), transaccion, authentication);
                    }
                }
                personalCategoriaService.findAndDeleteCategoria(user.getEmail(), categoria.getNombre(),
                        categoria.getIconPath());
            }
            List<PersonalTipoGasto> personalTipoGastos = personalTipoGastoService
                    .getPersonalTipoGastos(user.getEmail());
            for (PersonalTipoGasto tipoGasto : personalTipoGastos) {
                personalTipoGastoService.deletePersonalTipoGasto(tipoGasto.getId());
            }
            List<PasswordResetToken> tokens = passwordResetTokenService.getTokensByUser(user);
            for (PasswordResetToken token : tokens) {
                passwordResetTokenService.deleteToken(token.getId());
            }
            userService.deleteUser(user);

            return ResponseEntity.noContent().build();
        } catch (TransaccionNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        Optional<User> optionalUser = userRepository.findByEmail(user.getEmail());
        if (optionalUser.isPresent()) {
            return new ResponseEntity<>("El e-mail ya fue utilizado. Intente iniciar sesion",
                    HttpStatus.CONFLICT);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            return jwtUtil.generateToken(email);
        } catch (AuthenticationException e) {
            System.out.println("Error during authentication: " + e.getMessage());
            throw new RuntimeException("Login failed: Invalid email or password");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        userService.initiatePasswordReset(email);
        return ResponseEntity.ok("Correo de restablecimiento enviado.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        boolean success = userService.resetPassword(token, newPassword);
        if (success) {
            return ResponseEntity.ok("Contraseña restablecida exitosamente.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token expirado o no válido.");
        }
    }

}
