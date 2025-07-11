package com.Capinteria.carpinteria.DTO;

import com.Capinteria.carpinteria.Entity.Cliente;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearConsultaDTO {
    private Cliente cliente;
    private String fechaHoraAltaSolicitarVisita;
    private String consultaSolicitarVisita;
}
