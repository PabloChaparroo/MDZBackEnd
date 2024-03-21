package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.Entity.Domicilio;
import com.Capinteria.carpinteria.Repositories.BaseRepository;
import com.Capinteria.carpinteria.Repositories.DomicilioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DomicilioServiceImpl extends BaseSeriviceImpl<Domicilio,Long> implements DomicilioService {
    @Autowired
    private DomicilioRepository domicilioRepository;

    public DomicilioServiceImpl(BaseRepository<Domicilio, Long> baseRepository, DomicilioRepository domicilioRepository) {
        super(baseRepository);
        this.domicilioRepository = domicilioRepository;
    }
    @Override
    public boolean domicilioPerteneceAlCliente(Long idDomicilio, Long idCliente) throws Exception {
        try {
            boolean result = domicilioRepository.domicilioPerteneceAlCliente(idDomicilio, idCliente);
            return result;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
