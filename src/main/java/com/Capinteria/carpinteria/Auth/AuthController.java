package com.Capinteria.carpinteria.Auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request)
    {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // Para ver el error en el log
            return ResponseEntity.badRequest().body("Error en login: " + e.getMessage());
        }
    }

    @PostMapping(value = "register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request)
    {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping(value = "registerEmployee")
    public ResponseEntity<AuthResponse> registerEmployee(@RequestBody RegisterEmployeeRequest request)
    {
        return ResponseEntity.ok(authService.registerEmployee(request));
    }

    // Endpoint de prueba para verificar que /auth/** está funcionando
    @GetMapping(value = "test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth endpoint funcionando correctamente");
    }
    
    // Endpoint de prueba POST para verificar CORS
    @PostMapping(value = "test-post")
    public ResponseEntity<String> testPost() {
        return ResponseEntity.ok("POST endpoint funcionando correctamente");
    }
    
    // Endpoint de login simplificado para diagnóstico
    @PostMapping(value = "login-test")
    public ResponseEntity<String> loginTest(@RequestBody LoginRequest request) {
        try {
            // Solo verificar que el request llega correctamente
            return ResponseEntity.ok("Login test recibido: " + request.getUsername());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    /**
     * Endpoint para refrescar el access token usando un refresh token válido
     */
    @PostMapping("refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        // Validar el refresh token y emitir un nuevo access token
        try {
            String refreshToken = request.getRefreshToken();
            String username = authService.getUsernameFromRefreshToken(refreshToken);
            if (username == null) {
                return ResponseEntity.status(401).body(null);
            }
            // Buscar el usuario
            org.springframework.security.core.userdetails.UserDetails user = authService.loadUserByUsername(username);
            // Validar refresh token
            if (!authService.isRefreshTokenValid(refreshToken, user)) {
                return ResponseEntity.status(401).body(null);
            }
            // Emitir nuevo access token
            String newAccessToken = authService.generateAccessToken(user);
            String newRefreshToken = authService.generateRefreshToken(user);
            return ResponseEntity.ok(AuthResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(401).body(null);
        }
    }
}
