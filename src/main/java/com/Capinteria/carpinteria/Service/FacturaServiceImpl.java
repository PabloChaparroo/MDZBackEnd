package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.Entity.Factura;
import com.Capinteria.carpinteria.Repositories.BaseRepository;
import com.Capinteria.carpinteria.Repositories.FacturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FacturaServiceImpl extends BaseSeriviceImpl<Factura, Long> implements FacturaService{
    @Autowired
    private FacturaRepository facturaRepository;

    public FacturaServiceImpl(BaseRepository<Factura, Long> baseRepository, FacturaRepository facturaRepository) {
        super(baseRepository);
        this.facturaRepository = facturaRepository;
    }
}
