package com.Capinteria.carpinteria.Repositories;

import com.Capinteria.carpinteria.Entity.DestinatarioNotificacion;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DestinatarioNotificacionRepository extends BaseRepository<DestinatarioNotificacion, Long> {

    List<DestinatarioNotificacion> findAllByOrderByIdAsc();

    boolean existsByMailIgnoreCase(String mail);
}
