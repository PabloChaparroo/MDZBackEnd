package com.Capinteria.carpinteria.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "solitar_visita")
public class SolicitarVisita extends BaseEntity{

    @Column(name = "fecha_hora_alta_solicitar_visita")
    private String fechaHoraAltaSolicitarVisita;
    @Column(name = "fecha_hora_baja_solicitar_visita")
    private LocalDate fechaHoraBajaSolicitarVisita;
    @Column(name = "consulta_solicitar_visita")
    private String consultaSolicitarVisita;
    @Column(name = "fecha_hora_modificacion_solicitar_visita")
    private LocalDate fechaHoraModificacionSolicitarVisita;


    //Relacion con mueble
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "id_mueble")
    private Mueble mueble;

    //Relacion con el cliente estaban los dos como MERGE
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

}
