package com.Capinteria.carpinteria.Entity;

import com.Capinteria.carpinteria.enumeration.EstadoCliente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
@Entity
@Table(name ="cliente")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
        private LocalDate fechaHoraAltaCliente;

        @Column(name = "fecha_hora_modificacion_cliente")
        private LocalDate fechaHoraModificacionCliente;

        @Column(name = "fecha_hora_baja_cliente")
        private LocalDate fechaHoraBajaCliente;

        @Column(name = "estado_cliente")
        private EstadoCliente estadoCliente;

        //Relaciones
        @OneToMany(cascade = CascadeType.PERSIST, orphanRemoval = true, fetch = FetchType.EAGER)
        @Builder.Default
        @JoinColumn(name = "cliente_id")
        private List<Domicilio> domicilioList = new ArrayList<>();

        public void agregarDomicilio(Domicilio domi){
                domicilioList.add(domi);
        }

        //Agregué para mostrar los domicilios
        public void mostrarDomicilios(){
                System.out.println("\n Domicilios de: " + nombreCliente  + apellidoCliente + " : ");
                for (Domicilio domicilio : domicilioList) {
                        System.out.println("\nCalle: " + domicilio.getCalleDomicilio() + "\nLocalidad: " + domicilio.getLocalidadDomicilio() + "\nNumero: " + domicilio.getNroCalleDomicilio() );

                }

        }

}
