package com.Capinteria.carpinteria.DTO;

import com.Capinteria.carpinteria.Entity.Domicilio;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

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

    List<DomicilioDTO> domicilio;
}
