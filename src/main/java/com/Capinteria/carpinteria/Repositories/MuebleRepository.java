package com.Capinteria.carpinteria.Repositories;


import com.Capinteria.carpinteria.Entity.Mueble;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;


@Repository
public interface MuebleRepository extends BaseRepository<Mueble, Long> {

    // Consulta para obtener los primeros 9 muebles ordenados por nombre de manera ascendente
    Page<Mueble> findFirst9ByOrderByNombreMuebleAsc(Pageable pageable);

}
