package com.Capinteria.carpinteria.Repositories;

import com.Capinteria.carpinteria.Entity.MuebleImagenes;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MuebleImagenesRepository extends BaseRepository<MuebleImagenes,Long>{

    // Las imágenes cuelgan del mueble por mueble_id (sin campo inverso): se borran por SQL
    @Modifying
    @Query(value = "DELETE FROM mueble_imagenes WHERE mueble_id = :muebleId", nativeQuery = true)
    int eliminarPorMueble(@Param("muebleId") Long muebleId);

}
