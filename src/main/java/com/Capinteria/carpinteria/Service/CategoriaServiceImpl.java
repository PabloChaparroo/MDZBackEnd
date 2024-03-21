package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.Entity.Categoria;
import com.Capinteria.carpinteria.Repositories.BaseRepository;
import com.Capinteria.carpinteria.Repositories.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoriaServiceImpl extends BaseSeriviceImpl<Categoria, Long> implements CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(BaseRepository<Categoria, Long> baseRepository, CategoriaRepository categoriaRepository) {
        super(baseRepository);
        this.categoriaRepository = categoriaRepository;
    }
}
