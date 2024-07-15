package com.Capinteria.carpinteria.Entity;

import com.Capinteria.carpinteria.Controller.BaseControllerImpl;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "mueble_imagenes")
public class MuebleImagenes extends BaseEntity {

    private boolean esPortada;
    @Lob
    @Column(name = "imagen_imagenes", columnDefinition = "LONGBLOB")
    private byte[] imagenes;


}
