package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.Entity.Mueble;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface MuebleService extends BaseService<Mueble, Long>{


    // Método para obtener los primeros 9 muebles con paginación
    Page<Mueble> getFirst9Muebles(Pageable pageable);

}
