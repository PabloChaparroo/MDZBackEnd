package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.DTO.CambiarEstadoDTO;
import com.Capinteria.carpinteria.DTO.CrearConsultaDTO;
import com.Capinteria.carpinteria.Entity.SolicitarVisita;
import org.springframework.http.ResponseEntity;


public interface SolicitarVisitaService extends BaseService<SolicitarVisita, Long>{

    /**
     * Crear una consulta con cliente y solicitud de visita
     * @param crearConsultaDTO Objeto con los datos necesarios para crear la consulta
     * @return ResponseEntity con el resultado de la operación
     */
    ResponseEntity<?> crearConsulta(CrearConsultaDTO crearConsultaDTO);
    
    /**
     * Obtener todas las consultas paginadas
     * @param pageNumber Número de página (0-indexed)
     * @return ResponseEntity con las consultas paginadas
     */
    ResponseEntity<?> obtenerConsultasPaginadas(int pageNumber);
    
    /**
     * Obtener todas las solicitudes con mueble asociado, paginadas
     * @param pageNumber Número de página (0-indexed)
     * @return ResponseEntity con las solicitudes paginadas que tienen mueble
     */
    ResponseEntity<?> obtenerSolicitudesConMueble(int pageNumber);
    
    /**
     * Marcar una solicitud como finalizada (cambia el estado del cliente a FINALIZADO)
     * @param solicitudId ID de la solicitud a finalizar
     * @return ResponseEntity con el resultado de la operación
     */
    ResponseEntity<?> marcarSolicitudComoFinalizada(Long solicitudId);
    
    /**
     * Cambiar el estado de una solicitud de visita (cambia el estado del cliente asociado)
     * @param solicitudId ID de la solicitud 
     * @param cambiarEstadoDTO DTO con el nuevo estado
     * @return ResponseEntity con el resultado de la operación
     */
    ResponseEntity<?> cambiarEstadoSolicitud(Long solicitudId, CambiarEstadoDTO cambiarEstadoDTO);
    /**
     * Filtrar solicitudes de visita por nombre de cliente (paginado)
     */
    ResponseEntity<?> filtrarPorNombre(String nombre, int pagina, int tamanoPagina);
}
