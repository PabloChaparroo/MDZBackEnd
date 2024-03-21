package com.Capinteria.carpinteria.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomicilioDTO {

    Long idDomicilio;
    String calleDomicilio;
    int nroCalleDomicilio;
    String descripcionDomicilio;
    String localidadDomicilio;
    String provinciaDomicilio;


}