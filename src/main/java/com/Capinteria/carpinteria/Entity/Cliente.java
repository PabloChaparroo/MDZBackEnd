package com.Capinteria.carpinteria.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.Capinteria.carpinteria.enumeration.EstadoCliente;
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@Table(name = "cliente")
public class Cliente extends BaseEntity{
        @Column(name = "nombre_cliente")
        private String nombreCliente;
        @Column(name = "apellido_cliente")
        private String apellidoCliente;
        @Column(name = "telefono_cliente")
        private int telefonoCliente;
        @Column(name = "mail_cliente")
        private String mailCliente;
        @Column(name = "fecha_hora_alta_cliente")
        private LocalDateTime fechaHoraAltaCliente;

        @Column(name = "fecha_hora_modificacion_cliente")
        private LocalDateTime fechaHoraModificacionCliente;

        @Column(name = "fecha_hora_baja_cliente")
        private LocalDateTime fechaHoraBajaCliente;

        private EstadoCliente estadoCliente;


}
