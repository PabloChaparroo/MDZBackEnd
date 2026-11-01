package com.Capinteria.carpinteria.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Data
@EqualsAndHashCode(callSuper = true)
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
    @Column(name ="descripcion")
    private String descripcion;

    @Column(name = "link_instagram")
    private String linkInstagram;
    @Column(name = "link_facebook")
    private String linkFacebook;

    // Se decide solo al dar de alta: updatable = false evita que una edición lo cambie
    @Column(name = "publicar", nullable = false, updatable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private boolean publicar = false;

    @Column(name ="fecha_alta")
    private String fechaAltaMueble;
    @Column(name ="fecha_modificacion")
    private String fechaModificacionMueble;
    @Column(name ="fecha_baja")
    private String fechaBajaMueble;


    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;


    @OneToMany(cascade = CascadeType.MERGE)
    @JoinColumn(name = "mueble_id")
    @Builder.Default
    private List<MuebleImagenes> imagenes = new ArrayList<>();
}
