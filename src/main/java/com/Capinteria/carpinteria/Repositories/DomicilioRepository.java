package com.Capinteria.carpinteria.Repositories;

import com.Capinteria.carpinteria.Entity.Domicilio;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DomicilioRepository extends BaseRepository<Domicilio,Long>{

    @Query(value = "SELECT CASE WHEN EXISTS (SELECT * FROM cliente c " +
            "JOIN domicilio d ON c.id = d.cliente_id " +
            "WHERE c.id = %:idCliente% AND d.id = %:idDomicilio%) " +
            "THEN TRUE ELSE FALSE END",
            nativeQuery = true)
    boolean domicilioPerteneceAlCliente(
            @Param("idDomicilio") Long idDomicilio,
            @Param("idCliente") Long idCliente
    );
}
