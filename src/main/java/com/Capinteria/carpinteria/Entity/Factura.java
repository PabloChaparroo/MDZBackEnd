package com.Capinteria.carpinteria.Entity;

import com.Capinteria.carpinteria.enumeration.FormaPago;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "factura")
public class Factura extends BaseEntity {

    @Column(name = "fecha_hora_facturacion")
    private LocalDate fechaHoraFacturacion;

    @Column(name = "fecha_modificacion_factura")
    private LocalDate fechaModificacionFactura;

    @Column(name = "fecha_hora_baja_facturacion")
    private LocalDate fechaHoraBajaFacturacion;

    @Column(name = "descuento")
    private double descuento;

    @Column(name = "mp_merchant_order_id")
    private String mpMerchantOrderId;

    @Column(name = "mp_payment_id")
    private String mpPaymentId;

    @Column(name = "mp_payment_type")
    private String mpPaymentType;

    @Column(name = "mp_preference_id")
    private String mpPreferenceId;

    @Column(name = "totalPrecioFactura")
    private double totalPrecioFactura;

    //Enumeration
    @Column(name = "forma_pago")
    private FormaPago formaPago;


    //Relaciones
    @OneToOne(cascade = CascadeType.PERSIST, orphanRemoval = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

}
