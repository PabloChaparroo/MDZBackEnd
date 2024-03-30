package com.Capinteria.carpinteria.Auth;


import com.Capinteria.carpinteria.Entity.Cliente;

import com.Capinteria.carpinteria.Entity.Domicilio;
import com.Capinteria.carpinteria.Entity.Usuario;
import com.Capinteria.carpinteria.Jwt.JwtService;
import com.Capinteria.carpinteria.Repositories.UsuarioRepository;
import com.Capinteria.carpinteria.enumeration.EstadoCliente;
import com.Capinteria.carpinteria.enumeration.Role;
import org.springframework.beans.factory.annotation.Autowired;
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



    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        UserDetails user=usuarioRepository.findByUsername(request.getUsername()).orElseThrow();
        String token=jwtService.getToken(user);

        return AuthResponse.builder()
                .token(token)
                .build();
    }

    public AuthResponse register(RegisterRequest request) {

        Domicilio domicilio = Domicilio.builder()
                .calleDomicilio(request.calleDomicilio)
                .nroCalleDomicilio(request.getNroCalleDomicilio())
                .descripcionDomicilio(request.descripcionDomicilio)
                .localidadDomicilio(request.getLocalidadDomicilio())
                .provinciaDomicilio(request.getProvinciaDomicilio())

                .build();

        Cliente cliente = Cliente.builder()
                .nombreCliente(request.getNombreCliente())
                .apellidoCliente(request.getApellidoCliente())
                .telefonoCliente(request.getTelefonoCliente())
                .mailCliente(request.getMailCliente())
                .fechaHoraAltaCliente(LocalDate.now())
                .estadoCliente(EstadoCliente.ALTA)
                .build();

        cliente.agregarDomicilio(domicilio);

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
                .build();

    }

    public AuthResponse registerEmployee(RegisterEmployeeRequest request) {



        Domicilio domicilio = Domicilio.builder()
                .calleDomicilio(request.calleDomicilioEmpreado)
                .nroCalleDomicilio(request.nroCalleDomicilioEmpleado)
                .descripcionDomicilio(request.descripcionDomicilioEmplreado)
                .localidadDomicilio(request.localidadDomicilioEmpleado)
                .provinciaDomicilio(request.provinciaDomicilioEmpleado)
                .fechaHoraAltaDomicilio(LocalDate.now())
                .build();

        Cliente cliente = Cliente.builder()
                .nombreCliente(request.getNombreEmpleado())
                .apellidoCliente(request.getApellidoEmpleado())
                .telefonoCliente(request.getTelefonoEmpleado())
                .mailCliente(request.getMailEmpleado())
                .fechaHoraAltaCliente(LocalDate.now())
                .estadoCliente(EstadoCliente.ALTA)
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
                .build();

    }

}