package com.Capinteria.carpinteria.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImagenDTO {
    private Long id;
    private String imagenes; // Base64 encoded image
    private Boolean esPortada;
}