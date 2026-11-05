package com.Capinteria.carpinteria.Repositories;


import com.Capinteria.carpinteria.Entity.Mueble;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MuebleRepository extends BaseRepository<Mueble, Long> {

    // Consulta para obtener los primeros 9 muebles ordenados por nombre de manera ascendente
    Page<Mueble> findFirst9ByOrderByNombreMuebleAsc(Pageable pageable);

    // Consulta para verificar si un mueble existe y no está dado de baja
    @Query("SELECT m FROM Mueble m WHERE m.id = :id AND m.fechaBajaMueble IS NULL")
    Mueble findByIdAndNotDeleted(@Param("id") Long id);

    // Consulta para filtrar muebles por nombre O color (activos solamente) - CON PAGINACIÓN
    @Query("SELECT m FROM Mueble m WHERE (LOWER(m.nombreMueble) LIKE LOWER(CONCAT('%', :filtro, '%')) OR LOWER(m.colorMueble) LIKE LOWER(CONCAT('%', :filtro, '%'))) AND m.fechaBajaMueble IS NULL ORDER BY m.nombreMueble ASC")
    Page<Mueble> findByNombreOrColorContainingAndNotDeleted(@Param("filtro") String filtro, Pageable pageable);

    // Consulta nativa optimizada para filtrar muebles por nombre O color con solo imagen de portada
    @Query(value = "SELECT m.id, m.nombre_mueble, m.color_mueble, " +
                   "m.descripcion, m.fecha_alta, m.fecha_modificacion, " +
                   "c.nombre_categoria, i.imagen_imagenes as imagen_portada " +
                   "FROM mueble m " +
                   "LEFT JOIN categoria c ON m.categoria_id = c.id " +
                   "LEFT JOIN mueble_imagenes i ON m.id = i.mueble_id AND i.es_portada = true " +
                   "WHERE m.fecha_baja IS NULL " +
                   "AND (m.nombre_mueble ILIKE %:filtro% OR m.color_mueble ILIKE %:filtro%) " +
                   "ORDER BY m.nombre_mueble ASC",
           nativeQuery = true)
    Page<Object[]> findCatalogoMueblesByFiltroOptimized(@Param("filtro") String filtro, Pageable pageable);

    // Consulta nativa súper optimizada para catálogo
    // IMPORTANTE: Solo devuelve muebles ACTIVOS (fecha_baja IS NULL)
    // Incluye categoría y solo la imagen de portada convertida automáticamente
    @Query(value = "SELECT m.id, m.nombre_mueble, m.color_mueble, " +
                   "m.descripcion, m.fecha_alta, m.fecha_modificacion, " +
                   "c.nombre_categoria, i.imagen_imagenes as imagen_portada " +
                   "FROM mueble m " +
                   "LEFT JOIN categoria c ON m.categoria_id = c.id " +
                   "LEFT JOIN mueble_imagenes i ON m.id = i.mueble_id AND i.es_portada = true " +
                   "WHERE m.fecha_baja IS NULL " +
                   "ORDER BY m.nombre_mueble ASC",
           nativeQuery = true)
    Page<Object[]> findCatalogoMueblesOptimized(Pageable pageable);
    
    // Consulta nativa optimizada para catálogo filtrada por categoría
    // IMPORTANTE: Solo devuelve muebles ACTIVOS de una categoría específica
    @Query(value = "SELECT m.id, m.nombre_mueble, m.color_mueble, " +
                   "m.descripcion, m.fecha_alta, m.fecha_modificacion, " +
                   "c.nombre_categoria, i.imagen_imagenes as imagen_portada " +
                   "FROM mueble m " +
                   "LEFT JOIN categoria c ON m.categoria_id = c.id " +
                   "LEFT JOIN mueble_imagenes i ON m.id = i.mueble_id AND i.es_portada = true " +
                   "WHERE m.fecha_baja IS NULL AND m.categoria_id = :categoriaId " +
                   "ORDER BY m.nombre_mueble ASC",
           nativeQuery = true)
    Page<Object[]> findCatalogoMueblesByCategoriaOptimized(@Param("categoriaId") Long categoriaId, Pageable pageable);

    // Métodos para obtener muebles completos con imágenes
    Page<Mueble> findByFechaBajaMuebleIsNull(Pageable pageable);
    
    Page<Mueble> findByCategoriaIdAndFechaBajaMuebleIsNull(Long categoriaId, Pageable pageable);

    // Novedades: muebles activos marcados para publicar al darlos de alta
    Page<Mueble> findByPublicarTrueAndFechaBajaMuebleIsNull(Pageable pageable);

    // Destacados de la Home: el primer mueble activo de cada categoría
    @Query(value = "SELECT MIN(m.id) FROM mueble m WHERE m.fecha_baja IS NULL AND m.categoria_id IS NOT NULL " +
                   "GROUP BY m.categoria_id ORDER BY MIN(m.id) LIMIT 8", nativeQuery = true)
    List<Long> findIdsDestacados();

    // Consulta para obtener muebles dados de baja (fecha_baja IS NOT NULL)
    @Query("SELECT m FROM Mueble m WHERE m.fechaBajaMueble IS NOT NULL ORDER BY m.fechaBajaMueble DESC")
    Page<Mueble> findByFechaBajaMuebleIsNotNull(Pageable pageable);

    // Busca el mueble dueño de una imagen puntual sin recorrer toda la tabla
    @Query(value = "SELECT mueble_id FROM mueble_imagenes WHERE id = :imagenId", nativeQuery = true)
    Long findMuebleIdByImagenId(@Param("imagenId") Long imagenId);

}
