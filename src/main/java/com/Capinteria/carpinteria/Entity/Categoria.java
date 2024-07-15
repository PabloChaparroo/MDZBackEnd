package com.Capinteria.carpinteria.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Builder
@Table(name = "categoria")
public class Categoria extends BaseEntity{
    @Column(name = "nombre_categoria")
    private String nombreCategoria;

    @Column(name="fecha_alta_categoria")
    private Date fechaAltaCategoria;
    @Column(name="fecha_alta_modificacion")
    private Date fechaModificacionCategoria;



}
