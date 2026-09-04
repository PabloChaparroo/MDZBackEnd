package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.DTO.ClienteDTO;
import com.Capinteria.carpinteria.DTO.ClienteModifyDTO;
import com.Capinteria.carpinteria.Entity.Cliente;
import com.Capinteria.carpinteria.Entity.Usuario;
import com.Capinteria.carpinteria.Jwt.JwtService;
import com.Capinteria.carpinteria.Repositories.BaseRepository;
import com.Capinteria.carpinteria.Repositories.ClienteRepository;
import com.Capinteria.carpinteria.Repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;


import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ClienteServiceImpl extends BaseSeriviceImpl<Cliente, Long> implements ClienteService{
    
    private static final Logger logger = LoggerFactory.getLogger(ClienteServiceImpl.class);
    
    @Autowired
    private ClienteRepository clienteRepository;

    //Agregado para buscar el cliente atraves del usuario
    @Autowired
    private JwtService jwtService; // Inyecta tu servicio JwtService

    @Autowired
    private UsuarioRepository usuarioRepository;

    //@Autowired
    //private DomicilioRepository domicilioRepository;

    //@Autowired
    //private DomicilioService domicilioService;


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
            clienteExistente.setFechaHoraModificacionCliente(LocalDateTime.now());

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
            clienteExistente.setFechaHoraModificacionCliente(LocalDateTime.now());

            /*
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
            }*/

            return clienteRepository.save(clienteExistente);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Cliente deleteCliente(Long idCliente) throws Exception {
        try {
            Cliente clienteExistente = findById(idCliente);

            clienteExistente.setFechaHoraBajaCliente(LocalDateTime.now());
            // Nota: EstadoCliente no está definido en la entidad Cliente actual

            Usuario usuario = usuarioRepository.findUsuarioByClienteId(idCliente);

            usuario.setFechaBajaUsuario(LocalDate.now());

            return clienteRepository.save(clienteExistente);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Cliente getClienteByMailCliente(String mailCliente) {
        // Implementación para buscar un cliente por su correo electrónico en el repositorio
        return clienteRepository.findByMailCliente(mailCliente);
    }

    @Override
    public Cliente crearCliente(Cliente cliente) {
        logger.info("[CREAR-CLIENTE] Iniciando proceso de creación de cliente");
        
        try {
            // Validar que el cliente no sea null
            if (cliente == null) {
                logger.error("[ERROR] El cliente no puede ser null");
                throw new RuntimeException("El cliente no puede ser null");
            }
            
            // Validar campos requeridos
            if (cliente.getNombreCliente() == null || cliente.getNombreCliente().trim().isEmpty()) {
                logger.error("[ERROR] El nombre del cliente es requerido");
                throw new RuntimeException("El nombre del cliente es requerido");
            }
            
            if (cliente.getApellidoCliente() == null || cliente.getApellidoCliente().trim().isEmpty()) {
                logger.error("[ERROR] El apellido del cliente es requerido");
                throw new RuntimeException("El apellido del cliente es requerido");
            }
            
            if (cliente.getMailCliente() == null || cliente.getMailCliente().trim().isEmpty()) {
                logger.error("[ERROR] El email del cliente es requerido");
                throw new RuntimeException("El email del cliente es requerido");
            }
            
            // Verificar que el email no esté ya registrado
            Cliente clienteExistente = clienteRepository.findByMailCliente(cliente.getMailCliente());
            if (clienteExistente != null) {
                logger.error("[ERROR] Ya existe un cliente con el email: {}", cliente.getMailCliente());
                throw new RuntimeException("Ya existe un cliente registrado con este email");
            }
            
            logger.info("[VALIDACION] Cliente válido: {} {}", cliente.getNombreCliente(), cliente.getApellidoCliente());
            
            // Establecer fecha de alta automáticamente
            LocalDateTime fechaAlta = LocalDateTime.now();
            cliente.setFechaHoraAltaCliente(fechaAlta);
            logger.info("[FECHA-ALTA] Estableciendo fecha de alta: {} para cliente: {} {}", 
                      fechaAlta, cliente.getNombreCliente(), cliente.getApellidoCliente());
            
            // Limpiar fechas de modificación y baja
            cliente.setFechaHoraModificacionCliente(null);
            cliente.setFechaHoraBajaCliente(null);
            
            // Guardar el cliente
            logger.info("[GUARDANDO] Persistiendo cliente en base de datos");
            Cliente clienteGuardado = clienteRepository.save(cliente);
            
            logger.info("[EXITO] Cliente '{}{}' creado exitosamente con ID: {} y fecha de alta: {}", 
                      clienteGuardado.getNombreCliente(), clienteGuardado.getApellidoCliente(), 
                      clienteGuardado.getId(), fechaAlta);
            
            return clienteGuardado;
            
        } catch (Exception e) {
            logger.error("[ERROR] Error durante el proceso de creación de cliente: {}", e.getMessage());
            throw new RuntimeException("Error al crear el cliente: " + e.getMessage(), e);
        }
    }
}
