package com.Capinteria.carpinteria.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "mueble_imagenes_portada")
public class MuebleImagenesPortada extends BaseEntity {

    @Lob
    @Column(name = "imagen_portada", columnDefinition = "LONGBLOB")
    private byte[] imagenPortada;




}
