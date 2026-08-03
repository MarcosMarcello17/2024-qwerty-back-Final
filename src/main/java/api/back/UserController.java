package api.back;

import java.security.Principal;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest,
            Principal principal) {
        String email = principal.getName();
        boolean success = userService.changePassword(email, changePasswordRequest.getCurrentPassword(),
                changePasswordRequest.getNewPassword());

        if (success) {
            return ResponseEntity.ok("Password changed successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Current password is incorrect.");
        }
    }

    @GetMapping("/userTransaction")
    public ResponseEntity<Integer> getUserTransactions(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok().body(user.getTransaccionesCreadas());
    }

}
