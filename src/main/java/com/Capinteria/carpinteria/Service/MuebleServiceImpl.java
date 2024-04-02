package com.Capinteria.carpinteria.Service;


import com.Capinteria.carpinteria.Entity.Mueble;
import com.Capinteria.carpinteria.Repositories.BaseRepository;
import com.Capinteria.carpinteria.Repositories.MuebleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;



@Service
public class MuebleServiceImpl extends BaseSeriviceImpl<Mueble,Long> implements MuebleService {

    @Autowired
    private MuebleRepository muebleRepository;

    public MuebleServiceImpl(BaseRepository<Mueble, Long> baseRepository, MuebleRepository muebleRepository) {
        super(baseRepository);
        this.muebleRepository = muebleRepository;
    }
    @Override
    public Page<Mueble> getFirst9Muebles(Pageable pageable) {
    return muebleRepository.findFirst9ByOrderByNombreMuebleAsc(pageable);
}


}
