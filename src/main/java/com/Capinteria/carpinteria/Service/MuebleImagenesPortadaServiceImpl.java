package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.Entity.MuebleImagenesPortada;
import com.Capinteria.carpinteria.Repositories.BaseRepository;
import com.Capinteria.carpinteria.Repositories.MuebleFotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MuebleImagenesPortadaServiceImpl extends BaseSeriviceImpl<MuebleImagenesPortada,Long> implements MuebleImagenesPortadaService {
    @Autowired
    private MuebleFotoRepository muebleFotoRepository;

    public MuebleImagenesPortadaServiceImpl(BaseRepository<MuebleImagenesPortada, Long> baseRepository, MuebleFotoRepository muebleFotoRepository) {
        super(baseRepository);
        this.muebleFotoRepository = muebleFotoRepository;
    }
}
