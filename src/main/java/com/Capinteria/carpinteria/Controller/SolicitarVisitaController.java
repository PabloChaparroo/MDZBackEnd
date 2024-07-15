package com.Capinteria.carpinteria.Controller;

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

import java.util.List;

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

    @PostMapping(value = "/createSolicitarVisita")
    public ResponseEntity<String> createWithVisitAndClient(@RequestParam("solicitarVisita") String solicitarVisitaJson,
                                                           @RequestParam("mueble") String muebleJson,
                                                           @RequestParam("cliente") String clienteJson)
    {
        try {
            // Deserializar el JSON a objetos
            Mueble mueble = objectMapper.readValue(muebleJson, Mueble.class);
            SolicitarVisita solicitarVisita = objectMapper.readValue(solicitarVisitaJson, SolicitarVisita.class);
            Cliente cliente = objectMapper.readValue(clienteJson, Cliente.class);

            // Verificar si el cliente ya existe en la base de datos
            Cliente existingCliente = clienteService.getClienteByMailCliente(cliente.getMailCliente());

            if (existingCliente == null) {
                // Si el cliente no existe, crear uno nuevo
                Cliente newCliente = clienteService.save(cliente);
                // Asociar la solicitud de visita al nuevo cliente
                solicitarVisita.setCliente(newCliente);
            } else {
                // Si el cliente ya existe, asociar la solicitud de visita al cliente existente
                solicitarVisita.setCliente(existingCliente);
            }

            // Guardar el mueble
            Mueble savedMueble = muebleService.save(mueble);

            // Asociar la solicitud de visita al mueble
            solicitarVisita.setMueble(savedMueble);

            // Guardar la solicitud de visita
            solicitarVisitaService.save(solicitarVisita);

            return ResponseEntity.ok("{\"message\": \"Solicitud de Visita creada correctamente\"}");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la solicitud: " + e.getMessage());
        }
    }


    @GetMapping("/solicitudes")
    public List<SolicitarVisita> getSolicitudesPaginadas(@RequestParam(defaultValue = "0") int pagina,
                                                         @RequestParam(defaultValue = "20") int tamanoPagina) {
        return solicitarVisitaService.getSolicitudesPaginadas(pagina, tamanoPagina);
    }

}
