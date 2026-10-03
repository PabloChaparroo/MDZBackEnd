package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.DTO.CrearConsultaDTO;
import com.Capinteria.carpinteria.Entity.Consulta;
import org.springframework.http.ResponseEntity;

public interface ConsultaService extends BaseService<Consulta, Long> {

    /**
     * Crear una consulta de un cliente sobre un mueble puntual del catálogo
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
     * Filtrar consultas por nombre de cliente (paginado)
     */
    ResponseEntity<?> filtrarPorNombre(String nombre, int pagina, int tamanoPagina);

    /**
     * Guarda la respuesta del administrador a una consulta y la envía por mail al cliente
     * @param consultaId ID de la consulta a responder
     * @param respuesta Texto de la respuesta
     * @return ResponseEntity con el resultado de la operación
     */
    ResponseEntity<?> responderConsulta(Long consultaId, String respuesta);
}
