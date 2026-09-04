package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.DTO.UsuarioProfileDTO;
import com.Capinteria.carpinteria.Entity.Usuario;
import com.Capinteria.carpinteria.Jwt.JwtService;
import com.Capinteria.carpinteria.Repositories.BaseRepository;
import com.Capinteria.carpinteria.Repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServiceImpl extends BaseSeriviceImpl<Usuario,Long> implements UsuarioService{
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private JwtService jwtService;

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
    
    @Override
    public UsuarioProfileDTO showProfile(String authorizationHeader) throws Exception {
        try {
            // Extraer el token del header Authorization
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                throw new Exception("Token de autorización inválido o faltante");
            }
            
            String token = authorizationHeader.substring(7); // Remover "Bearer "
            
            // Extraer el username del token (esto también valida el token)
            String username;
            try {
                username = jwtService.getUsernameFromToken(token);
            } catch (Exception tokenException) {
                throw new Exception("Token inválido o expirado: " + tokenException.getMessage());
            }
            
            if (username == null || username.trim().isEmpty()) {
                throw new Exception("No se pudo extraer el usuario del token");
            }
            
            // Buscar el usuario en la base de datos
            Usuario usuario = usuarioRepository.findByUsername(username)
                    .orElseThrow(() -> new Exception("Usuario no encontrado: " + username));
            
            // Validar el token con los detalles del usuario
            if (!jwtService.isTokenValid(token, usuario)) {
                throw new Exception("Token expirado o inválido para este usuario");
            }
            
            // Construir el DTO con información del usuario y cliente
            UsuarioProfileDTO.UsuarioProfileDTOBuilder builder = UsuarioProfileDTO.builder()
                    .id(usuario.getId())
                    .username(usuario.getUsername())
                    .fechaAltaUsuario(usuario.getFechaAltaUsuario())
                    .role(usuario.getRole());
            
            // Agregar información del cliente si existe
            if (usuario.getCliente() != null) {
                builder.clienteId(usuario.getCliente().getId())
                       .nombreCliente(usuario.getCliente().getNombreCliente())
                       .apellidoCliente(usuario.getCliente().getApellidoCliente())
                       .mailCliente(usuario.getCliente().getMailCliente());
            }
            
            return builder.build();
            
        } catch (Exception e) {
            throw new Exception("Error al obtener el perfil del usuario: " + e.getMessage());
        }
    }
}
