package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.Entity.SolicitarVisita;
import com.Capinteria.carpinteria.Repositories.BaseRepository;
import com.Capinteria.carpinteria.Repositories.SolicitarVisitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SolicitarVisitaServiceImpl extends BaseSeriviceImpl<SolicitarVisita, Long> implements SolicitarVisitaService {

    @Autowired
    private SolicitarVisitaRepository solicitarVisitaRepository;


    public SolicitarVisitaServiceImpl(BaseRepository<SolicitarVisita, Long> baseRepository, SolicitarVisitaRepository solicitarVisitaRepository) {
        super(baseRepository);
        this.solicitarVisitaRepository = solicitarVisitaRepository;
    }

    @Override
    public List<SolicitarVisita> getSolicitudesPaginadas(int pagina, int tamanoPagina) {
        Pageable pageable = PageRequest.of(getUltimaPagina(pagina, tamanoPagina), tamanoPagina);
        return solicitarVisitaRepository.findAll(pageable).getContent();
    }

    private int getUltimaPagina(int pagina, int tamanoPagina) {
        long totalSolicitudes = solicitarVisitaRepository.count();
        int totalPages = (int) Math.ceil((double) totalSolicitudes / tamanoPagina);
        // Calculamos la página inicial para que comience desde el final
        int ultimaPagina = totalPages - 1;
        int paginaInicial = Math.max(0, ultimaPagina - pagina); // Aseguramos que no sea negativa
        return paginaInicial;
    }



}
