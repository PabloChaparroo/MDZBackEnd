package com.Capinteria.carpinteria.Entity;

import com.Capinteria.carpinteria.enumeration.TipoMadera;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "mueble")
public class Mueble extends BaseEntity{
    @Column(name = "nombreMueble")
    private String nombreMueble;
    @Column(name ="colorMueble")
    private String colorMueble;
    @Column(name = "dimension")
    private String dimension;
    @Column(name= "tipoMadera")
    private TipoMadera tipoMadera;
    @Column(name = "precio")
    private double precio;
    @Column(name ="descripcion")
    private String descripcion;
    @Column(name="imagen")
    private String imagen;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "mueble_id")
    private Categoria categoria;


}
