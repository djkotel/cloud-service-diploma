package ru.netology.cloud.service;

import org.springframework.stereotype.Service;
import ru.netology.cloud.model.Token;
import ru.netology.cloud.model.User;
import ru.netology.cloud.repository.TokenRepository;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenService {
    private final TokenRepository tokenRepository;

    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public String generateToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        Token token = new Token(tokenValue, user);
        tokenRepository.save(token);
        return tokenValue;
    }

    public Optional<Token> validateToken(String token) {
        return tokenRepository.findByToken(token);
    }

    public void deleteToken(String token) {
        tokenRepository.deleteByToken(token);
    }

    public boolean existsByToken(String token) {
        return tokenRepository.existsByToken(token);
    }
}
