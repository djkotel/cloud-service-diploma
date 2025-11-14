\1

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.netology.cloud.model.User;
import ru.netology.cloud.service.UserService;
import ru.netology.cloud.service.TokenService;
import java.util.Map;
import java.util.Optional;

@RestController
public class AuthController {
    private final UserService userService;
    private final TokenService tokenService;

    public AuthController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String login = credentials.get("login");
        String password = credentials.get("password");

        if (login == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Login and password required"));
        }

        if (userService.validateUser(login, password)) {
            Optional<User> userOpt = userService.findByLogin(login);
            if (userOpt.isPresent()) {
                String token = tokenService.generateToken(userOpt.get());
                return ResponseEntity.ok(Map.of("auth-token", token));
            }
        }

        return ResponseEntity.badRequest().body(Map.of("message", "Bad credentials"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("auth-token") String authToken) {
        if (tokenService.existsByToken(authToken)) {
            tokenService.deleteToken(authToken);
        }
        return ResponseEntity.ok().build();
    }
}
