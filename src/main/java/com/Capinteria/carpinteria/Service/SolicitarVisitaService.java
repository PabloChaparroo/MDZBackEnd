package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.Entity.SolicitarVisita;

import java.util.List;


public interface SolicitarVisitaService extends BaseService<SolicitarVisita, Long>{

    List<SolicitarVisita> getSolicitudesPaginadas(int pagina, int tamanoPagina);
}
