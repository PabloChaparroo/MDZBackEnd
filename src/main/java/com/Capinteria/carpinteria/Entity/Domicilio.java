package com.Capinteria.carpinteria.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "domicilio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Domicilio extends BaseEntity {

    @Column(name = "calleDomiclio")
    private String calleDomicilio;

    @Column(name = "numero_calle_domicilio")
    private int nroCalleDomicilio;

    @Column(name = "descripcion_domicilio")
    private String descripcionDomicilio;

    @Column(name = "localidad_domicilio")
    private String localidadDomicilio;

    @Column(name = "provincia_domicilio")
    private String provinciaDomicilio;

    @Column(name = "fecha_hora_alta_domicilio")
    private LocalDate fechaHoraAltaDomicilio;

    @Column(name = "fecha_hora_modificacion_domicilio")
    private LocalDate fechaHoraModificacionDomicilio;

    @Column(name = "fecha_hora_baja_domicilio")
    private LocalDate fechaHoraBajaDomicilio;

}