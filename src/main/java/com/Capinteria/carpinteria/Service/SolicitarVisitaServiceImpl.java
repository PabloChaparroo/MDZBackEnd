package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.DTO.CambiarEstadoDTO;
import com.Capinteria.carpinteria.DTO.CrearConsultaDTO;
import com.Capinteria.carpinteria.Entity.Cliente;
import com.Capinteria.carpinteria.Entity.SolicitarVisita;
import com.Capinteria.carpinteria.Repositories.BaseRepository;
import com.Capinteria.carpinteria.Repositories.ClienteRepository;
import com.Capinteria.carpinteria.Repositories.SolicitarVisitaRepository;
import com.Capinteria.carpinteria.enumeration.EstadoCliente;
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
public class SolicitarVisitaServiceImpl extends BaseSeriviceImpl<SolicitarVisita, Long> implements SolicitarVisitaService {

    private static final Logger logger = LoggerFactory.getLogger(SolicitarVisitaServiceImpl.class);
    
    @Autowired
    private SolicitarVisitaRepository solicitarVisitaRepository;
    
    @Autowired
    private ClienteRepository clienteRepository;

    public SolicitarVisitaServiceImpl(BaseRepository<SolicitarVisita, Long> baseRepository, SolicitarVisitaRepository solicitarVisitaRepository) {
        super(baseRepository);
        this.solicitarVisitaRepository = solicitarVisitaRepository;
    }


    @Override
    public ResponseEntity<?> filtrarPorNombre(String nombre, int pagina, int tamanoPagina) {
        Pageable pageable = PageRequest.of(pagina, tamanoPagina, Sort.by(Sort.Direction.DESC, "fechaHoraAltaSolicitarVisita"));
        Page<SolicitarVisita> page = solicitarVisitaRepository.findByCliente_NombreClienteContainingIgnoreCase(nombre, pageable);
        return ResponseEntity.ok().body(page);
    }

    @Override
    @Transactional
    public ResponseEntity<?> crearConsulta(CrearConsultaDTO crearConsultaDTO) {
        logger.info("\n=== [CREAR-CONSULTA] INICIANDO CREACIÓN DE CONSULTA ===");
        logger.info("Cliente: {} {}", crearConsultaDTO.getCliente().getNombreCliente(), 
                crearConsultaDTO.getCliente().getApellidoCliente());
        logger.info("Email: {}", crearConsultaDTO.getCliente().getMailCliente());
        logger.info("Consulta: {}", crearConsultaDTO.getConsultaSolicitarVisita());
        
        try {
            // Verificar si el cliente ya existe por email
            Cliente clienteExistente = clienteRepository.findByMailCliente(crearConsultaDTO.getCliente().getMailCliente());
            
            Cliente clienteGuardado;
            if (clienteExistente != null) {
                logger.info("✅ [CLIENTE-EXISTENTE] Cliente encontrado con ID: {}", clienteExistente.getId());
                clienteGuardado = clienteExistente;
            } else {
                // Crear nuevo cliente
                Cliente nuevoCliente = Cliente.builder()
                        .nombreCliente(crearConsultaDTO.getCliente().getNombreCliente())
                        .apellidoCliente(crearConsultaDTO.getCliente().getApellidoCliente())
                        .telefonoCliente(crearConsultaDTO.getCliente().getTelefonoCliente())
                        .mailCliente(crearConsultaDTO.getCliente().getMailCliente())
                        .fechaHoraAltaCliente(LocalDateTime.now())
                        .estadoCliente(com.Capinteria.carpinteria.enumeration.EstadoCliente.PENDIENTE)
                        .build();
                
                clienteGuardado = clienteRepository.save(nuevoCliente);
                logger.info("✅ [CLIENTE-NUEVO] Cliente creado con ID: {} - Estado: PENDIENTE", clienteGuardado.getId());
            }
            
            // Crear solicitud de visita
            SolicitarVisita solicitudVisita = SolicitarVisita.builder()
                    .fechaHoraAltaSolicitarVisita(java.time.LocalDateTime.now().toString())
                    .consultaSolicitarVisita(crearConsultaDTO.getConsultaSolicitarVisita())
                    .cliente(clienteGuardado)
                    .mueble(null) // Sin mueble específico
                    .build();
            
            SolicitarVisita solicitudGuardada = solicitarVisitaRepository.save(solicitudVisita);
            logger.info("✅ [SOLICITUD-CREADA] Solicitud de visita creada con ID: {}", solicitudGuardada.getId());
            
            // Crear respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Consulta creada exitosamente");
            response.put("solicitudId", solicitudGuardada.getId());
            response.put("clienteId", clienteGuardado.getId());
            response.put("clienteNuevo", clienteExistente == null);
            
            logger.info("✅ [CREAR-CONSULTA] Consulta creada exitosamente");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ [CREAR-CONSULTA] Error al crear consulta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error interno al crear la consulta: " + e.getMessage() + "\"}");
        }
    }

    @Override
    public ResponseEntity<?> obtenerConsultasPaginadas(int pageNumber) {
        logger.info("\n=== [OBTENER-CONSULTAS] INICIANDO CONSULTA PAGINADA ===");
        logger.info("Página solicitada: {}", pageNumber);
        logger.info("Filtros aplicados: Solo consultas SIN mueble asociado (mueble IS NULL)");
        
        try {
            // Crear paginación ordenada por fecha de alta descendente (más recientes primero)
            Pageable pageable = PageRequest.of(pageNumber, 20, Sort.by(Sort.Direction.DESC, "fechaHoraAltaSolicitarVisita"));
            Page<SolicitarVisita> consultasPaginadas = solicitarVisitaRepository.findByMuebleIsNull(pageable);
            
            logger.info("✅ [CONSULTA-DB] Consultas generales encontradas: {}", consultasPaginadas.getTotalElements());
            
            // Log para verificar que el estado del cliente se está incluyendo
            if (!consultasPaginadas.getContent().isEmpty()) {
                SolicitarVisita primera = consultasPaginadas.getContent().get(0);
                logger.info("🔍 [DEBUG-ESTADO] Primera consulta:");
                logger.info("    - ID Solicitud: {}", primera.getId());
                logger.info("    - Cliente ID: {}", primera.getCliente().getId());
                logger.info("    - Cliente Email: {}", primera.getCliente().getMailCliente());
                logger.info("    - Cliente Nombre: {} {}", primera.getCliente().getNombreCliente(), primera.getCliente().getApellidoCliente());
                logger.info("    - Estado Cliente: {}", primera.getCliente().getEstadoCliente());
                logger.info("    - Consulta: {}", primera.getConsultaSolicitarVisita());
            }
            
            // Crear respuesta paginada
            Map<String, Object> response = new HashMap<>();
            response.put("content", consultasPaginadas.getContent());
            response.put("totalElements", consultasPaginadas.getTotalElements());
            response.put("totalPages", consultasPaginadas.getTotalPages());
            response.put("number", consultasPaginadas.getNumber());
            response.put("size", consultasPaginadas.getSize());
            response.put("numberOfElements", consultasPaginadas.getNumberOfElements());
            response.put("first", consultasPaginadas.isFirst());
            response.put("last", consultasPaginadas.isLast());
            
            logger.info("✅ [OBTENER-CONSULTAS] Consultas generales obtenidas - Página: {}, Total: {}", 
                    pageNumber, consultasPaginadas.getTotalElements());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ [OBTENER-CONSULTAS] Error al obtener consultas paginadas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error interno al obtener las consultas\"}");
        }
    }

    @Override
    public ResponseEntity<?> obtenerSolicitudesConMueble(int pageNumber) {
        logger.info("\n=== [OBTENER-SOLICITUDES-CON-MUEBLE] INICIANDO CONSULTA PAGINADA ===");
        logger.info("Página solicitada: {}", pageNumber);
        logger.info("Filtros aplicados: Solo solicitudes CON mueble asociado (mueble IS NOT NULL)");
        
        try {
            // Crear paginación ordenada por fecha de alta descendente (más recientes primero)
            Pageable pageable = PageRequest.of(pageNumber, 20, Sort.by(Sort.Direction.DESC, "fechaHoraAltaSolicitarVisita"));
            Page<SolicitarVisita> solicitudesPaginadas = solicitarVisitaRepository.findByMuebleIsNotNull(pageable);
            
            logger.info("✅ [CONSULTA-DB] Solicitudes con mueble encontradas: {}", solicitudesPaginadas.getTotalElements());
            
            // Log para verificar que el estado del cliente se está incluyendo
            if (!solicitudesPaginadas.getContent().isEmpty()) {
                SolicitarVisita primera = solicitudesPaginadas.getContent().get(0);
                logger.info("🔍 [DEBUG-ESTADO] Primera solicitud:");
                logger.info("    - ID Solicitud: {}", primera.getId());
                logger.info("    - Cliente ID: {}", primera.getCliente().getId());
                logger.info("    - Cliente Email: {}", primera.getCliente().getMailCliente());
                logger.info("    - Cliente Nombre: {} {}", primera.getCliente().getNombreCliente(), primera.getCliente().getApellidoCliente());
                logger.info("    - Estado Cliente: {}", primera.getCliente().getEstadoCliente());
                logger.info("    - Mueble: {}", primera.getMueble().getNombreMueble());
                logger.info("    - Consulta: {}", primera.getConsultaSolicitarVisita());
            }
            
            // Crear respuesta paginada
            Map<String, Object> response = new HashMap<>();
            response.put("content", solicitudesPaginadas.getContent());
            response.put("totalElements", solicitudesPaginadas.getTotalElements());
            response.put("totalPages", solicitudesPaginadas.getTotalPages());
            response.put("number", solicitudesPaginadas.getNumber());
            response.put("size", solicitudesPaginadas.getSize());
            response.put("numberOfElements", solicitudesPaginadas.getNumberOfElements());
            response.put("first", solicitudesPaginadas.isFirst());
            response.put("last", solicitudesPaginadas.isLast());
            
            logger.info("✅ [OBTENER-SOLICITUDES-CON-MUEBLE] Solicitudes con mueble obtenidas - Página: {}, Total: {}", 
                    pageNumber, solicitudesPaginadas.getTotalElements());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ [OBTENER-SOLICITUDES-CON-MUEBLE] Error al obtener solicitudes con mueble: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error interno al obtener las solicitudes con mueble\"}");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> marcarSolicitudComoFinalizada(Long solicitudId) {
        logger.info("\n=== [MARCAR-FINALIZADA] INICIANDO PROCESO DE FINALIZACIÓN ===");
        logger.info("Solicitud ID: {}", solicitudId);
        
        try {
            // Buscar la solicitud
            SolicitarVisita solicitud = solicitarVisitaRepository.findById(solicitudId).orElse(null);
            
            if (solicitud == null) {
                logger.warn("⚠️ [SOLICITUD-NO-ENCONTRADA] Solicitud con ID {} no existe", solicitudId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"Solicitud no encontrada\"}");
            }
            
            // Obtener el cliente asociado
            Cliente cliente = solicitud.getCliente();
            if (cliente == null) {
                logger.error("❌ [CLIENTE-NO-ASOCIADO] Solicitud {} no tiene cliente asociado", solicitudId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\":\"La solicitud no tiene cliente asociado\"}");
            }
            
            logger.info("Cliente encontrado: {} {} - Estado actual: {}", 
                    cliente.getNombreCliente(), cliente.getApellidoCliente(), cliente.getEstadoCliente());
            
            // Cambiar estado del cliente a FINALIZADO
            cliente.setEstadoCliente(com.Capinteria.carpinteria.enumeration.EstadoCliente.FINALIZADO);
            cliente.setFechaHoraModificacionCliente(LocalDateTime.now());
            
            // Guardar cliente actualizado
            Cliente clienteActualizado = clienteRepository.save(cliente);
            
            logger.info("✅ [CLIENTE-FINALIZADO] Cliente ID {} marcado como FINALIZADO", clienteActualizado.getId());
            
            // Crear respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Solicitud marcada como finalizada exitosamente");
            response.put("solicitudId", solicitudId);
            response.put("clienteId", clienteActualizado.getId());
            response.put("estadoAnterior", "PENDIENTE");
            response.put("estadoActual", "FINALIZADO");
            response.put("fechaFinalizacion", LocalDateTime.now().toString());
            
            logger.info("✅ [MARCAR-FINALIZADA] Proceso completado exitosamente");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ [MARCAR-FINALIZADA] Error al marcar solicitud como finalizada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error interno al finalizar la solicitud: " + e.getMessage() + "\"}");
        }
    }
    
    @Override
    @Transactional
    public ResponseEntity<?> cambiarEstadoSolicitud(Long solicitudId, CambiarEstadoDTO cambiarEstadoDTO) {
        logger.info("\n=== [CAMBIAR-ESTADO-SOLICITUD] INICIANDO PROCESO DE CAMBIO DE ESTADO ===");
        logger.info("Solicitud ID: {}", solicitudId);
        logger.info("Nuevo estado solicitado: {}", cambiarEstadoDTO.getEstado());
        
        try {
            // Validar el estado recibido
            EstadoCliente nuevoEstado;
            try {
                nuevoEstado = cambiarEstadoDTO.getEstadoEnum();
                logger.info("✅ [VALIDACION-ESTADO] Estado válido: {}", nuevoEstado);
            } catch (IllegalArgumentException e) {
                logger.warn("⚠️ [ESTADO-INVALIDO] Estado no válido: {}", cambiarEstadoDTO.getEstado());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\":\"" + e.getMessage() + "\"}");
            }
            
            // Buscar la solicitud
            SolicitarVisita solicitud = solicitarVisitaRepository.findById(solicitudId).orElse(null);
            
            if (solicitud == null) {
                logger.warn("⚠️ [SOLICITUD-NO-ENCONTRADA] Solicitud con ID {} no existe", solicitudId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"Solicitud no encontrada\"}");
            }
            
            // Obtener el cliente asociado
            Cliente cliente = solicitud.getCliente();
            if (cliente == null) {
                logger.error("❌ [CLIENTE-NO-ASOCIADO] Solicitud {} no tiene cliente asociado", solicitudId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\":\"La solicitud no tiene cliente asociado\"}");
            }
            
            // Obtener el estado actual del cliente
            EstadoCliente estadoAnterior = cliente.getEstadoCliente();
            
            logger.info("Cliente encontrado: {} {} - Estado actual: {}", 
                    cliente.getNombreCliente(), cliente.getApellidoCliente(), estadoAnterior);
            
            // Verificar si el estado es diferente al actual
            if (estadoAnterior == nuevoEstado) {
                logger.info("ℹ️ [MISMO-ESTADO] El cliente ya tiene el estado: {}", nuevoEstado);
                
                Map<String, Object> response = new HashMap<>();
                response.put("mensaje", "El cliente ya tiene el estado solicitado");
                response.put("solicitudId", solicitudId);
                response.put("clienteId", cliente.getId());
                response.put("estadoActual", nuevoEstado.toString());
                response.put("fechaConsulta", LocalDateTime.now().toString());
                
                return ResponseEntity.ok(response);
            }
            
            // Cambiar estado del cliente
            cliente.setEstadoCliente(nuevoEstado);
            cliente.setFechaHoraModificacionCliente(LocalDateTime.now());
            
            // Guardar cliente actualizado
            Cliente clienteActualizado = clienteRepository.save(cliente);
            
            logger.info("✅ [CLIENTE-ACTUALIZADO] Cliente ID {} cambiado de {} a {}", 
                    clienteActualizado.getId(), estadoAnterior, nuevoEstado);
            
            // Crear respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Estado de la solicitud cambiado exitosamente");
            response.put("solicitudId", solicitudId);
            response.put("clienteId", clienteActualizado.getId());
            response.put("estadoAnterior", estadoAnterior.toString());
            response.put("estadoActual", nuevoEstado.toString());
            response.put("fechaCambio", LocalDateTime.now().toString());
            
            logger.info("✅ [CAMBIAR-ESTADO-SOLICITUD] Proceso completado exitosamente");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ [CAMBIAR-ESTADO-SOLICITUD] Error al cambiar estado de solicitud: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error interno al cambiar el estado: " + e.getMessage() + "\"}");
        }
    }
}
