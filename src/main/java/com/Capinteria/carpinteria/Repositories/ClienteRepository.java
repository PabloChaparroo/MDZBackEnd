package com.Capinteria.carpinteria.Repositories;

import com.Capinteria.carpinteria.Entity.Cliente;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends BaseRepository<Cliente, Long> {

    //Buscar un cliente  con su id
    @Query(
            value = "SELECT * FROM Cliente WHERE id = %:idCliente%",
            nativeQuery = true
    )
    Cliente searchById(
            @Param("idCliente") Long idCliente
    );

    //Buscar un cliente con su username de Usuario
    @Query(
            value = "SELECT c.* FROM usuario u " +
                    "INNER JOIN cliente c ON u.cliente_id = c.id AND c.estado_cliente = '1'" +
                    "WHERE u.username = :username",
            nativeQuery = true
    )
    Cliente findClienteByUsername(@Param("username") String username);
    }
