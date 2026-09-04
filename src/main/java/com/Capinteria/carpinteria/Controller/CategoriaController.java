
package com.Capinteria.carpinteria.Controller;

import com.Capinteria.carpinteria.Entity.Categoria;
import com.Capinteria.carpinteria.Service.CategoriaServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/categoria")
public class CategoriaController extends BaseControllerImpl<Categoria, CategoriaServiceImpl> {

    private static final Logger logger = LoggerFactory.getLogger(CategoriaController.class);

    @GetMapping("/dadas-de-baja")
    public ResponseEntity<?> obtenerCategoriasDadasDeBaja() {
        logger.info("[API-GET] Recibida solicitud para obtener categorías dadas de baja");
        try {
            return ResponseEntity.ok(servicio.obtenerCategoriasDadasDeBaja());
        } catch (Exception e) {
            logger.error("[API-GET] Error al obtener categorías dadas de baja: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Error interno al obtener categorías dadas de baja\"}");
        }
    }

    @GetMapping("/todas")
    public ResponseEntity<?> obtenerTodasCategorias() {
        logger.info("\n[API-GET] Recibida solicitud para obtener todas las categorías");
        
        try {
            return servicio.obtenerCategoriasFormateadas();
        } catch (Exception e) {
            logger.error("\n[API-GET] Error al obtener categorías: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Error interno del servidor\"}");
        }
    }

    @PostMapping("/crear")
    public ResponseEntity<?> crearCategoria(@RequestBody Categoria categoria) {
        logger.info("\n[API-CREAR] Recibida solicitud de creación de categoría: {}", 
                   categoria != null ? categoria.getNombreCategoria() : "null");
        
        try {
            Categoria categoriaCreada = servicio.crearCategoria(categoria);
            
            // Formatear la fecha de alta para el frontend
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String fechaAltaFormateada = formatter.format(categoriaCreada.getFechaAltaCategoria());
            
            logger.info("\n[API-CREAR] Categoría creada exitosamente con ID: {}", categoriaCreada.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body("{\"mensaje\":\"Categoría creada exitosamente\"," +
                      "\"categoriaId\":" + categoriaCreada.getId() + "," +
                      "\"nombre\":\"" + categoriaCreada.getNombreCategoria() + "\"," +
                      "\"fechaAlta\":\"" + fechaAltaFormateada + "\"}");
            
        } catch (RuntimeException e) {
            logger.error("\n[API-CREAR] Error al crear categoría: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("\n[API-CREAR] Error interno al crear categoría: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Error interno del servidor\"}");
        }
    }

    @PutMapping("/modificar/{id}")
    public ResponseEntity<?> modificarCategoria(@PathVariable Long id, @RequestBody Categoria categoria) {
        logger.info("\n=== [API-MODIFICAR] INICIANDO MODIFICACIÓN ===");
        logger.info("ID recibido: {}", id);
        logger.info("Datos recibidos: {}", categoria != null ? categoria.toString() : "null");
        if (categoria != null) {
            logger.info("Nombre categoría: '{}'", categoria.getNombreCategoria());
        }
        
        try {
            Categoria categoriaModificada = servicio.modificarCategoria(id, categoria);
            
            // Formatear la fecha de modificación para el frontend
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String fechaModificacionFormateada = formatter.format(categoriaModificada.getFechaModificacionCategoria());
            
            logger.info("\n✅ [API-MODIFICAR] ÉXITO - Categoría modificada exitosamente con ID: {}", categoriaModificada.getId());
            return ResponseEntity.status(HttpStatus.OK)
                .body("{\"mensaje\":\"Categoría modificada exitosamente\"," +
                      "\"categoriaId\":" + categoriaModificada.getId() + "," +
                      "\"nombre\":\"" + categoriaModificada.getNombreCategoria() + "\"," +
                      "\"fechaModificacion\":\"" + fechaModificacionFormateada + "\"}");
            
        } catch (RuntimeException e) {
            logger.error("\n❌ [API-MODIFICAR] ERROR al modificar categoría ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("\n❌ [API-MODIFICAR] ERROR INTERNO al modificar categoría ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Error interno del servidor\"}");
        }
    }

    @DeleteMapping("/baja-logica/{id}")
    public ResponseEntity<?> bajaLogica(@PathVariable Long id) {
        logger.info("\n[API] Recibida solicitud de baja lógica para categoría ID: {}", id);
        
        try {
            boolean resultado = servicio.bajaLogica(id);
            
            if (resultado) {
                logger.info("\n[API] Baja lógica exitosa para categoría ID: {}", id);
                
                // Obtener la categoría actualizada para devolver la fecha formateada
                Categoria categoriaActualizada = servicio.findById(id);
                
                // Formatear la fecha de baja para el frontend
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String fechaBajaFormateada = formatter.format(categoriaActualizada.getFechaBajaCategoria());
                
                return ResponseEntity.status(HttpStatus.OK)
                    .body("{\"mensaje\":\"Categoría dada de baja exitosamente\"," +
                          "\"categoriaId\":" + id + "," +
                          "\"nombre\":\"" + categoriaActualizada.getNombreCategoria() + "\"," +
                          "\"fechaBaja\":\"" + fechaBajaFormateada + "\"}");
            } else {
                logger.error("\n[API] Fallo en baja lógica para categoría ID: {}", id);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"No se pudo realizar la baja lógica\"}");
            }
            
        } catch (RuntimeException e) {
            logger.error("\n[API] Error en baja lógica para categoría ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("\n[API] Error interno en baja lógica para categoría ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Error interno del servidor\"}");
        }
    }
}
