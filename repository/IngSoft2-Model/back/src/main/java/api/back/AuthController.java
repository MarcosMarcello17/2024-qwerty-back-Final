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
    private final TransaccionesPendientesService transaccionesPendientesService;
    private final UserService userService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final PersonalTipoGastoService personalTipoGastoService;
    private final PersonalCategoriaService personalCategoriaService;
    private final BudgetService budgetService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, JwtUtil jwtUtil,
            UserService userService, TransaccionesService transaccionesService,
            TransaccionesPendientesService transaccionesPendientesService,
            PasswordResetTokenService passwordResetTokenService, PersonalTipoGastoService personalTipoGastoService,
            BudgetService budgetService, PersonalCategoriaService personalCategoriaService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.personalCategoriaService = personalCategoriaService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.transaccionesService = transaccionesService;
        this.transaccionesPendientesService = transaccionesPendientesService;
        this.passwordResetTokenService = passwordResetTokenService;
        this.personalTipoGastoService = personalTipoGastoService;
        this.budgetService = budgetService;
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            
            // 1. Eliminar presupuestos
            try {
                List<Budget> presupuestos = budgetService.getPresupuestosByUserId(user);
                for (Budget presupuesto : presupuestos) {
                    budgetService.deleteBudget(presupuesto.getId());
                }
            } catch (Exception e) {
                System.err.println("Error al eliminar presupuestos: " + e.getMessage());
                // Continuamos con el proceso
            }
            
            // 2. Eliminar transacciones pendientes
            try {
                List<TransaccionesPendientes> transaccionesPendientes = transaccionesPendientesService.getPendingTransaccionesByUserId(user.getId());
                for (TransaccionesPendientes transaccionPendiente : transaccionesPendientes) {
                    transaccionesPendientesService.delete(transaccionPendiente.getId());
                }
            } catch (Exception e) {
                System.err.println("Error al eliminar transacciones pendientes: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            
            // 3. Eliminar transacciones
            try {
                List<Transacciones> transacciones = transaccionesService.getTransaccionesByUserId(user.getId());
                for (Transacciones transaction : transacciones) {
                    transaccionesService.deleteTransaccion(transaction.getId(), user.getEmail());
                }
            } catch (TransaccionNotFoundException e) {
                System.err.println("Error al eliminar transacciones: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            // 4. Actualizar categorías personales y eliminarlas
            try {
                List<PersonalCategoria> categorias = personalCategoriaService.getPersonalCategoria(user.getEmail());
                for (PersonalCategoria categoria : categorias) {
                    try {
                        List<Transacciones> transaccionesUser = transaccionesService.getTransaccionesByUserId(user.getId());
                        for (Transacciones transaccion : transaccionesUser) {
                            if (transaccion.getCategoria() != null && transaccion.getCategoria().equals(categoria.getNombre())) {
                                transaccion.setCategoria("Otros");
                                transaccionesService.saveTransaccion(transaccion);
                            }
                        }
                        personalCategoriaService.findAndDeleteCategoria(user.getEmail(), categoria.getNombre(),
                                categoria.getIconPath());
                    } catch (Exception e) {
                        System.err.println("Error al procesar categoría " + categoria.getNombre() + ": " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error al eliminar categorías: " + e.getMessage());
                // Continuamos con el proceso
            }
            
            // 5. Eliminar tipos de gasto personales
            try {
                List<PersonalTipoGasto> personalTipoGastos = personalTipoGastoService
                        .getPersonalTipoGastos(user.getEmail());
                for (PersonalTipoGasto tipoGasto : personalTipoGastos) {
                    personalTipoGastoService.deletePersonalTipoGasto(tipoGasto.getId());
                }
            } catch (Exception e) {
                System.err.println("Error al eliminar tipos de gasto: " + e.getMessage());
                // Continuamos con el proceso
            }
            
            // 6. Eliminar tokens de reinicio de contraseña
            try {
                List<PasswordResetToken> tokens = passwordResetTokenService.getTokensByUser(user);
                for (PasswordResetToken token : tokens) {
                    passwordResetTokenService.deleteToken(token.getId());
                }
            } catch (Exception e) {
                System.err.println("Error al eliminar tokens: " + e.getMessage());
                // Continuamos con el proceso
            }
            
            // 7. Finalmente, eliminar el usuario
            userService.deleteUser(user);

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
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
