package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.Entity.Categoria;
import com.Capinteria.carpinteria.Repositories.BaseRepository;
import com.Capinteria.carpinteria.Repositories.CategoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoriaServiceImpl extends BaseSeriviceImpl<Categoria, Long> implements CategoriaService {

    private static final Logger logger = LoggerFactory.getLogger(CategoriaServiceImpl.class);

    @Autowired
    private CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(BaseRepository<Categoria, Long> baseRepository, CategoriaRepository categoriaRepository) {
        super(baseRepository);
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public boolean bajaLogica(Long id) {
        logger.info("[BAJA-LOGICA] Iniciando proceso de baja lógica para categoría ID: {}", id);
        
        try {
            // Paso 1: Verificar que la categoría existe
            logger.info("[VERIFICACION] Buscando categoría con ID: {}", id);
            Categoria categoria = categoriaRepository.findByIdAndNotDeleted(id);
            
            if (categoria == null) {
                logger.error("[ERROR] Categoría con ID {} no encontrada o ya está dada de baja", id);
                throw new RuntimeException("La categoría no existe o ya está dada de baja");
            }
            
            logger.info("[OK] Categoría encontrada: {}", categoria.getNombreCategoria());
            
            // Paso 2: Verificar que no tenga muebles asociados
            logger.info("[VERIFICACION] Verificando muebles asociados a categoría ID: {}", id);
            long mueblesAsociados = categoriaRepository.countMueblesByCategoria(id);
            
            if (mueblesAsociados > 0) {
                logger.error("[ERROR] La categoría tiene {} muebles asociados. No se puede dar de baja", mueblesAsociados);
                throw new RuntimeException("No se puede dar de baja la categoría porque tiene " + mueblesAsociados + " muebles asociados");
            }
            
            logger.info("[OK] No hay muebles asociados a la categoría");
            
            // Paso 3: Realizar la baja lógica
            logger.info("[BAJA-LOGICA] Estableciendo fecha de baja para categoría: {}", categoria.getNombreCategoria());
            Date fechaBaja = new Date();
            categoria.setFechaBajaCategoria(fechaBaja);
            
            // Paso 4: Guardar los cambios
            logger.info("[GUARDANDO] Persistiendo baja lógica en base de datos");
            categoriaRepository.save(categoria);
            
            logger.info("[EXITO] Baja lógica completada exitosamente para categoría: {} en fecha: {}", 
                      categoria.getNombreCategoria(), fechaBaja);
            
            return true;
            
        } catch (Exception e) {
            logger.error("[ERROR] Error durante el proceso de baja lógica: {}", e.getMessage());
            throw new RuntimeException("Error al realizar la baja lógica: " + e.getMessage(), e);
        }
    }

    @Override
    public Categoria crearCategoria(Categoria categoria) {
        logger.info("\n=== [CREAR-CATEGORIA] INICIANDO CREACIÓN ===");
        logger.info("Nombre de categoría a crear: '{}'", categoria.getNombreCategoria());
        
        // Validar que el nombre no esté vacío
        if (categoria.getNombreCategoria() == null || categoria.getNombreCategoria().trim().isEmpty()) {
            logger.error("❌ Error: El nombre de la categoría no puede estar vacío");
            throw new RuntimeException("El nombre de la categoría es requerido");
        }
        
        // Verificar si ya existe una categoría ACTIVA con el mismo nombre
        boolean existeActiva = categoriaRepository.existsByNombreCategoriaAndNotDeleted(categoria.getNombreCategoria());
        logger.info("¿Existe una categoría ACTIVA con el nombre '{}'? {}", categoria.getNombreCategoria(), existeActiva);
        
        if (existeActiva) {
            logger.error("❌ Error: Ya existe una categoría ACTIVA con el nombre: {}", categoria.getNombreCategoria());
            throw new RuntimeException("Ya existe una categoría activa con el nombre: " + categoria.getNombreCategoria());
        }
        
        // Verificar si existe una categoría dada de baja con el mismo nombre (para logging)
        boolean existeEnGeneral = categoriaRepository.existsByNombreCategoria(categoria.getNombreCategoria());
        if (existeEnGeneral && !existeActiva) {
            logger.info("ℹ️ [INFO] Existe una categoría dada de baja con el nombre '{}', pero se permite crear una nueva", categoria.getNombreCategoria());
        }
        
        // Establecer fecha de alta automáticamente
        categoria.setFechaAltaCategoria(new Date());
        logger.info("📅 Fecha de alta establecida: {}", categoria.getFechaAltaCategoria());
        
        // Guardar la categoría
        Categoria categoriaGuardada = categoriaRepository.save(categoria);
        logger.info("✅ Categoría creada exitosamente con ID: {} y nombre: '{}'", 
                   categoriaGuardada.getId(), categoriaGuardada.getNombreCategoria());
        
        return categoriaGuardada;
    }
    
    @Override
    public Categoria modificarCategoria(Long id, Categoria categoria) {
        logger.info("\n=== [SERVICIO-MODIFICAR] INICIANDO MODIFICACIÓN ===");
        logger.info("ID: {}", id);
        logger.info("Datos recibidos: {}", categoria);
        
        // Verificar que la categoría exista
        Optional<Categoria> categoriaExistente = categoriaRepository.findById(id);
        if (categoriaExistente.isEmpty()) {
            logger.error("❌ Error: No se encontró la categoría con ID: {}", id);
            throw new RuntimeException("Categoría no encontrada");
        }
        
        Categoria categoriaActual = categoriaExistente.get();
        logger.info("Categoría encontrada: {}", categoriaActual);
        
        // Verificar que no esté dada de baja
        if (categoriaActual.getFechaBajaCategoria() != null) {
            logger.error("❌ Error: No se puede modificar una categoría dada de baja. ID: {}", id);
            throw new RuntimeException("No se puede modificar una categoría dada de baja");
        }
        
        // Validar que el nombre no esté vacío
        if (categoria.getNombreCategoria() == null || categoria.getNombreCategoria().trim().isEmpty()) {
            logger.error("❌ Error: El nombre de la categoría no puede estar vacío");
            throw new RuntimeException("El nombre de la categoría es requerido");
        }
        
        // Verificar si ya existe otra categoría ACTIVA con el mismo nombre
        boolean existeOtraActiva = categoriaRepository.existsByNombreCategoriaAndIdNotAndNotDeleted(categoria.getNombreCategoria(), id);
        logger.info("¿Existe otra categoría ACTIVA con el nombre '{}'? {}", categoria.getNombreCategoria(), existeOtraActiva);
        
        if (existeOtraActiva) {
            logger.error("❌ Error: Ya existe otra categoría ACTIVA con el nombre: {}", categoria.getNombreCategoria());
            throw new RuntimeException("Ya existe otra categoría activa con el nombre: " + categoria.getNombreCategoria());
        }
        
        // Actualizar los campos
        categoriaActual.setNombreCategoria(categoria.getNombreCategoria());
        
        // Establecer fecha de modificación automáticamente
        categoriaActual.setFechaModificacionCategoria(new Date());
        logger.info("Fecha de modificación establecida: {}", categoriaActual.getFechaModificacionCategoria());
        
        // Guardar la categoría actualizada
        Categoria categoriaActualizada = categoriaRepository.save(categoriaActual);
        logger.info("✅ Categoría modificada exitosamente: {}", categoriaActualizada);
        
        return categoriaActualizada;
    }
    
    @Override
    public Categoria updateCategoria(Long id, Categoria categoria) {
        return modificarCategoria(id, categoria);
    }

    @Override
    public ResponseEntity<?> obtenerCategoriasFormateadas() {
        logger.info("[OBTENER-CATEGORIAS] Iniciando proceso para obtener categorías formateadas");
        
        try {
            // Obtener todas las categorías activas (no dadas de baja)
            List<Categoria> categorias = categoriaRepository.findAll().stream()
                .filter(categoria -> categoria.getFechaBajaCategoria() == null)
                .collect(Collectors.toList());
            
            logger.info("[PROCESANDO] Encontradas {} categorías activas", categorias.size());
            
            // Formatear las categorías para el frontend
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            List<Map<String, Object>> categoriasFormateadas = categorias.stream()
                .map(categoria -> {
                    Map<String, Object> categoriaMap = new HashMap<>();
                    categoriaMap.put("id", categoria.getId());
                    categoriaMap.put("nombreCategoria", categoria.getNombreCategoria());
                    
                    // Formatear fecha de alta
                    if (categoria.getFechaAltaCategoria() != null) {
                        categoriaMap.put("fechaAltaCategoria", formatter.format(categoria.getFechaAltaCategoria()));
                    } else {
                        categoriaMap.put("fechaAltaCategoria", null);
                    }
                    
                    // Formatear fecha de modificación
                    if (categoria.getFechaModificacionCategoria() != null) {
                        categoriaMap.put("fechaModificacionCategoria", formatter.format(categoria.getFechaModificacionCategoria()));
                    } else {
                        categoriaMap.put("fechaModificacionCategoria", null);
                    }
                    
                    // Fecha de baja siempre será null para categorías activas
                    categoriaMap.put("fechaBajaCategoria", null);
                    
                    return categoriaMap;
                })
                .collect(Collectors.toList());
            
            logger.info("[EXITO] Categorías formateadas exitosamente. Total: {}", categoriasFormateadas.size());
            
            return ResponseEntity.ok(categoriasFormateadas);
            
        } catch (Exception e) {
            logger.error("[ERROR] Error al obtener categorías formateadas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Error interno al obtener las categorías\"}");
        }
    }

    @Override
    public ResponseEntity<?> obtenerCategoriasDadasDeBaja() {
        logger.info("\n=== [OBTENER-CATEGORIAS-BAJA] INICIANDO PROCESO ===");
        logger.info("Obteniendo todas las categorías dadas de baja");
        
        try {
            // Obtener todas las categorías que están dadas de baja
            List<Categoria> categoriasBaja = categoriaRepository.findAll().stream()
                .filter(categoria -> categoria.getFechaBajaCategoria() != null)
                .collect(Collectors.toList());
            
            logger.info("📋 [PROCESANDO] Encontradas {} categorías dadas de baja", categoriasBaja.size());
            
            // Formatear las categorías para el frontend
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            List<Map<String, Object>> categoriasBajaFormateadas = categoriasBaja.stream()
                .map(categoria -> {
                    Map<String, Object> categoriaMap = new HashMap<>();
                    categoriaMap.put("id", categoria.getId());
                    categoriaMap.put("nombreCategoria", categoria.getNombreCategoria());
                    
                    // Formatear fecha de alta
                    if (categoria.getFechaAltaCategoria() != null) {
                        categoriaMap.put("fechaAltaCategoria", formatter.format(categoria.getFechaAltaCategoria()));
                    } else {
                        categoriaMap.put("fechaAltaCategoria", null);
                    }
                    
                    // Formatear fecha de modificación
                    if (categoria.getFechaModificacionCategoria() != null) {
                        categoriaMap.put("fechaModificacionCategoria", formatter.format(categoria.getFechaModificacionCategoria()));
                    } else {
                        categoriaMap.put("fechaModificacionCategoria", null);
                    }
                    
                    // Formatear fecha de baja (siempre presente para categorías dadas de baja)
                    if (categoria.getFechaBajaCategoria() != null) {
                        categoriaMap.put("fechaBajaCategoria", formatter.format(categoria.getFechaBajaCategoria()));
                    } else {
                        categoriaMap.put("fechaBajaCategoria", null);
                    }
                    
                    return categoriaMap;
                })
                .collect(Collectors.toList());
            
            logger.info("✅ [EXITO] Categorías dadas de baja formateadas exitosamente. Total: {}", categoriasBajaFormateadas.size());
            
            return ResponseEntity.ok(categoriasBajaFormateadas);
            
        } catch (Exception e) {
            logger.error("❌ [ERROR] Error al obtener categorías dadas de baja: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Error interno al obtener las categorías dadas de baja\"}");
        }
    }
}
