package com.Capinteria.carpinteria.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogoMuebleDTO {
    private Long id;
    private String nombreMueble;
    private String colorMueble;
    private String descripcion;
    private String fechaAltaMueble;
    private String fechaModificacionMueble;
    private String nombreCategoria;
    private String imagenPortada; // Solo para compatibilidad con código existente
    private List<Object> imagenes; // Lista completa de imágenes para el catálogo (temporalmente como Object)
}
