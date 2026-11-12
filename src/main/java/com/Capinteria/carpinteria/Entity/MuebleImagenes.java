package com.Capinteria.carpinteria.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name = "mueble_imagenes")
public class MuebleImagenes extends BaseEntity {

    private boolean esPortada;
    // bytea en PostgreSQL. Sin @Lob: con Postgres, Hibernate mapearia @Lob byte[] a "oid".
    @Column(name = "imagen_imagenes", columnDefinition = "bytea")
    private byte[] imagenes;

    // Version chica (JPEG ~640px) para los listados. No se expone en la API.
    @JsonIgnore
    @Column(name = "miniatura_imagenes", columnDefinition = "bytea")
    private byte[] miniatura;

}
