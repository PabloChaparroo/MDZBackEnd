package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.Entity.MuebleImagenes;
import com.Capinteria.carpinteria.Entity.MuebleImagenesPortada;
import com.Capinteria.carpinteria.Repositories.BaseRepository;
import com.Capinteria.carpinteria.Repositories.MuebleImagenesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MuebleImagenesServiceImpl extends BaseSeriviceImpl<MuebleImagenes,Long> implements MuebleImagenesService{
    @Autowired
    private MuebleImagenesRepository muebleImagenesRepository;

    public MuebleImagenesServiceImpl(BaseRepository<MuebleImagenes, Long> baseRepository, MuebleImagenesRepository muebleImagenesRepository) {
        super(baseRepository);
        this.muebleImagenesRepository = muebleImagenesRepository;
    }
}
