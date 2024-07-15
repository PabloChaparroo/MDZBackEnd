package com.Capinteria.carpinteria.Repositories;

import com.Capinteria.carpinteria.Entity.SolicitarVisita;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;


@Repository
public interface SolicitarVisitaRepository extends BaseRepository<SolicitarVisita, Long>{


    Page<SolicitarVisita> findAll(Pageable pageable);
}
