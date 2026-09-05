package com.Capinteria.carpinteria.Controller;

import com.Capinteria.carpinteria.DTO.CrearConsultaDTO;
import com.Capinteria.carpinteria.Entity.Consulta;
import com.Capinteria.carpinteria.Service.ConsultaService;
import com.Capinteria.carpinteria.Service.ConsultaServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/consultas")
public class ConsultaController extends BaseControllerImpl<Consulta, ConsultaServiceImpl> {

    @Autowired
    private ConsultaService consultaService;

    @PostMapping("/crear")
    public ResponseEntity<?> crearConsulta(@RequestBody CrearConsultaDTO crearConsultaDTO) {
        try {
            return consultaService.crearConsulta(crearConsultaDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/obtener-consultas/{page}")
    public ResponseEntity<?> obtenerConsultasPaginadas(@PathVariable("page") int page) {
        return consultaService.obtenerConsultasPaginadas(page);
    }

    @GetMapping("/filtrar-por-nombre")
    public ResponseEntity<?> filtrarPorNombre(
            @RequestParam String nombre,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanoPagina) {
        return consultaService.filtrarPorNombre(nombre, pagina, tamanoPagina);
    }
}
