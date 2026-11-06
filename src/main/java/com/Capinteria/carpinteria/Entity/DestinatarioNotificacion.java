package com.Capinteria.carpinteria.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Casilla que recibe el aviso de "consulta nueva". */
@Entity
@Table(name = "destinatario_notificacion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DestinatarioNotificacion extends BaseEntity {

    @Column(name = "mail", nullable = false)
    private String mail;

    @Column(name = "fecha_alta", nullable = false)
    private LocalDateTime fechaAlta;
}
