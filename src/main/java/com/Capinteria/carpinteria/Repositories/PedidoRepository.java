package com.Capinteria.carpinteria.Repositories;

import com.Capinteria.carpinteria.Entity.Pedido;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;


@Repository
public interface PedidoRepository extends BaseRepository<Pedido, Long>{

}
