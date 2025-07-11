package com.Capinteria.carpinteria.Auth;


import com.Capinteria.carpinteria.Entity.Cliente;

import com.Capinteria.carpinteria.Entity.Usuario;
import com.Capinteria.carpinteria.Jwt.JwtService;
import com.Capinteria.carpinteria.Repositories.UsuarioRepository;
import com.Capinteria.carpinteria.enumeration.Role;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // Métodos para refresh token
    public String getUsernameFromRefreshToken(String refreshToken) {
        try {
            return jwtService.getUsernameFromToken(refreshToken);
        } catch (Exception e) {
            return null;
        }
    }

    public UserDetails loadUserByUsername(String username) {
        return usuarioRepository.findByUsername(username).orElse(null);
    }

    public boolean isRefreshTokenValid(String refreshToken, UserDetails user) {
        try {
            return jwtService.isTokenValid(refreshToken, user);
        } catch (Exception e) {
            return false;
        }
    }

    public String generateAccessToken(UserDetails user) {
        return jwtService.getToken(user);
    }

    public String generateRefreshToken(UserDetails user) {
        return jwtService.getRefreshToken(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        UserDetails user = usuarioRepository.findByUsername(request.getUsername()).orElseThrow();
        return AuthResponse.builder()
                .token(jwtService.getToken(user))
                .refreshToken(jwtService.getRefreshToken(user))
                .build();
    }

    public AuthResponse register(RegisterRequest request) {

        Cliente cliente = Cliente.builder()
                .nombreCliente(request.getNombreCliente())
                .apellidoCliente(request.getApellidoCliente())
                .telefonoCliente(request.getTelefonoCliente())
                .mailCliente(request.getMailCliente())
                .fechaHoraAltaCliente(java.time.LocalDateTime.now())
                .build();

        Usuario user = Usuario.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fechaAltaUsuario(LocalDate.now())
                .role(Role.CLIENTE)
                .build();

        user.setCliente(cliente);
        usuarioRepository.save(user);

        return AuthResponse.builder()
                .token(jwtService.getToken(user))
                .refreshToken(jwtService.getRefreshToken(user))
                .build();

    }

    public AuthResponse registerEmployee(RegisterEmployeeRequest request) {

        Cliente cliente = Cliente.builder()
                .nombreCliente(request.getNombreEmpleado())
                .apellidoCliente(request.getApellidoEmpleado())
                .telefonoCliente(request.getTelefonoEmpleado())
                .mailCliente(request.getMailEmpleado())
                .fechaHoraAltaCliente(java.time.LocalDateTime.now())
                .build();

        Usuario user = Usuario.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.provisionalPassword))
                .fechaAltaUsuario(LocalDate.now())
                .role(Role.EMPLEADO) //ver numeracion de roles
                .build();

        user.setCliente(cliente);
        usuarioRepository.save(user);

        return AuthResponse.builder()
                .token(jwtService.getToken(user))
                .refreshToken(jwtService.getRefreshToken(user))
                .build();

    }

}