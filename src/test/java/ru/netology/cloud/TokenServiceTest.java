package ru.netology.cloud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.netology.cloud.model.Token;
import ru.netology.cloud.model.User;
import ru.netology.cloud.repository.TokenRepository;
import ru.netology.cloud.service.TokenService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TokenServiceTest {

    private TokenRepository tokenRepository;
    private TokenService tokenService;

    @BeforeEach
    public void setup() {
        tokenRepository = Mockito.mock(TokenRepository.class);
        tokenService = new TokenService(tokenRepository);
    }

    @Test
    public void generateToken_savesTokenAndReturnsValue() {
        User user = new User("ivan","$2a$10$hash");
        Mockito.when(tokenRepository.save(Mockito.any())).thenAnswer(i -> i.getArgument(0));

        String token = tokenService.generateToken(user);
        assertNotNull(token);
        Mockito.verify(tokenRepository, Mockito.times(1)).save(Mockito.any(Token.class));
    }

    @Test
    public void validateToken_whenPresent_returnsOptional() {
        Token t = new Token("abc", new User("ivan","p"));
        Mockito.when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(t));

        Optional<Token> res = tokenService.validateToken("abc");
        assertTrue(res.isPresent());
        assertEquals("abc", res.get().getToken());
    }

    @Test
    public void deleteToken_callsRepository() {
        tokenService.deleteToken("to-delete");
        Mockito.verify(tokenRepository, Mockito.times(1)).deleteByToken("to-delete");
    }
}
