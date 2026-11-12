package com.Capinteria.carpinteria.Repositories;

import com.Capinteria.carpinteria.Entity.MuebleImagenes;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Repository
public interface MuebleImagenesRepository extends BaseRepository<MuebleImagenes,Long>{

    // Las imágenes cuelgan del mueble por mueble_id (sin campo inverso): se borran por SQL
    @Modifying
    @Query(value = "DELETE FROM mueble_imagenes WHERE mueble_id = :muebleId", nativeQuery = true)
    int eliminarPorMueble(@Param("muebleId") Long muebleId);

    @Query(value = "SELECT id FROM mueble_imagenes WHERE miniatura_imagenes IS NULL ORDER BY id", nativeQuery = true)
    List<Long> idsSinMiniatura();

    @Transactional
    @Modifying
    @Query(value = "UPDATE mueble_imagenes SET miniatura_imagenes = :miniatura WHERE id = :id", nativeQuery = true)
    int guardarMiniatura(@Param("id") Long id, @Param("miniatura") byte[] miniatura);

    // Una foto por mueble (la portada; si no hay, la primera), en su version miniatura si existe.
    // Filas: [mueble_id, imagen_id, es_portada, bytes]. La subconsulta elige el id sin tocar los bytea.
    @Query(value = "SELECT i.mueble_id, i.id, i.es_portada, COALESCE(i.miniatura_imagenes, i.imagen_imagenes) " +
                   "FROM mueble_imagenes i WHERE i.id IN (" +
                   "  SELECT DISTINCT ON (mueble_id) id FROM mueble_imagenes " +
                   "  WHERE mueble_id IN (:muebleIds) ORDER BY mueble_id, es_portada DESC, id)",
           nativeQuery = true)
    List<Object[]> findPortadas(@Param("muebleIds") Collection<Long> muebleIds);

}
