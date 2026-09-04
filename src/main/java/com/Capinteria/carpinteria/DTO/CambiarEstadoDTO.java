package com.Capinteria.carpinteria.DTO;

import com.Capinteria.carpinteria.enumeration.EstadoCliente;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CambiarEstadoDTO {
    private String estado;
    
    /**
     * Convierte el estado string a EstadoCliente enum
     * @return EstadoCliente enum correspondiente
     * @throws IllegalArgumentException si el estado no es válido
     */
    public EstadoCliente getEstadoEnum() {
        if (estado == null || estado.trim().isEmpty()) {
            throw new IllegalArgumentException("El estado no puede estar vacío");
        }
        
        try {
            return EstadoCliente.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado no válido: " + estado + ". Estados válidos: PENDIENTE, FINALIZADO");
        }
    }
}
