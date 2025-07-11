package com.Capinteria.carpinteria.DTO;

import com.Capinteria.carpinteria.enumeration.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioProfileDTO {
    
    private Long id;
    private String username;
    private LocalDate fechaAltaUsuario;
    private Role role;
    
    // Información del cliente directamente en el DTO
    private Long clienteId;
    private String nombreCliente;
    private String apellidoCliente;
    private String mailCliente;
}