package com.Capinteria.carpinteria.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name = "consulta")
public class Consulta extends BaseEntity {

    @Column(name = "mensaje_consulta")
    private String mensajeConsulta;

    @Column(name = "fecha_hora_alta_consulta")
    private LocalDateTime fechaHoraAltaConsulta;

    @Column(name = "fecha_hora_modificacion_consulta")
    private LocalDateTime fechaHoraModificacionConsulta;

    // Relacion con el cliente que realiza la consulta
    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // Relacion con el mueble del catalogo sobre el que se consulta
    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "mueble_id", nullable = false)
    private Mueble mueble;

}
