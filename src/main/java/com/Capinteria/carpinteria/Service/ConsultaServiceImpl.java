package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.DTO.CrearConsultaDTO;
import com.Capinteria.carpinteria.Entity.Cliente;
import com.Capinteria.carpinteria.Entity.Consulta;
import com.Capinteria.carpinteria.Entity.Mueble;
import com.Capinteria.carpinteria.Repositories.BaseRepository;
import com.Capinteria.carpinteria.Repositories.ClienteRepository;
import com.Capinteria.carpinteria.Repositories.ConsultaRepository;
import com.Capinteria.carpinteria.Repositories.MuebleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ConsultaServiceImpl extends BaseSeriviceImpl<Consulta, Long> implements ConsultaService {

    private static final Logger logger = LoggerFactory.getLogger(ConsultaServiceImpl.class);

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MuebleRepository muebleRepository;

    public ConsultaServiceImpl(BaseRepository<Consulta, Long> baseRepository, ConsultaRepository consultaRepository) {
        super(baseRepository);
        this.consultaRepository = consultaRepository;
    }

    @Override
    public ResponseEntity<?> filtrarPorNombre(String nombre, int pagina, int tamanoPagina) {
        Pageable pageable = PageRequest.of(pagina, tamanoPagina, Sort.by(Sort.Direction.DESC, "fechaHoraAltaConsulta"));
        Page<Consulta> page = consultaRepository.findByCliente_NombreClienteContainingIgnoreCase(nombre, pageable);
        return ResponseEntity.ok().body(page);
    }

    @Override
    @Transactional
    public ResponseEntity<?> crearConsulta(CrearConsultaDTO crearConsultaDTO) {
        logger.info("\n=== [CREAR-CONSULTA] INICIANDO CREACIÓN DE CONSULTA ===");

        if (crearConsultaDTO.getCliente() == null || crearConsultaDTO.getMuebleId() == null) {
            logger.warn("⚠️ [DATOS-INCOMPLETOS] Falta 'cliente' o 'muebleId' en la solicitud");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"Los datos de la consulta están incompletos\"}");
        }

        logger.info("Cliente: {} {}", crearConsultaDTO.getCliente().getNombreCliente(),
                crearConsultaDTO.getCliente().getApellidoCliente());
        logger.info("Email: {}", crearConsultaDTO.getCliente().getMailCliente());
        logger.info("Mueble ID: {}", crearConsultaDTO.getMuebleId());
        logger.info("Mensaje: {}", crearConsultaDTO.getMensajeConsulta());

        try {
            Mueble mueble = muebleRepository.findById(crearConsultaDTO.getMuebleId()).orElse(null);
            if (mueble == null) {
                logger.warn("⚠️ [MUEBLE-NO-ENCONTRADO] Mueble con ID {} no existe", crearConsultaDTO.getMuebleId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\":\"El mueble consultado no existe\"}");
            }

            // Verificar si el cliente ya existe por email
            Cliente clienteExistente = clienteRepository.findByMailCliente(crearConsultaDTO.getCliente().getMailCliente());

            Cliente clienteGuardado;
            if (clienteExistente != null) {
                logger.info("✅ [CLIENTE-EXISTENTE] Cliente encontrado con ID: {}", clienteExistente.getId());
                clienteGuardado = clienteExistente;
            } else {
                Cliente nuevoCliente = Cliente.builder()
                        .nombreCliente(crearConsultaDTO.getCliente().getNombreCliente())
                        .apellidoCliente(crearConsultaDTO.getCliente().getApellidoCliente())
                        .telefonoCliente(crearConsultaDTO.getCliente().getTelefonoCliente())
                        .mailCliente(crearConsultaDTO.getCliente().getMailCliente())
                        .fechaHoraAltaCliente(LocalDateTime.now())
                        .build();

                clienteGuardado = clienteRepository.save(nuevoCliente);
                logger.info("✅ [CLIENTE-NUEVO] Cliente creado con ID: {}", clienteGuardado.getId());
            }

            Consulta consulta = Consulta.builder()
                    .fechaHoraAltaConsulta(LocalDateTime.now())
                    .mensajeConsulta(crearConsultaDTO.getMensajeConsulta())
                    .cliente(clienteGuardado)
                    .mueble(mueble)
                    .build();

            Consulta consultaGuardada = consultaRepository.save(consulta);
            logger.info("✅ [CONSULTA-CREADA] Consulta creada con ID: {}", consultaGuardada.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Consulta creada exitosamente");
            response.put("consultaId", consultaGuardada.getId());
            response.put("clienteId", clienteGuardado.getId());
            response.put("clienteNuevo", clienteExistente == null);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ [CREAR-CONSULTA] Error al crear consulta: {}", e.getMessage());
            // Se relanza para que @Transactional revierta el cliente recién creado (si lo hubo)
            // en la misma transacción. El controller la traduce a una respuesta 500 limpia.
            throw new RuntimeException("Error interno al crear la consulta: " + e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<?> obtenerConsultasPaginadas(int pageNumber) {
        logger.info("\n=== [OBTENER-CONSULTAS] INICIANDO CONSULTA PAGINADA ===");
        logger.info("Página solicitada: {}", pageNumber);

        try {
            Pageable pageable = PageRequest.of(pageNumber, 20, Sort.by(Sort.Direction.DESC, "fechaHoraAltaConsulta"));
            Page<Consulta> consultasPaginadas = consultaRepository.findAll(pageable);

            logger.info("✅ [CONSULTA-DB] Consultas encontradas: {}", consultasPaginadas.getTotalElements());

            Map<String, Object> response = new HashMap<>();
            response.put("content", consultasPaginadas.getContent());
            response.put("totalElements", consultasPaginadas.getTotalElements());
            response.put("totalPages", consultasPaginadas.getTotalPages());
            response.put("number", consultasPaginadas.getNumber());
            response.put("size", consultasPaginadas.getSize());
            response.put("numberOfElements", consultasPaginadas.getNumberOfElements());
            response.put("first", consultasPaginadas.isFirst());
            response.put("last", consultasPaginadas.isLast());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ [OBTENER-CONSULTAS] Error al obtener consultas paginadas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error interno al obtener las consultas\"}");
        }
    }
}
