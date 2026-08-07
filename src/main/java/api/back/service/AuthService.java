package api.back.service;

import org.springframework.stereotype.Service;

@Service
public class AuthService {
    public boolean isPasswordValid(String password){
        if(password == null || password.isEmpty() || password.length() < 8){
            return false;
        };
        return password.matches("^(?!.*['\"\\\\\\/|])(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,128}\\z");
    }
}
