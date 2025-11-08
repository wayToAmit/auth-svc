package com.backend.auth.svc.service;

import com.backend.auth.svc.model.entity.User;
import com.backend.auth.svc.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisTokenService redisTokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RedisTokenService redisTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.redisTokenService = redisTokenService;
    }

    public String login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        return redisTokenService.hasValidToken(user.getId()+"")
                .map(token -> {
                    System.out.println("Found valid cached token for " + email);
                    return token;
                })
                .orElseGet(() -> {
                    System.out.println("No valid token found — generating new one...");
                    return generateNewToken(user);
                });
        //return generateNewToken(email, password);
    }

    private String generateNewToken(User user){
        String token = jwtService.generateToken(user);
        long expiry = jwtService.getExpirationSeconds(token);

        redisTokenService.storeToken(token, user, expiry);
        return token;
    }

    public String getToken(String username) {
        return redisTokenService.getToken(username);
    }

    public void logout(String token) {
        redisTokenService.removeToken(jwtService.getTokenId(token));
    }



    // Utility method for testing multiple logins
    public List<String> multiLogin(int start, int end) {
//        List<String> tokens = new ArrayList<>();
//        for(int i = start; i <= end; i++) {
//            String email = "user" + i + "@gmail.com";
//            String password = "pass@user" + i;
//            tokens.add(login(email, password));
//        }
//        return tokens;

        return IntStream.rangeClosed(start, end)
                .parallel()
                .mapToObj(i -> {
                    String email = "user" + i + "@gmail.com";
                    String password = "pass@user" + i;
                    return login(email, password);
                })
                .collect(Collectors.toList());
    }
}
