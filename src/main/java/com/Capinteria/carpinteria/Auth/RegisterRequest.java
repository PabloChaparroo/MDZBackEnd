package com.Capinteria.carpinteria.Auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    int id;
    String username;
    String password;

    String nombreCliente;
    String apellidoCliente;
    int telefonoCliente;
    String mailCliente;

    //Domicilio
    String calleDomicilio;
    int nroCalleDomicilio;
    String descripcionDomicilio;
    String localidadDomicilio;
    String provinciaDomicilio;
}