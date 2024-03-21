package com.Capinteria.carpinteria.Auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterEmployeeRequest {
    String username;
    String provisionalPassword;
    int idRole;

    String nombreEmpleado;
    String apellidoEmpleado;
    int telefonoEmpleado;
    String mailEmpleado;

    //Domicilio
    String calleDomicilioEmpreado;
    int nroCalleDomicilioEmpleado;
    String descripcionDomicilioEmplreado;
    String localidadDomicilioEmpleado;
    String provinciaDomicilioEmpleado;
}
