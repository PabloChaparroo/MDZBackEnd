package com.Capinteria.carpinteria.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name="pedido_mueble")
public class PedidoMueble extends BaseEntity {
    @Column(name="cantida_pedido")
    private int cantidadPedido;

    //Relacion Mueble
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_cantidad_pedido")
    private Mueble mueble;
}
