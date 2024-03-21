package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.Entity.Usuario;
import com.Capinteria.carpinteria.Repositories.BaseRepository;
import com.Capinteria.carpinteria.Repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServiceImpl extends BaseSeriviceImpl<Usuario,Long> implements UsuarioService{
    @Autowired
    private UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(BaseRepository<Usuario, Long> baseRepository, UsuarioRepository usuarioRepository) {
        super(baseRepository);
        this.usuarioRepository = usuarioRepository;
    }
    @Override
    public Usuario findUserByUsername(String username) throws Exception{
        try {
            Usuario usuario = usuarioRepository.findUserByUsername(username);
            return usuario;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }
}
