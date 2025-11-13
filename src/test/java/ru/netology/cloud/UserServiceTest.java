package ru.netology.cloud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.netology.cloud.model.User;
import ru.netology.cloud.repository.UserRepository;
import ru.netology.cloud.service.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

public class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    public void setup() {
        userRepository = Mockito.mock(UserRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    public void validateUser_correctCredentials_returnsTrue() {
        User user = new User("ivan", "$2a$10$examplehash");
        Mockito.when(userRepository.findByLogin("ivan")).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("ivan", user.getPassword())).thenReturn(true);

        assertTrue(userService.validateUser("ivan", "ivan"));
    }

    @Test
    public void validateUser_wrongPassword_returnsFalse() {
        User user = new User("ivan", "$2a$10$examplehash");
        Mockito.when(userRepository.findByLogin("ivan")).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);

        assertFalse(userService.validateUser("ivan", "wrong"));
    }

    @Test
    public void findByLogin_notFound_returnsEmpty() {
        Mockito.when(userRepository.findByLogin("nope")).thenReturn(Optional.empty());
        assertTrue(userService.findByLogin("nope").isEmpty());
    }
}
