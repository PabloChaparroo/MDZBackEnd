package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.DTO.UsuarioProfileDTO;
import com.Capinteria.carpinteria.Entity.Usuario;

public interface UsuarioService extends BaseService<Usuario,Long> {

    Usuario findUserByUsername(String username) throws Exception;
    
    UsuarioProfileDTO showProfile(String authorizationHeader) throws Exception;
}
