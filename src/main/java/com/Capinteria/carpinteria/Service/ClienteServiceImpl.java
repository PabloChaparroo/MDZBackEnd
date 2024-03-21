package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.DTO.ClienteDTO;
import com.Capinteria.carpinteria.DTO.ClienteModifyDTO;
import com.Capinteria.carpinteria.DTO.DomicilioDTO;
import com.Capinteria.carpinteria.Entity.Cliente;
import com.Capinteria.carpinteria.Entity.Domicilio;
import com.Capinteria.carpinteria.Entity.Usuario;
import com.Capinteria.carpinteria.Jwt.JwtService;
import com.Capinteria.carpinteria.Repositories.BaseRepository;
import com.Capinteria.carpinteria.Repositories.ClienteRepository;
import com.Capinteria.carpinteria.Repositories.DomicilioRepository;
import com.Capinteria.carpinteria.Repositories.UsuarioRepository;
import com.Capinteria.carpinteria.enumeration.EstadoCliente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;


import java.time.LocalDate;

@Service
public class ClienteServiceImpl extends BaseSeriviceImpl<Cliente, Long> implements ClienteService{
    @Autowired
    private ClienteRepository clienteRepository;

    //Agregado para buscar el cliente atraves del usuario
    @Autowired
    private JwtService jwtService; // Inyecta tu servicio JwtService

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DomicilioRepository domicilioRepository;

    @Autowired
    private DomicilioService domicilioService;


    public ClienteServiceImpl(BaseRepository<Cliente, Long> baseRepository) {
        super(baseRepository);
    }
    @Override
    public Cliente searchById(Long idCliente) throws Exception {
        try {
            Cliente cliente = clienteRepository.searchById(idCliente);
            return cliente;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
    @Override
    public ClienteDTO showProfile(String token) throws Exception{
        try {
            // Obtiene el token JWT de la solicitud HTTP.
            String jwtToken = token.substring(7);
            // Extrae el username del usuario del token JWT utilizando tu servicio JwtService.
            String username = jwtService.getUsernameFromToken(jwtToken);
            Cliente cliente = clienteRepository.findClienteByUsername(username);


            ModelMapper modelMapper = new ModelMapper();
            ClienteDTO clienteDTO = modelMapper.map(cliente, ClienteDTO.class);

            clienteDTO.setUsername(username);

            return clienteDTO;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public ClienteDTO updateProfile(String token, ClienteDTO clienteActualizado) throws Exception {
        try {
            String jwtToken = token.substring(7);
            String username = jwtService.getUsernameFromToken(jwtToken);
            Cliente clienteExistente = clienteRepository.findClienteByUsername(username);

            //System.out.println("!!!!!!!!nombre clienteActualizado " + clienteActualizado.getNombre());
            //System.out.println("!!!!!!!!apellido clienteActualizado " + clienteActualizado.getApellido());

            // Actualizar los campos del cliente con los nuevos datos
            clienteExistente.setNombreCliente(clienteActualizado.getNombreCliente());
            clienteExistente.setApellidoCliente(clienteActualizado.getApellidoCliente());
            clienteExistente.setTelefonoCliente(clienteActualizado.getTelefonoCliente());
            clienteExistente.setMailCliente(clienteActualizado.getMailCliente());
            clienteExistente.setFechaHoraModificacionCliente(LocalDate.now());

            // Guardar el cliente actualizado en la base de datos
            Cliente clienteActualizadoEnBaseDeDatos = clienteRepository.save(clienteExistente);

            ModelMapper modelMapper = new ModelMapper();
            ClienteDTO clienteDTO = modelMapper.map(clienteActualizadoEnBaseDeDatos, ClienteDTO.class);

            return clienteDTO;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Cliente modifyCliente(ClienteModifyDTO clienteModifyDTO) throws Exception {
        try {
            Cliente clienteExistente = findById(clienteModifyDTO.getIdCliente());
            System.out.println("##################CLIENTE: "+clienteExistente.getId()+clienteExistente.getNombreCliente());

            clienteExistente.setNombreCliente(clienteModifyDTO.getNombreCliente());
            clienteExistente.setApellidoCliente(clienteModifyDTO.getApellidoCliente());
            clienteExistente.setTelefonoCliente(clienteModifyDTO.getTelefonoCliente());
            clienteExistente.setMailCliente(clienteModifyDTO.getMailCliente());
            clienteExistente.setFechaHoraModificacionCliente(LocalDate.now());

            for (DomicilioDTO domicilioDTO : clienteModifyDTO.getDomicilioDTOList()) {
                Domicilio domicilioExistente = domicilioRepository.findById(domicilioDTO.getIdDomicilio()).orElse(null);

                if ((domicilioExistente != null) &&
                        domicilioService.domicilioPerteneceAlCliente(domicilioExistente.getId(), clienteModifyDTO.getIdCliente())) {
                    domicilioExistente.setCalleDomicilio(domicilioDTO.getCalleDomicilio());
                    domicilioExistente.setNroCalleDomicilio(domicilioDTO.getNroCalleDomicilio());
                    domicilioExistente.setDescripcionDomicilio(domicilioDTO.getDescripcionDomicilio());
                    domicilioExistente.setLocalidadDomicilio(domicilioDTO.getLocalidadDomicilio());
                    domicilioExistente.setProvinciaDomicilio(domicilioDTO.getProvinciaDomicilio());




                }
                System.out.println("#############RESULTADO service: "+domicilioService.domicilioPerteneceAlCliente(domicilioExistente.getId(), clienteModifyDTO.getIdCliente()));
            }

            return clienteRepository.save(clienteExistente);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Cliente deleteCliente(Long idCliente) throws Exception {
        try {
            Cliente clienteExistente = findById(idCliente);

            clienteExistente.setFechaHoraBajaCliente(LocalDate.now());
            clienteExistente.setEstadoCliente(EstadoCliente.BAJA);

            Usuario usuario = usuarioRepository.findUsuarioByClienteId(idCliente);

            usuario.setFechaBajaUsuario(LocalDate.now());

            return clienteRepository.save(clienteExistente);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
