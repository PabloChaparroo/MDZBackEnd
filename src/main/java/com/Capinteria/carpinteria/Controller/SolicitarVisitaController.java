
package com.Capinteria.carpinteria.Controller;

import com.Capinteria.carpinteria.DTO.CambiarEstadoDTO;
import com.Capinteria.carpinteria.Entity.Cliente;
import com.Capinteria.carpinteria.Entity.Mueble;
import com.Capinteria.carpinteria.Entity.SolicitarVisita;
import com.Capinteria.carpinteria.Service.ClienteService;
import com.Capinteria.carpinteria.Service.MuebleService;
import com.Capinteria.carpinteria.Service.SolicitarVisitaService;
import com.Capinteria.carpinteria.Service.SolicitarVisitaServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/solicitarVisita")
public class SolicitarVisitaController extends BaseControllerImpl<SolicitarVisita, SolicitarVisitaServiceImpl> {
    @Autowired
    private  ClienteService clienteService;

    @Autowired
    private MuebleService muebleService;
    @Autowired
    private SolicitarVisitaService solicitarVisitaService;
    private final ObjectMapper objectMapper;

    public SolicitarVisitaController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @PostMapping("/crearConsulta")
    public ResponseEntity<?> crearConsulta(@RequestBody com.Capinteria.carpinteria.DTO.CrearConsultaDTO crearConsultaDTO) {
        return solicitarVisitaService.crearConsulta(crearConsultaDTO);
    }

    @PostMapping(value = "/createSolicitarVisita")
    public ResponseEntity<String> createWithVisitAndClient(@RequestParam("solicitarVisita") String solicitarVisitaJson,
                                                           @RequestParam("muebleId") Long muebleId,
                                                           @RequestParam("cliente") String clienteJson) {
        try {

            // Deserializar el JSON a objetos
            SolicitarVisita solicitarVisita = objectMapper.readValue(solicitarVisitaJson, SolicitarVisita.class);
            Cliente cliente = objectMapper.readValue(clienteJson, Cliente.class);

            // Verificar si el cliente ya existe en la base de datos
            Cliente existingCliente = clienteService.getClienteByMailCliente(cliente.getMailCliente());
            if (existingCliente == null) {
                // Si el cliente no existe, crear uno nuevo
                Cliente newCliente = clienteService.save(cliente);
                solicitarVisita.setCliente(newCliente);
            } else {
                solicitarVisita.setCliente(existingCliente);
            }

            // Buscar el mueble por ID
            Mueble mueble;
            try {
                mueble = muebleService.findById(muebleId);
            } catch (Exception ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al buscar mueble: " + ex.getMessage());
            }
            if (mueble == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Mueble no encontrado con id " + muebleId);
            }
            solicitarVisita.setMueble(mueble);

            // Setear la fecha de alta
            solicitarVisita.setFechaHoraAltaSolicitarVisita(java.time.LocalDateTime.now().toString());

            // Guardar la solicitud de visita
            solicitarVisitaService.save(solicitarVisita);

            return ResponseEntity.ok("{\"message\": \"Solicitud de Visita creada correctamente\"}");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la solicitud: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstadoSolicitud(@PathVariable("id") Long id, @RequestBody CambiarEstadoDTO cambiarEstadoDTO) {
        return solicitarVisitaService.cambiarEstadoSolicitud(id, cambiarEstadoDTO);
    }


    @GetMapping("/obtener-consultas/{page}")
    public ResponseEntity<?> obtenerConsultasPaginadas(@PathVariable("page") int page) {
        return solicitarVisitaService.obtenerConsultasPaginadas(page);
    }

    @GetMapping("/filtrar-por-nombre")
    public ResponseEntity<?> filtrarPorNombre(
            @RequestParam String nombre,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanoPagina) {
        return solicitarVisitaService.filtrarPorNombre(nombre, pagina, tamanoPagina);
    }

    @GetMapping("/obtener-solicitudes-con-mueble/{page}")
    public ResponseEntity<?> obtenerSolicitudesConMueble(@PathVariable("page") int page) {
        return solicitarVisitaService.obtenerSolicitudesConMueble(page);
    }
}
