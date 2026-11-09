package com.Capinteria.carpinteria.Repositories;

import com.Capinteria.carpinteria.Entity.Consulta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultaRepository extends BaseRepository<Consulta, Long> {
    Page<Consulta> findByCliente_NombreClienteContainingIgnoreCase(String nombre, Pageable pageable);

    // Borra todas las consultas de un mueble (se usa al eliminarlo definitivamente)
    @Modifying
    @Query("DELETE FROM Consulta c WHERE c.mueble.id = :muebleId")
    int eliminarPorMueble(@Param("muebleId") Long muebleId);
}
