package com.Capinteria.carpinteria.Entity;

import com.Capinteria.carpinteria.enumeration.FormaPago;
import com.Capinteria.carpinteria.enumeration.TipoEnvio;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "pedido")
public class Pedido extends BaseEntity{

    @Column(name = "fecha_hora_pedido")
    private LocalDate fechaHoraPedido;
    @Column(name = "fecha_hora_baja_pedido")
    private LocalDate fechaHoraBajaPedido;
    @Column(name = "fecha_hora_estimada")
    private LocalDate fechaHoraEstimada;
    @Column(name = "total_precio")
    private double totalPrecio;
    @Column(name = "tipo_envio")
    private TipoEnvio tipoEnvio;


    //Relacion con Pedido Mueble
    @OneToMany(cascade = CascadeType.PERSIST, orphanRemoval = true,fetch = FetchType.EAGER)
    @JoinColumn(name = "id_pedido")
    @Builder.Default
    private List<PedidoMueble> pedidoMuebles = new ArrayList<>();

    //Relacion con el cliente
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

}
