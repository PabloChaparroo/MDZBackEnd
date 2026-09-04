package com.Capinteria.carpinteria.Controller;

import com.Capinteria.carpinteria.Entity.Mueble;
import com.Capinteria.carpinteria.Service.MuebleServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/mueble")
public class MuebleController extends BaseControllerImpl<Mueble, MuebleServiceImpl> {

    @Override
    @PostMapping
    public ResponseEntity<?> save(@RequestBody Mueble entity) {
        return super.save(entity);
    }

    @Override
    @PutMapping
    public ResponseEntity<?> saveAll(@RequestBody List<Mueble> entities) {
        return super.saveAll(entities);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        return super.getOne(id);
    }

    @Override
    @GetMapping
    public ResponseEntity<?> getAll() {
        return super.getAll();
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Mueble entity) {
        return super.update(id, entity);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return super.delete(id);
    }

    private static final Logger logger = LoggerFactory.getLogger(MuebleController.class);
    
    private final MuebleServiceImpl muebleService;
    @Autowired
    public MuebleController(MuebleServiceImpl muebleService, ObjectMapper objectMapper) {
        this.muebleService = muebleService;
    }

    //Create
    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<String> create(@RequestParam("mueble") String muebleJson,
                                         @RequestParam("categoriaId") Long categoriaId,
                                         @RequestParam("files") List<MultipartFile> files,
                                         @RequestParam(value = "portadaIndex", required = false) Integer portadaIndex) {
        logger.info("\n=== [API-CREATE] INICIANDO CREACION DE MUEBLE ===");
        logger.info("Categoria ID recibida: {}", categoriaId);
        logger.info("Numero de archivos: {}", files != null ? files.size() : 0);
        logger.info("Portada index: {}", portadaIndex);
        
        try {
            ResponseEntity<String> resultado = muebleService.createMueble(muebleJson, categoriaId, files, portadaIndex);
            
            if (resultado.getStatusCode().is2xxSuccessful()) {
                logger.info("✅ [API-CREATE] Mueble creado exitosamente");
            } else {
                logger.warn("⚠️ [API-CREATE] Problema al crear mueble");
            }
            
            return resultado;
            
        } catch (Exception e) {
            logger.error("❌ [API-CREATE] Error interno al crear mueble: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Error interno del servidor: " + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{id}/imagenes")
    public ResponseEntity<?> updateImages(@PathVariable Long id,
                                          @RequestParam("files") MultipartFile[] files,
                                          @RequestParam(value = "portadaIndex", required = false) Integer portadaIndex) {
        return muebleService.updateMuebleImages(id, files, portadaIndex);
    }

    @PutMapping("/imagenes")
    public ResponseEntity<?> updateImagesAll(@RequestParam("files") MultipartFile[] files,
                                             @RequestParam(value = "portadaIndex", required = false) Integer portadaIndex) {
        return muebleService.updateAllMueblesImages(files, portadaIndex);
    }

    @GetMapping("/page/{pageNumber}")
    public ResponseEntity<?> getMueblesByPage(@PathVariable int pageNumber) {
        return muebleService.getMueblesPaginated(pageNumber);
    }
    
    @GetMapping("/obtener-todos-muebles/{pageNumber}")
    public ResponseEntity<?> obtenerTodosMuebles(@PathVariable int pageNumber) {
        logger.info("\n=== [API-OBTENER-TODOS-MUEBLES] INICIANDO CONSULTA PARA TODOS LOS MUEBLES ===");
        logger.info("Pagina solicitada: {}", pageNumber);
        logger.info("Filtro por categoria: TODAS las categorias");
        logger.info("Filtro por estado: Solo muebles ACTIVOS (no dados de baja)");
        
        try {
            ResponseEntity<?> resultado = muebleService.obtenerTodosMuebles(pageNumber);
            
            if (resultado.getStatusCode().is2xxSuccessful()) {
                logger.info("✅ [API-OBTENER-TODOS-MUEBLES] Todos los muebles activos cargados - Pagina: {}", pageNumber);
            } else {
                logger.warn("⚠️ [API-OBTENER-TODOS-MUEBLES] Problema al cargar todos los muebles - Pagina: {}", pageNumber);
            }
            
            return resultado;
            
        } catch (Exception e) {
            logger.error("❌ [API-OBTENER-TODOS-MUEBLES] Error interno al obtener todos los muebles: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Error interno del servidor\"}");
        }
    }
    
    @GetMapping("/muebles-por-categoria/{pageNumber}/categoria/{categoriaId}")
    public ResponseEntity<?> getMueblesPorCategoria(@PathVariable int pageNumber, @PathVariable Long categoriaId) {
        logger.info("\n=== [API-MUEBLES-POR-CATEGORIA] INICIANDO CONSULTA DE MUEBLES POR CATEGORIA ===");
        logger.info("Página solicitada: {}", pageNumber);
        logger.info("Filtro por categoría ID: {}", categoriaId);
        logger.info("Respuesta: Objetos Mueble completos con imagen de portada");
        
        try {
            ResponseEntity<?> resultado = muebleService.getCatalogoMueblesPorCategoria(pageNumber, categoriaId);
            
            if (resultado.getStatusCode().is2xxSuccessful()) {
                logger.info("✅ [API-MUEBLES-POR-CATEGORIA] Muebles por categoría cargados - Página: {}, Categoría: {}", pageNumber, categoriaId);
            } else {
                logger.warn("⚠️ [API-MUEBLES-POR-CATEGORIA] Problema al cargar muebles por categoría - Página: {}, Categoría: {}", pageNumber, categoriaId);
            }
            
            return resultado;
            
        } catch (Exception e) {
            logger.error("❌ [API-MUEBLES-POR-CATEGORIA] Error interno al obtener muebles por categoría: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Error interno del servidor\"}");
        }
    }

    @GetMapping("/catalogo-completo/{pageNumber}")
    public ResponseEntity<?> getCatalogoMueblesCompleto(@PathVariable int pageNumber) {
        logger.info("\n=== [API-CATALOGO-COMPLETO] INICIANDO CONSULTA DE MUEBLES COMPLETOS ===");
        logger.info("Página solicitada: {}", pageNumber);
        logger.info("Filtro por categoría: TODAS las categorías");
        logger.info("Respuesta: Objetos completos con todas las imágenes");
        
        try {
            ResponseEntity<?> resultado = muebleService.getCatalogoMueblesCompleto(pageNumber);
            
            if (resultado.getStatusCode().is2xxSuccessful()) {
                logger.info("✅ [API-CATALOGO-COMPLETO] Catálogo completo cargado - Página: {}", pageNumber);
            } else {
                logger.warn("⚠️ [API-CATALOGO-COMPLETO] Problema al cargar catálogo completo - Página: {}", pageNumber);
            }
            
            return resultado;
            
        } catch (Exception e) {
            logger.error("❌ [API-CATALOGO-COMPLETO] Error interno al obtener catálogo completo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Error interno del servidor\"}");
        }
    }

    @GetMapping("/{id}/imagenes")
    public ResponseEntity<?> obtenerImagenesMueble(@PathVariable Long id) {
        logger.info("[API-IMAGENES] Recibida solicitud para obtener imagenes del mueble ID: {}", id);
        
        try {
            ResponseEntity<?> resultado = servicio.obtenerImagenesMueble(id);
            
            if (resultado.getStatusCode() == HttpStatus.OK) {
                logger.info("[API-IMAGENES] Imagenes obtenidas exitosamente para mueble ID: {}", id);
            } else {
                logger.warn("[API-IMAGENES] No se pudieron obtener imagenes para mueble ID: {}", id);
            }
            
            return resultado;
            
        } catch (Exception e) {
            logger.error("[API-IMAGENES] Error interno al obtener imágenes para mueble ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Error interno del servidor\"}");
        }
    }

    @PutMapping("/modificar/{id}")
    public ResponseEntity<?> modificarMueble(@PathVariable Long id, @RequestBody Mueble mueble) {
        logger.info("[API-MODIFICAR] Recibida solicitud de modificacion para mueble ID: {}", id);
        
        try {
            ResponseEntity<?> resultado = servicio.modificarMueble(id, mueble);
            
            if (resultado.getStatusCode() == HttpStatus.OK) {
                logger.info("[API-MODIFICAR] Mueble modificado exitosamente. ID: {}", id);
            } else {
                logger.warn("[API-MODIFICAR] No se pudo modificar el mueble ID: {}", id);
            }
            
            return resultado;
            
        } catch (Exception e) {
            logger.error("[API-MODIFICAR] Error interno al modificar mueble ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Error interno del servidor\"}");
        }
    }

    @DeleteMapping("/baja-logica/{id}")
    public ResponseEntity<?> bajaLogica(@PathVariable Long id) {
        logger.info("[API-MUEBLE] Recibida solicitud de baja logica para mueble ID: {}", id);
        
        try {
            boolean resultado = servicio.bajaLogica(id);
            
            if (resultado) {
                logger.info("[API-MUEBLE] Baja logica exitosa para mueble ID: {}", id);
                return ResponseEntity.status(HttpStatus.OK)
                    .body("{\"mensaje\":\"Mueble dado de baja exitosamente\",\"muebleId\":" + id + "}");
            } else {
                logger.error("[API-MUEBLE] Fallo en baja logica para mueble ID: {}", id);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"No se pudo realizar la baja logica del mueble\"}");
            }
            
        } catch (RuntimeException e) {
            logger.error("[API-MUEBLE] Error en baja logica para mueble ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("[API-MUEBLE] Error interno en baja logica para mueble ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Error interno del servidor\"}");
        }
    }
    
    @GetMapping("/filtrar/buscar/{filtro}/{pageNumber}")
    public ResponseEntity<?> filtrarPorNombreOColor(@PathVariable String filtro, @PathVariable int pageNumber) {
        logger.info("\n=== [API-FILTRAR-BUSQUEDA] INICIANDO BUSQUEDA GENERAL CON PAGINACIÓN ===");
        logger.info("Filtro general: '{}'", filtro);
        logger.info("Página solicitada: {}", pageNumber);
        logger.info("Respuesta: Objetos con solo imagen de portada (optimizado)");
        
        try {
            ResponseEntity<?> resultado = servicio.filtrarPorNombreOColor(filtro, pageNumber);
            logger.info("✅ [API-FILTRAR-BUSQUEDA] Busqueda completada para filtro: '{}' página: {}", filtro, pageNumber);
            return resultado;
        } catch (Exception e) {
            logger.error("❌ [API-FILTRAR-BUSCAR] Error al buscar con filtro '{}' página {}: {}", filtro, pageNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Error interno del servidor\"}");
        }
    }
    
    @PostMapping("/{id}/agregar-imagenes")
    public ResponseEntity<?> agregarImagenesMueble(@PathVariable Long id,
                                                  @RequestParam("files") List<MultipartFile> files,
                                                  @RequestParam(value = "portadaIndex", required = false) Integer portadaIndex) {
        logger.info("\n=== [API-AGREGAR-IMAGENES] INICIANDO AGREGADO DE IMAGENES ===");
        logger.info("Mueble ID: {}", id);
        logger.info("Número de archivos nuevos: {}", files != null ? files.size() : 0);
        logger.info("Índice de nueva portada (opcional): {}", portadaIndex);
        
        try {
            ResponseEntity<?> resultado = muebleService.agregarImagenesMueble(id, files, portadaIndex);
            
            if (resultado.getStatusCode().is2xxSuccessful()) {
                logger.info("✅ [API-AGREGAR-IMAGENES] Imágenes agregadas exitosamente al mueble ID: {}", id);
            } else {
                logger.warn("⚠️ [API-AGREGAR-IMAGENES] Problema al agregar imágenes al mueble ID: {}", id);
            }
            
            return resultado;
            
        } catch (Exception e) {
            logger.error("❌ [API-AGREGAR-IMAGENES] Error interno al agregar imágenes al mueble ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Error interno del servidor: " + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/imagen/{imagenId}")
    public ResponseEntity<?> eliminarImagen(@PathVariable Long imagenId) {
        logger.info("\n=== [API-ELIMINAR-IMAGEN] INICIANDO ELIMINACIÓN DE IMAGEN ===");
        logger.info("Imagen ID: {}", imagenId);
        
        try {
            ResponseEntity<?> resultado = muebleService.eliminarImagen(imagenId);
            
            if (resultado.getStatusCode().is2xxSuccessful()) {
                logger.info("✅ [API-ELIMINAR-IMAGEN] Imagen eliminada exitosamente - ID: {}", imagenId);
            } else {
                logger.warn("⚠️ [API-ELIMINAR-IMAGEN] Problema al eliminar imagen - ID: {}", imagenId);
            }
            
            return resultado;
            
        } catch (Exception e) {
            logger.error("❌ [API-ELIMINAR-IMAGEN] Error interno al eliminar imagen ID {}: {}", imagenId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Error interno del servidor: " + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/imagen/{imagenId}/establecer-portada")
    public ResponseEntity<?> establecerImagenPortada(@PathVariable Long imagenId) {
        logger.info("\n=== [API-ESTABLECER-PORTADA] INICIANDO CAMBIO DE IMAGEN PORTADA ===");
        logger.info("Imagen ID para nueva portada: {}", imagenId);
        
        try {
            ResponseEntity<?> resultado = muebleService.establecerImagenPortada(imagenId);
            
            if (resultado.getStatusCode().is2xxSuccessful()) {
                logger.info("✅ [API-ESTABLECER-PORTADA] Imagen de portada cambiada exitosamente - ID: {}", imagenId);
            } else {
                logger.warn("⚠️ [API-ESTABLECER-PORTADA] Problema al cambiar imagen de portada - ID: {}", imagenId);
            }
            
            return resultado;
            
        } catch (Exception e) {
            logger.error("❌ [API-ESTABLECER-PORTADA] Error interno al cambiar imagen de portada ID {}: {}", imagenId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Error interno del servidor: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/dados-de-baja/{pageNumber}")
    public ResponseEntity<?> obtenerMueblesDadosDeBaja(@PathVariable int pageNumber) {
        logger.info("\n=== [API-MUEBLES-DADOS-DE-BAJA] INICIANDO CONSULTA DE MUEBLES DADOS DE BAJA ===");
        logger.info("Página solicitada: {}", pageNumber);
        logger.info("Filtro por estado: Solo muebles DADOS DE BAJA");
        
        try {
            ResponseEntity<?> resultado = muebleService.obtenerMueblesDadosDeBaja(pageNumber);
            
            if (resultado.getStatusCode().is2xxSuccessful()) {
                logger.info("✅ [API-MUEBLES-DADOS-DE-BAJA] Muebles dados de baja cargados - Página: {}", pageNumber);
            } else {
                logger.warn("⚠️ [API-MUEBLES-DADOS-DE-BAJA] Problema al cargar muebles dados de baja - Página: {}", pageNumber);
            }
            
            return resultado;
            
        } catch (Exception e) {
            logger.error("❌ [API-MUEBLES-DADOS-DE-BAJA] Error interno al obtener muebles dados de baja: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Error interno del servidor\"}");
        }
    }

}
