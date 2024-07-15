package com.Capinteria.carpinteria.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {

    String username;

    Long id;
    String nombreCliente;
    String apellidoCliente;
    int telefonoCliente;
    String mailCliente;
    LocalDate fechaHoraModificacionCliente;
    //Falta direccion

    //List<DomicilioDTO> domicilio;
}
