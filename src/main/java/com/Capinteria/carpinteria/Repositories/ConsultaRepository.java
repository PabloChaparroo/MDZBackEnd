package com.Capinteria.carpinteria.Repositories;

import com.Capinteria.carpinteria.Entity.Consulta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultaRepository extends BaseRepository<Consulta, Long> {
    Page<Consulta> findByCliente_NombreClienteContainingIgnoreCase(String nombre, Pageable pageable);
}
