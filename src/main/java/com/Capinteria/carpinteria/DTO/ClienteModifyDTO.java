package com.Capinteria.carpinteria.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteModifyDTO {

    Long idCliente;
    int idRole;

    String nombreCliente;
    String apellidoCliente;
    int telefonoCliente;
    String mailCliente;
}