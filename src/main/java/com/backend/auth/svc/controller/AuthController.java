package com.backend.auth.svc.controller;

import com.backend.auth.svc.model.request.LoginRequest;
import com.backend.auth.svc.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(Map.of("access_token", token));
    }

    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> getToken(@RequestParam String userId) {
        try{
            String token =  authService.getToken(userId);
            return ResponseEntity.ok(Map.of("access_token", token));
        }
        catch (RuntimeException e){
            System.out.println(e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized", "message", "JWT token expired. Please log in again to obtain a new token."));
        }
    }

    @PostMapping("/login/multi/users/{start}/{end}")
    public ResponseEntity<Map<String, String>> login(@PathVariable int start, @PathVariable int end) {
        List<String> tokens = authService.multiLogin(start,end);
        Map<String, String> tokenMap = new LinkedHashMap<>();
        for (int i = 0; i < tokens.size(); i++) {
            tokenMap.put("access_token_" + (i + 1), tokens.get(i));
        }
        return ResponseEntity.ok(tokenMap);
    }
}
