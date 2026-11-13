package com.Capinteria.carpinteria.Service;

    import com.Capinteria.carpinteria.DTO.CatalogoMuebleDTO;
    import com.Capinteria.carpinteria.Entity.Categoria;
    import com.Capinteria.carpinteria.Entity.Mueble;
    import com.Capinteria.carpinteria.Entity.MuebleImagenes;
    import com.Capinteria.carpinteria.Repositories.BaseRepository;
    import com.Capinteria.carpinteria.Repositories.CategoriaRepository;
    import com.Capinteria.carpinteria.Repositories.ConsultaRepository;
    import com.Capinteria.carpinteria.Repositories.MuebleImagenesRepository;
    import com.Capinteria.carpinteria.Repositories.MuebleRepository;
    import com.Capinteria.carpinteria.Util.Miniaturas;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.data.domain.Pageable;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import org.springframework.web.multipart.MultipartFile;

    import java.io.IOException;
    import java.time.LocalDateTime;
    import java.time.format.DateTimeFormatter;
    import java.util.ArrayList;
    import java.util.Collection;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.Optional;
    import java.util.stream.Collectors;

    @Service
    // Ver nota en BaseSeriviceImpl: warnings de nulabilidad por generics sin @NonNull.
    @SuppressWarnings("null")
    public class MuebleServiceImpl extends BaseSeriviceImpl<Mueble, Long> implements MuebleService {

        private static final Logger logger = LoggerFactory.getLogger(MuebleServiceImpl.class);
        private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB
        private final MuebleRepository muebleRepository;
        private final CategoriaRepository categoriaRepository;
        private final MuebleImagenesServiceImpl muebleImagenesService;
        private final ObjectMapper objectMapper;
        private final ConsultaRepository consultaRepository;
        private final MuebleImagenesRepository muebleImagenesRepository;

        @Autowired
        public MuebleServiceImpl(BaseRepository<Mueble, Long> baseRepository, MuebleRepository muebleRepository,
                                CategoriaRepository categoriaRepository, MuebleImagenesServiceImpl muebleImagenesService, ObjectMapper objectMapper,
                                ConsultaRepository consultaRepository, MuebleImagenesRepository muebleImagenesRepository) {
            super(baseRepository);
            this.muebleRepository = muebleRepository;
            this.categoriaRepository = categoriaRepository;
            this.muebleImagenesService = muebleImagenesService;
            this.objectMapper = objectMapper;
            this.consultaRepository = consultaRepository;
            this.muebleImagenesRepository = muebleImagenesRepository;
        }

        @Override
        public Page<Mueble> getFirst9Muebles(Pageable pageable) {
            return muebleRepository.findFirst9ByOrderByNombreMuebleAsc(pageable);
        }

        @Override
        public ResponseEntity<String> createMueble(String muebleJson, Long categoriaId, List<MultipartFile> files, Integer portadaIndex) {
            logger.info("\n=== [CREAR-MUEBLE] INICIANDO PROCESO DE CREACIÓN ===");
            logger.info("Categoría ID: {}", categoriaId);
            logger.info("Número de archivos: {}", files != null ? files.size() : 0);
            
            try {
                // Paso 1: Deserializar el JSON del mueble
                logger.info("[DESERIALIZACION] Convirtiendo JSON a objeto Mueble");
                Mueble mueble = objectMapper.readValue(muebleJson, Mueble.class);

                if (mueble == null) {
                    logger.error("❌ [ERROR] El objeto Mueble no puede ser nulo");
                    return ResponseEntity.badRequest().body("El objeto Mueble no puede ser nulo");
                }
                
                logger.info("[MUEBLE] Nombre: {}, Color: {}",
                        mueble.getNombreMueble(), mueble.getColorMueble());

                // Paso 2: Buscar y asignar la categoría
                logger.info("[CATEGORIA] Buscando categoría con ID: {}", categoriaId);
                Optional<Categoria> categoriaOpt = categoriaRepository.findById(categoriaId);
                
                if (categoriaOpt.isEmpty()) {
                    logger.error("❌ [ERROR] No se encontró la categoría con ID: {}", categoriaId);
                    return ResponseEntity.badRequest().body("No se encontró la categoría con ID: " + categoriaId);
                }
                
                Categoria categoria = categoriaOpt.get();
                
                // Verificar que la categoría no esté dada de baja
                if (categoria.getFechaBajaCategoria() != null) {
                    logger.error("❌ [ERROR] La categoría '{}' está dada de baja", categoria.getNombreCategoria());
                    return ResponseEntity.badRequest().body("La categoría '" + categoria.getNombreCategoria() + "' está dada de baja");
                }
                
                mueble.setCategoria(categoria);
                logger.info("✅ [CATEGORIA] Categoría '{}' asignada al mueble", categoria.getNombreCategoria());

                // Paso 3: Establecer fecha de alta automáticamente
                String fechaAlta = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                mueble.setFechaAltaMueble(fechaAlta);
                logger.info("📅 [FECHA-ALTA] Establecida: {}", fechaAlta);

                // Paso 4: Validar que se proporcionó al menos una imagen
                if (files == null || files.isEmpty()) {
                    logger.error("❌ [ERROR] Es obligatorio proporcionar al menos una imagen para crear un mueble");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\":\"Es obligatorio proporcionar al menos una imagen para crear un mueble\"}");
                }
                
                logger.info("[IMAGENES] Procesando {} archivos de imagen", files.size());
                
                // Validar tamaño de archivos
                for (int i = 0; i < files.size(); i++) {
                    MultipartFile file = files.get(i);
                    
                    if (file.getSize() > MAX_FILE_SIZE_BYTES) {
                        logger.error("❌ [ERROR] Archivo {} excede el tamaño máximo permitido", file.getOriginalFilename());
                        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                            .body("{\"error\":\"El archivo '" + file.getOriginalFilename() + "' excede el tamaño máximo permitido\"}");
                    }
                }

                // Paso 5: Guardar el mueble
                logger.info("[GUARDANDO] Persistiendo mueble en base de datos");
                Mueble savedMueble = save(mueble);
                logger.info("✅ [GUARDADO] Mueble guardado con ID: {}", savedMueble.getId());

                // Paso 6: Procesar imágenes (ahora siempre hay al menos una)
                logger.info("[PROCESANDO-IMAGENES] Guardando {} imágenes", files.size());
                
                for (int i = 0; i < files.size(); i++) {
                    MultipartFile file = files.get(i);
                    
                    byte[] bytes = file.getBytes();
                    MuebleImagenes imagen = new MuebleImagenes();
                    imagen.setImagenes(bytes);
                    imagen.setMiniatura(Miniaturas.generar(bytes));
                    
                    // Determinar si es portada
                    boolean esPortada = portadaIndex != null && i == portadaIndex;
                    imagen.setEsPortada(esPortada);
                    
                    logger.info("[IMAGEN] Procesando archivo '{}' - Es portada: {}", 
                            file.getOriginalFilename(), esPortada);

                    savedMueble.getImagenes().add(imagen);
                    muebleImagenesService.save(imagen);
                }
                
                logger.info("✅ [IMAGENES] Todas las imágenes procesadas correctamente");

                logger.info("🎉 [EXITO] Mueble '{}' creado exitosamente con ID: {} y categoría: '{}'", 
                        savedMueble.getNombreMueble(), savedMueble.getId(), categoria.getNombreCategoria());
                
                return ResponseEntity.ok("Mueble y imágenes asociadas creadas correctamente");
                
            } catch (IOException e) {
                logger.error("❌ [ERROR] Error al cargar las imágenes: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cargar las imágenes: " + e.getMessage());
            } catch (Exception e) {
                logger.error("❌ [ERROR] Error inesperado al crear mueble: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado al crear mueble: " + e.getMessage());
            }
        }

        public ResponseEntity<?> updateMuebleImages(Long id, MultipartFile[] files, Integer portadaIndex) {
            try {
                Mueble mueble = findById(id);

                if (mueble != null) {
                    mueble.getImagenes().clear();
                    addImagesToMueble(mueble, files, portadaIndex);
                    save(mueble);
                    return ResponseEntity.ok("Imágenes actualizadas correctamente");
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontró el mueble con el ID proporcionado");
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar las imágenes del mueble: " + e.getMessage());
            }
        }

        public ResponseEntity<?> updateAllMueblesImages(MultipartFile[] files, Integer portadaIndex) {
            try {
                List<Mueble> muebles = findAll();

                if (!muebles.isEmpty()) {
                    for (Mueble mueble : muebles) {
                        mueble.getImagenes().clear();
                        addImagesToMueble(mueble, files, portadaIndex);
                        save(mueble);
                    }
                    return ResponseEntity.ok("Imágenes actualizadas correctamente en todos los muebles");
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontraron muebles para actualizar");
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar las imágenes de los muebles: " + e.getMessage());
            }
        }

        private void addImagesToMueble(Mueble mueble, MultipartFile[] files, Integer portadaIndex) throws IOException {
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                byte[] bytes = file.getBytes();
                MuebleImagenes imagenes = new MuebleImagenes();
                imagenes.setImagenes(bytes);
                imagenes.setMiniatura(Miniaturas.generar(bytes));
                imagenes.setEsPortada(portadaIndex != null && i == portadaIndex);
                mueble.getImagenes().add(imagenes);
            }
        }

        public ResponseEntity<?> getMueblesPaginated(int pageNumber) {
            try {
                Pageable pageable = PageRequest.of(pageNumber, 12);
                Page<Mueble> mueblesPage = findAll(pageable);
                return ResponseEntity.ok(mueblesPage);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        }
        
        public ResponseEntity<?> obtenerTodosMuebles(int pageNumber) {
            logger.info("\n=== [OBTENER-TODOS-MUEBLES] INICIANDO CONSULTA PARA TODOS LOS MUEBLES ACTIVOS ===");
            logger.info("Página solicitada: {}", pageNumber);
            logger.info("Filtros aplicados: Solo muebles ACTIVOS (fecha_baja IS NULL)");
            
            try {
                Pageable pageable = PageRequest.of(pageNumber, 12);
                Page<Object[]> resultados = muebleRepository.findCatalogoMueblesOptimized(pageable);
                
                logger.info("✅ [CONSULTA-DB] Consulta ejecutada - {} muebles activos encontrados", 
                        resultados.getTotalElements());
                
                // Convertir los resultados a DTOs
                List<CatalogoMuebleDTO> catalogo = resultados.getContent().stream()
                    .map(row -> {
                        CatalogoMuebleDTO dto = new CatalogoMuebleDTO();
                        dto.setId(((Number) row[0]).longValue());
                        dto.setNombreMueble((String) row[1]);
                        dto.setColorMueble((String) row[2]);
                        dto.setDescripcion((String) row[3]);
                        dto.setFechaAltaMueble((String) row[4]);
                        dto.setFechaModificacionMueble((String) row[5]);
                        dto.setNombreCategoria((String) row[6]);

                        // Convertir imagen a Base64 si existe
                        if (row[7] != null) {
                            byte[] imagen = (byte[]) row[7];
                            dto.setImagenPortada(java.util.Base64.getEncoder().encodeToString(imagen));
                            logger.debug("Imagen portada convertida para mueble ID: {}", dto.getId());
                        } else {
                            logger.debug("Mueble ID: {} no tiene imagen de portada", dto.getId());
                        }
                        
                        return dto;
                    })
                    .collect(Collectors.toList());
                
                // Crear respuesta paginada
                Map<String, Object> response = new HashMap<>();
                response.put("content", catalogo);
                response.put("totalElements", resultados.getTotalElements());
                response.put("totalPages", resultados.getTotalPages());
                response.put("number", resultados.getNumber());
                response.put("size", resultados.getSize());
                response.put("numberOfElements", resultados.getNumberOfElements());
                response.put("first", resultados.isFirst());
                response.put("last", resultados.isLast());
                
                logger.info("✅ [OBTENER-TODOS-MUEBLES] Procesados {} muebles ACTIVOS para página {}", 
                        catalogo.size(), pageNumber);
                logger.info("📊 [ESTADISTICAS] Total muebles activos: {}, Páginas totales: {}", 
                        resultados.getTotalElements(), resultados.getTotalPages());
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                logger.error("❌ [OBTENER-TODOS-MUEBLES] Error al obtener todos los muebles: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error interno al cargar todos los muebles\"}");
            }
        }
        
        public ResponseEntity<?> getCatalogoMueblesPorCategoria(int pageNumber, Long categoriaId) {
            logger.info("\n=== [CATALOGO-POR-CATEGORIA] INICIANDO CONSULTA COMPLETA CON OBJETOS MUEBLE ===");
            logger.info("Página solicitada: {}", pageNumber);
            logger.info("Categoría ID: {}", categoriaId);
            logger.info("Filtros aplicados: Solo muebles ACTIVOS de categoría específica");
            logger.info("Respuesta: Objetos Mueble completos con imagen de portada");
            
            try {
                // Verificar que la categoría existe y está activa
                Optional<Categoria> categoriaOpt = categoriaRepository.findById(categoriaId);
                if (categoriaOpt.isEmpty()) {
                    logger.error("❌ [ERROR] Categoría con ID {} no encontrada", categoriaId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"Categoría no encontrada\"}");
                }
                
                Categoria categoria = categoriaOpt.get();
                if (categoria.getFechaBajaCategoria() != null) {
                    logger.error("❌ [ERROR] Categoría '{}' está dada de baja", categoria.getNombreCategoria());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\":\"La categoría está dada de baja\"}");
                }
                
                logger.info("✅ [CATEGORIA-VALIDA] Categoría encontrada: '{}'", categoria.getNombreCategoria());
                
                Pageable pageable = PageRequest.of(pageNumber, 12);
                
                // Obtener muebles activos de la categoría específica con entidades completas
                Page<Mueble> mueblePage = muebleRepository.findByCategoriaIdAndFechaBajaMuebleIsNull(categoriaId, pageable);
                
                logger.info("✅ [CONSULTA-DB] Consulta ejecutada - {} muebles activos encontrados para categoría '{}'", 
                        mueblePage.getTotalElements(), categoria.getNombreCategoria());
                
                Map<Long, Object[]> portadas = portadasDe(mueblePage.getContent());
                // Convertir a objetos Mueble completos con imagen de portada
                List<Map<String, Object>> mueblesCompletos = mueblePage.getContent().stream()
                    .map(mueble -> {
                        Map<String, Object> muebleCompleto = new HashMap<>();
                        
                        // Todos los atributos del mueble
                        muebleCompleto.put("id", mueble.getId());
                        muebleCompleto.put("nombreMueble", mueble.getNombreMueble());
                        muebleCompleto.put("colorMueble", mueble.getColorMueble());
                        muebleCompleto.put("descripcion", mueble.getDescripcion());
                        muebleCompleto.put("fechaAltaMueble", mueble.getFechaAltaMueble());
                        muebleCompleto.put("fechaModificacionMueble", mueble.getFechaModificacionMueble());
                        muebleCompleto.put("fechaBajaMueble", mueble.getFechaBajaMueble());
                        
                        // Información de la categoría
                        if (mueble.getCategoria() != null) {
                            Map<String, Object> categoriaInfo = new HashMap<>();
                            categoriaInfo.put("id", mueble.getCategoria().getId());
                            categoriaInfo.put("nombreCategoria", mueble.getCategoria().getNombreCategoria());
                            muebleCompleto.put("categoria", categoriaInfo);
                        }
                        
                        // Solo la imagen de portada (miniatura convertida a Base64)
                        Object[] portada = portadas.get(mueble.getId());
                        if (portada != null) {
                            Map<String, Object> imagenPortadaInfo = new HashMap<>();
                            imagenPortadaInfo.put("id", ((Number) portada[1]).longValue());
                            imagenPortadaInfo.put("imagenes", portadaBase64(portada));
                            imagenPortadaInfo.put("esPortada", Boolean.TRUE.equals(portada[2]));
                            muebleCompleto.put("imagenPortada", imagenPortadaInfo);
                        } else {
                            muebleCompleto.put("imagenPortada", null);
                        }

                        logger.debug("Procesado mueble completo ID: {} - {}", mueble.getId(), mueble.getNombreMueble());
                        
                        return muebleCompleto;
                    })
                    .collect(Collectors.toList());
                
                // Crear respuesta paginada
                Map<String, Object> response = new HashMap<>();
                response.put("content", mueblesCompletos);
                response.put("totalElements", mueblePage.getTotalElements());
                response.put("totalPages", mueblePage.getTotalPages());
                response.put("number", mueblePage.getNumber());
                response.put("size", mueblePage.getSize());
                response.put("numberOfElements", mueblePage.getNumberOfElements());
                response.put("first", mueblePage.isFirst());
                response.put("last", mueblePage.isLast());
                response.put("categoriaId", categoriaId);
                response.put("categoriaNombre", categoria.getNombreCategoria());
                
                logger.info("✅ [CATALOGO-POR-CATEGORIA] Procesados {} muebles COMPLETOS para página {} de categoría '{}'", 
                        mueblesCompletos.size(), pageNumber, categoria.getNombreCategoria());
                logger.info("📊 [ESTADISTICAS] Total muebles activos en categoría: {}, Páginas totales: {}", 
                        mueblePage.getTotalElements(), mueblePage.getTotalPages());
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                logger.error("❌ [CATALOGO-POR-CATEGORIA] Error al obtener catálogo completo por categoría: {}", e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error interno al cargar el catálogo por categoría\"}");
            }
        }

        @Override
        @Transactional
        public void eliminarDefinitivamente(Long id) {
            Mueble mueble = muebleRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("El mueble no existe"));
            if (mueble.getFechaBajaMueble() == null || mueble.getFechaBajaMueble().isEmpty()) {
                throw new RuntimeException("Solo se puede eliminar definitivamente un mueble que ya fue dado de baja");
            }
            int consultas = consultaRepository.eliminarPorMueble(id);
            int imagenes = muebleImagenesRepository.eliminarPorMueble(id);
            muebleRepository.delete(mueble);
            logger.info("[ELIMINAR-MUEBLE] Mueble {} eliminado definitivamente ({} consultas y {} imagenes borradas)", id, consultas, imagenes);
        }

        @Override
        public boolean bajaLogica(Long id) {
            logger.info("[BAJA-LOGICA-MUEBLE] Iniciando proceso de baja lógica para mueble ID: {}", id);
            
            try {
                // Paso 1: Verificar que el mueble existe y no está dado de baja
                logger.info("[VERIFICACION] Buscando mueble con ID: {}", id);
                Mueble mueble = muebleRepository.findByIdAndNotDeleted(id);
                
                if (mueble == null) {
                    logger.error("[ERROR] Mueble con ID {} no encontrado o ya está dado de baja", id);
                    throw new RuntimeException("El mueble no existe o ya está dado de baja");
                }
                
                logger.info("[OK] Mueble encontrado: {} - {}", mueble.getNombreMueble(), mueble.getDescripcion());
                
                // Paso 2: Verificar que no esté ya dado de baja (validación adicional)
                if (mueble.getFechaBajaMueble() != null && !mueble.getFechaBajaMueble().isEmpty()) {
                    logger.error("[ERROR] El mueble ya está dado de baja desde: {}", mueble.getFechaBajaMueble());
                    throw new RuntimeException("El mueble ya está dado de baja");
                }
                
                logger.info("[OK] El mueble está activo y puede ser dado de baja");
                
                // Paso 3: Realizar la baja lógica
                String fechaBaja = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                logger.info("[BAJA-LOGICA] Estableciendo fecha de baja: {} para mueble: {}", fechaBaja, mueble.getNombreMueble());
                mueble.setFechaBajaMueble(fechaBaja);
                
                // Paso 4: Guardar los cambios
                logger.info("[GUARDANDO] Persistiendo baja lógica en base de datos");
                muebleRepository.save(mueble);
                
                logger.info("[EXITO] Baja lógica completada exitosamente para mueble: {} en fecha: {}", 
                        mueble.getNombreMueble(), fechaBaja);
                
                return true;
                
            } catch (Exception e) {
                logger.error("[ERROR] Error durante el proceso de baja lógica del mueble: {}", e.getMessage());
                throw new RuntimeException("Error al realizar la baja lógica del mueble: " + e.getMessage(), e);
            }
        }

        @Override
        public ResponseEntity<?> obtenerImagenesMueble(Long muebleId) {
            logger.info("[OBTENER-IMAGENES] Iniciando proceso para obtener imágenes del mueble ID: {}", muebleId);
            
            try {
                // Paso 1: Verificar que el mueble existe
                logger.info("[VERIFICACION] Buscando mueble con ID: {}", muebleId);
                Mueble mueble = muebleRepository.findById(muebleId).orElse(null);
                
                if (mueble == null) {
                    logger.error("[ERROR] Mueble con ID {} no encontrado", muebleId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"Mueble no encontrado\"}");
                }
                
                logger.info("[OK] Mueble encontrado: {}", mueble.getNombreMueble());
                
                // Paso 2: Verificar que el mueble no esté dado de baja
                if (mueble.getFechaBajaMueble() != null && !mueble.getFechaBajaMueble().isEmpty()) {
                    logger.error("[ERROR] El mueble está dado de baja desde: {}", mueble.getFechaBajaMueble());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\":\"El mueble está dado de baja\"}");
                }
                
                // Paso 3: Obtener y procesar las imágenes
                List<MuebleImagenes> imagenes = mueble.getImagenes();
                logger.info("[PROCESANDO] Procesando {} imágenes del mueble", imagenes.size());
                
                if (imagenes.isEmpty()) {
                    logger.info("[INFO] El mueble no tiene imágenes asociadas");
                }

                // Siempre devuelve un array (vacío si no tiene imágenes), para que el
                // frontend pueda hacer .map()/.find() sin chequear la forma de la respuesta.
                return ResponseEntity.ok(imagenes);
                
            } catch (Exception e) {
                logger.error("[ERROR] Error al obtener imágenes del mueble ID {}: {}", muebleId, e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error interno al obtener las imágenes\"}");
            }
        }

        /** Datos del mueble sin fotos, para la ficha publica (las fotos se piden aparte, de a una). */
        public ResponseEntity<?> obtenerFicha(Long muebleId) {
            Mueble mueble = muebleRepository.findById(muebleId).orElse(null);
            if (mueble == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Mueble no encontrado\"}");
            }
            Map<String, Object> ficha = new HashMap<>();
            ficha.put("id", mueble.getId());
            ficha.put("nombreMueble", mueble.getNombreMueble());
            ficha.put("colorMueble", mueble.getColorMueble());
            ficha.put("descripcion", mueble.getDescripcion());
            ficha.put("fechaBajaMueble", mueble.getFechaBajaMueble());
            return ResponseEntity.ok(ficha);
        }

        /** Lista liviana de las fotos de un mueble: solo ids y cual es la portada. */
        public ResponseEntity<?> obtenerGaleria(Long muebleId) {
            List<Map<String, Object>> galeria = new ArrayList<>();
            for (Object[] fila : muebleImagenesRepository.findGaleria(muebleId)) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", ((Number) fila[0]).longValue());
                item.put("esPortada", Boolean.TRUE.equals(fila[1]));
                galeria.add(item);
            }
            return ResponseEntity.ok(galeria);
        }

        /** Bytes de una foto (original o miniatura) con su tipo y cache: una foto nunca cambia bajo el mismo id. */
        public ResponseEntity<byte[]> obtenerArchivoImagen(Long imagenId, boolean miniatura) {
            byte[] bytes = miniatura
                ? muebleImagenesRepository.findMiniaturaOUnOriginal(imagenId)
                : muebleImagenesRepository.findOriginal(imagenId);
            if (bytes == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                .contentType(tipoDeImagen(bytes))
                .cacheControl(org.springframework.http.CacheControl.maxAge(java.time.Duration.ofDays(365)).cachePublic().immutable())
                .body(bytes);
        }

        private static org.springframework.http.MediaType tipoDeImagen(byte[] b) {
            if (b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8) return org.springframework.http.MediaType.IMAGE_JPEG;
            if (b.length > 3 && (b[0] & 0xFF) == 0x89 && b[1] == 'P') return org.springframework.http.MediaType.IMAGE_PNG;
            if (b.length > 3 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F') return org.springframework.http.MediaType.IMAGE_GIF;
            if (b.length > 12 && b[0] == 'R' && b[1] == 'I' && b[8] == 'W' && b[9] == 'E') return org.springframework.http.MediaType.valueOf("image/webp");
            return org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
        }

        @Override
        public ResponseEntity<?> modificarMueble(Long id, Mueble muebleActualizado) {
            logger.info("[MODIFICAR-MUEBLE] Iniciando proceso de modificación para mueble ID: {}", id);
            
            try {
                // Paso 1: Verificar que el mueble existe y no está dado de baja
                logger.info("[VERIFICACION] Buscando mueble con ID: {}", id);
                Mueble muebleExistente = muebleRepository.findByIdAndNotDeleted(id);
                
                if (muebleExistente == null) {
                    logger.error("[ERROR] Mueble con ID {} no encontrado o está dado de baja", id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"Mueble no encontrado o está dado de baja\"}");
                }
                
                logger.info("[OK] Mueble encontrado: {}", muebleExistente.getNombreMueble());
                
                // Paso 2: Actualizar campos
                muebleExistente.setNombreMueble(muebleActualizado.getNombreMueble());
                muebleExistente.setColorMueble(muebleActualizado.getColorMueble());
                muebleExistente.setDescripcion(muebleActualizado.getDescripcion());
                muebleExistente.setLinkInstagram(muebleActualizado.getLinkInstagram());
                muebleExistente.setLinkFacebook(muebleActualizado.getLinkFacebook());

                // Establecer fecha de modificación
                String fechaModificacion = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                muebleExistente.setFechaModificacionMueble(fechaModificacion);
                
                logger.info("[FECHA-MODIFICACION] Estableciendo fecha de modificación: {}", fechaModificacion);
                
                // Paso 3: Guardar los cambios
                Mueble muebleGuardado = muebleRepository.save(muebleExistente);
                
                logger.info("[EXITO] Mueble '{}' modificado exitosamente", muebleGuardado.getNombreMueble());
                
                return ResponseEntity.ok(muebleGuardado);
                
            } catch (Exception e) {
                logger.error("[ERROR] Error durante la modificación del mueble ID {}: {}", id, e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error interno al modificar el mueble\"}");
            }
        }

        @Override
        public ResponseEntity<?> filtrarPorNombreOColor(String filtro, int pageNumber) {
            logger.info("\n=== [FILTRAR-NOMBRE-O-COLOR] INICIANDO FILTRADO OPTIMIZADO CON PAGINACIÓN ===");
            logger.info("Filtro general: '{}'", filtro);
            logger.info("Página solicitada: {}", pageNumber);
            logger.info("Respuesta: Objetos con solo imagen de portada");
            
            try {
                if (filtro == null || filtro.trim().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\":\"El filtro no puede estar vacío\"}");
                }
                
                // Configurar paginación (10 elementos por página)
                Pageable pageable = PageRequest.of(pageNumber, 10);
                
                // Usar consulta optimizada que devuelve solo imagen de portada
                Page<Object[]> resultados = muebleRepository.findCatalogoMueblesByFiltroOptimized(filtro.trim(), pageable);
                
                logger.info("Encontrados {} muebles en total que contienen '{}' en nombre o color", 
                    resultados.getTotalElements(), filtro);
                logger.info("Mostrando página {} de {} con {} elementos", 
                    pageNumber + 1, resultados.getTotalPages(), resultados.getNumberOfElements());
                
                // Convertir Object[] a CatalogoMuebleDTO
                List<CatalogoMuebleDTO> catalogoMuebles = resultados.getContent().stream()
                    .map(result -> {
                        try {
                            // Formatear fechas - manejar tanto String como LocalDateTime
                            String fechaAlta = null;
                            if (result[4] != null) {
                                if (result[4] instanceof LocalDateTime) {
                                    fechaAlta = ((LocalDateTime) result[4]).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                                } else {
                                    fechaAlta = result[4].toString();
                                }
                            }

                            String fechaModificacion = null;
                            if (result[5] != null) {
                                if (result[5] instanceof LocalDateTime) {
                                    fechaModificacion = ((LocalDateTime) result[5]).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                                } else {
                                    fechaModificacion = result[5].toString();
                                }
                            }

                            // Convertir imagen a Base64 si existe
                            String imagenPortadaBase64 = null;
                            if (result[7] != null) {
                                byte[] imagenBytes = (byte[]) result[7];
                                imagenPortadaBase64 = java.util.Base64.getEncoder().encodeToString(imagenBytes);
                            }

                            CatalogoMuebleDTO dto = CatalogoMuebleDTO.builder()
                                .id(((Number) result[0]).longValue())
                                .nombreMueble(result[1] != null ? result[1].toString() : null)
                                .colorMueble(result[2] != null ? result[2].toString() : null)
                                .descripcion(result[3] != null ? result[3].toString() : null)
                                .fechaAltaMueble(fechaAlta)
                                .fechaModificacionMueble(fechaModificacion)
                                .nombreCategoria(result[6] != null ? result[6].toString() : null)
                                .imagenPortada(imagenPortadaBase64)
                                .build();
                            return dto;
                        } catch (Exception e) {
                            logger.error("❌ [ERROR-CONVERSION] Error al convertir resultado: {}", e.getMessage());
                            throw new RuntimeException("Error al procesar resultado de mueble", e);
                        }
                    })
                    .collect(Collectors.toList());
                
                // Crear respuesta con información de paginación
                Map<String, Object> response = new HashMap<>();
                response.put("content", catalogoMuebles);
                response.put("totalElements", resultados.getTotalElements());
                response.put("totalPages", resultados.getTotalPages());
                response.put("currentPage", pageNumber);
                response.put("hasNext", resultados.hasNext());
                response.put("hasPrevious", resultados.hasPrevious());
                
                logger.info("✅ [FILTRAR-NOMBRE-O-COLOR] Filtrado completado exitosamente - {} muebles encontrados", catalogoMuebles.size());
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                logger.error("Error al filtrar muebles por nombre o color '{}': {}", filtro, e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error interno al filtrar por nombre o color\"}");
            }
        }
        
        /**
         * Método para obtener catálogo de muebles con objetos completos incluyendo todas las imágenes
         * Este método retorna la entidad completa con todas sus imágenes para el catálogo del frontend
         */
        public ResponseEntity<?> getDestacados() {
            try {
                List<Mueble> muebles = muebleRepository.findAllById(muebleRepository.findIdsDestacados());
                Map<Long, Object[]> portadas = portadasDe(muebles);
                List<CatalogoMuebleDTO> destacados = muebles.stream()
                    .map(mueble -> aDtoConPortada(mueble, portadas))
                    .collect(Collectors.toList());
                return ResponseEntity.ok(destacados);
            } catch (Exception e) {
                logger.error("❌ [DESTACADOS] Error al obtener destacados: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error interno del servidor\"}");
            }
        }

        /**
         * Portada de cada mueble en UNA consulta, en su version miniatura. Evita recorrer mueble.getImagenes(),
         * que traeria de la base todas las fotos originales de cada mueble solo para descartarlas.
         * Clave: id del mueble. Valor: [mueble_id, imagen_id, es_portada, bytes].
         */
        private Map<Long, Object[]> portadasDe(Collection<Mueble> muebles) {
            Map<Long, Object[]> portadas = new HashMap<>();
            if (muebles.isEmpty()) {
                return portadas;
            }
            List<Long> ids = muebles.stream().map(Mueble::getId).collect(Collectors.toList());
            for (Object[] fila : muebleImagenesRepository.findPortadas(ids)) {
                portadas.put(((Number) fila[0]).longValue(), fila);
            }
            return portadas;
        }

        private static String portadaBase64(Object[] fila) {
            return fila == null || fila[3] == null ? null
                : java.util.Base64.getEncoder().encodeToString((byte[]) fila[3]);
        }

        private CatalogoMuebleDTO aDtoConPortada(Mueble mueble, Map<Long, Object[]> portadas) {
            CatalogoMuebleDTO dto = CatalogoMuebleDTO.builder()
                .id(mueble.getId())
                .nombreMueble(mueble.getNombreMueble())
                .colorMueble(mueble.getColorMueble())
                .descripcion(mueble.getDescripcion())
                .fechaAltaMueble(mueble.getFechaAltaMueble())
                .nombreCategoria(mueble.getCategoria() != null ? mueble.getCategoria().getNombreCategoria() : "SIN_CATEGORIA")
                .build();
            dto.setImagenPortada(portadaBase64(portadas.get(mueble.getId())));
            return dto;
        }

        public ResponseEntity<?> getNovedades(int pageNumber) {
            try {
                Pageable pageable = PageRequest.of(pageNumber, 9, org.springframework.data.domain.Sort.by("id").descending());
                Page<Mueble> mueblePage = muebleRepository.findByPublicarTrueAndFechaBajaMuebleIsNull(pageable);

                Map<Long, Object[]> portadas = portadasDe(mueblePage.getContent());
                List<CatalogoMuebleDTO> novedades = mueblePage.getContent().stream()
                    .map(mueble -> aDtoConPortada(mueble, portadas))
                    .collect(Collectors.toList());

                Map<String, Object> response = new HashMap<>();
                response.put("content", novedades);
                response.put("totalElements", mueblePage.getTotalElements());
                response.put("totalPages", mueblePage.getTotalPages());
                response.put("number", mueblePage.getNumber());
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                logger.error("❌ [NOVEDADES] Error al obtener novedades: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error interno del servidor\"}");
            }
        }

        public ResponseEntity<?> getCatalogoMueblesCompleto(int pageNumber) {
            logger.info("\n=== [CATALOGO-COMPLETO] INICIANDO CONSULTA DE MUEBLES COMPLETOS ===");
            logger.info("Página solicitada: {}", pageNumber);
            logger.info("Filtros aplicados: Solo muebles ACTIVOS (fecha_baja IS NULL)");
            
            try {
                Pageable pageable = PageRequest.of(pageNumber, 12);
                
                // Obtener muebles activos con sus imágenes
                Page<Mueble> mueblePage = muebleRepository.findByFechaBajaMuebleIsNull(pageable);
                
                logger.info("✅ [CONSULTA-DB] Consulta ejecutada - {} muebles activos encontrados", 
                        mueblePage.getTotalElements());
                
                Map<Long, Object[]> portadas = portadasDe(mueblePage.getContent());
                // Convertir a DTO completo con todas las imágenes
                List<CatalogoMuebleDTO> catalogo = mueblePage.getContent().stream()
                    .map(mueble -> {
                        CatalogoMuebleDTO dto = CatalogoMuebleDTO.builder()
                            .id(mueble.getId())
                            .nombreMueble(mueble.getNombreMueble())
                            .colorMueble(mueble.getColorMueble())
                            .descripcion(mueble.getDescripcion())
                            .fechaAltaMueble(mueble.getFechaAltaMueble())
                            .fechaModificacionMueble(mueble.getFechaModificacionMueble())
                            .nombreCategoria(mueble.getCategoria() != null ? mueble.getCategoria().getNombreCategoria() : "SIN_CATEGORIA")
                            .build();
                        
                        // Solo la portada: la galería se pide por mueble al abrir su ficha
                        dto.setImagenPortada(portadaBase64(portadas.get(mueble.getId())));
                        
                        return dto;
                    })
                    .collect(Collectors.toList());
                
                // Crear respuesta paginada
                Map<String, Object> response = new HashMap<>();
                response.put("content", catalogo);
                response.put("totalElements", mueblePage.getTotalElements());
                response.put("totalPages", mueblePage.getTotalPages());
                response.put("number", mueblePage.getNumber());
                response.put("size", mueblePage.getSize());
                response.put("numberOfElements", mueblePage.getNumberOfElements());
                response.put("first", mueblePage.isFirst());
                response.put("last", mueblePage.isLast());
                
                logger.info("✅ [CATALOGO-COMPLETO] Procesados {} muebles ACTIVOS con imágenes completas para página {}", 
                        catalogo.size(), pageNumber);
                logger.info("📊 [ESTADISTICAS] Total muebles activos: {}, Páginas totales: {}", 
                        mueblePage.getTotalElements(), mueblePage.getTotalPages());
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                logger.error("❌ [CATALOGO-COMPLETO] Error al obtener catálogo completo: {}", e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error interno al cargar el catálogo completo\"}");
            }
        }

        @Override
        public ResponseEntity<?> agregarImagenesMueble(Long muebleId, List<MultipartFile> files, Integer portadaIndex) {
            logger.info("\n=== [AGREGAR-IMAGENES] INICIANDO PROCESO DE AGREGADO DE IMÁGENES ===");
            logger.info("Mueble ID: {}", muebleId);
            logger.info("Número de archivos a agregar: {}", files != null ? files.size() : 0);
            logger.info("Índice de nueva portada: {}", portadaIndex);
            
            try {
                // Validar que existan archivos
                if (files == null || files.isEmpty()) {
                    logger.warn("⚠️ [VALIDACION] No se proporcionaron archivos para agregar");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\":\"No se proporcionaron archivos para agregar\"}");
                }
                
                // Verificar que el mueble existe
                Optional<Mueble> muebleOpt = muebleRepository.findById(muebleId);
                if (muebleOpt.isEmpty()) {
                    logger.warn("⚠️ [MUEBLE-NO-ENCONTRADO] Mueble con ID {} no existe", muebleId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"Mueble no encontrado\"}");
                }
                
                Mueble mueble = muebleOpt.get();
                logger.info("✅ [MUEBLE-ENCONTRADO] Mueble encontrado: {}", mueble.getNombreMueble());
                logger.info("📊 [IMAGENES-ACTUALES] El mueble tiene {} imágenes existentes", 
                        mueble.getImagenes() != null ? mueble.getImagenes().size() : 0);
                
                // Validar tamaño de archivos
                for (MultipartFile file : files) {
                    if (file.getSize() > MAX_FILE_SIZE_BYTES) {
                        logger.warn("⚠️ [ARCHIVO-GRANDE] Archivo {} excede el tamaño máximo", file.getOriginalFilename());
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("{\"error\":\"El archivo " + file.getOriginalFilename() + " excede el tamaño máximo de 10MB\"}");
                    }
                }
                
                // Si se especifica un portadaIndex, desmarcar todas las portadas existentes
                if (portadaIndex != null) {
                    if (portadaIndex < 0 || portadaIndex >= files.size()) {
                        logger.warn("⚠️ [PORTADA-INDEX-INVALIDO] Índice de portada {} inválido para {} archivos", 
                                portadaIndex, files.size());
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("{\"error\":\"Índice de portada inválido\"}");
                    }
                    
                    // Desmarcar todas las imágenes existentes como portada
                    if (mueble.getImagenes() != null) {
                        for (MuebleImagenes imagen : mueble.getImagenes()) {
                            imagen.setEsPortada(false);
                            muebleImagenesService.save(imagen);
                        }
                        logger.info("🔄 [PORTADA-ACTUALIZADA] Portadas existentes desmarcadas");
                    }
                }
                
                // Procesar y guardar las nuevas imágenes
                List<MuebleImagenes> nuevasImagenes = new ArrayList<>();
                for (int i = 0; i < files.size(); i++) {
                    MultipartFile file = files.get(i);
                    
                    try {
                        byte[] imageBytes = file.getBytes();
                        
                        MuebleImagenes nuevaImagen = MuebleImagenes.builder()
                            .imagenes(imageBytes)
                            .miniatura(Miniaturas.generar(imageBytes))
                            .esPortada(portadaIndex != null && i == portadaIndex)
                            .build();
                        
                        MuebleImagenes imagenGuardada = muebleImagenesService.save(nuevaImagen);
                        nuevasImagenes.add(imagenGuardada);
                        
                        logger.info("✅ [IMAGEN-PROCESADA] Imagen {} procesada - ID: {}, Es portada: {}", 
                                file.getOriginalFilename(), imagenGuardada.getId(), imagenGuardada.isEsPortada());
                        
                    } catch (IOException e) {
                        logger.error("❌ [ERROR-ARCHIVO] Error al procesar archivo {}: {}", 
                                file.getOriginalFilename(), e.getMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("{\"error\":\"Error al procesar el archivo: " + file.getOriginalFilename() + "\"}");
                    }
                }
                
                // Agregar las nuevas imágenes al mueble
                if (mueble.getImagenes() == null) {
                    mueble.setImagenes(new ArrayList<>());
                }
                mueble.getImagenes().addAll(nuevasImagenes);
                
                // Actualizar fecha de modificación
                mueble.setFechaModificacionMueble(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                
                // Guardar el mueble actualizado
                Mueble muebleActualizado = muebleRepository.save(mueble);
                
                // Crear respuesta
                Map<String, Object> response = new HashMap<>();
                response.put("mensaje", "Imágenes agregadas exitosamente");
                response.put("muebleId", muebleId);
                response.put("imagenesAgregadas", nuevasImagenes.size());
                response.put("totalImagenes", muebleActualizado.getImagenes().size());
                response.put("nuevaPortada", portadaIndex != null);
                
                logger.info("✅ [AGREGAR-IMAGENES] Proceso completado exitosamente");
                logger.info("📊 [RESULTADO] {} imágenes agregadas, total de imágenes: {}", 
                        nuevasImagenes.size(), muebleActualizado.getImagenes().size());
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                logger.error("❌ [AGREGAR-IMAGENES] Error interno: {}", e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error interno del servidor\"}");
            }
        }

        @Override
        public ResponseEntity<?> establecerImagenPortada(Long imagenId) {
            logger.info("\n=== [ESTABLECER-PORTADA] INICIANDO CAMBIO DE IMAGEN PORTADA ===");
            logger.info("Imagen ID para nueva portada: {}", imagenId);
            
            try {
                // Buscar la imagen
                MuebleImagenes imagenNuevaPortada = muebleImagenesService.findById(imagenId);
                if (imagenNuevaPortada == null) {
                    logger.warn("⚠️ [IMAGEN-NO-ENCONTRADA] Imagen con ID {} no existe", imagenId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"Imagen no encontrada\"}");
                }
                
                logger.info("✅ [IMAGEN-ENCONTRADA] Imagen encontrada - ID: {}", imagenId);

                // Buscar el mueble que contiene esta imagen (consulta directa, sin recorrer todo el catálogo)
                Long muebleId = muebleRepository.findMuebleIdByImagenId(imagenId);
                Mueble muebleContenedor = muebleId != null ? muebleRepository.findById(muebleId).orElse(null) : null;

                if (muebleContenedor == null) {
                    logger.warn("⚠️ [MUEBLE-NO-ENCONTRADO] No se encontró el mueble que contiene la imagen ID: {}", imagenId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"No se encontró el mueble que contiene esta imagen\"}");
                }

                logger.info("✅ [MUEBLE-ENCONTRADO] Imagen pertenece al mueble: {} (ID: {})",
                        muebleContenedor.getNombreMueble(), muebleContenedor.getId());

                // Verificar si la imagen ya es portada
                if (imagenNuevaPortada.isEsPortada()) {
                    logger.info("ℹ️ [YA-ES-PORTADA] La imagen ID {} ya es la portada actual", imagenId);
                    return ResponseEntity.ok()
                        .body("{\"mensaje\":\"La imagen ya es la portada actual\",\"imagenId\":" + imagenId + ",\"muebleId\":" + muebleContenedor.getId() + "}");
                }
                
                // Buscar la portada actual y desmarcarla
                MuebleImagenes portadaAnterior = null;
                for (MuebleImagenes img : muebleContenedor.getImagenes()) {
                    if (img.isEsPortada()) {
                        img.setEsPortada(false);
                        muebleImagenesService.save(img);
                        portadaAnterior = img;
                        logger.info("🔄 [PORTADA-ANTERIOR-DESMARCADA] Portada anterior desmarcada - ID: {}", img.getId());
                        break;
                    }
                }
                
                // Establecer la nueva portada
                imagenNuevaPortada.setEsPortada(true);
                muebleImagenesService.save(imagenNuevaPortada);
                logger.info("🎯 [NUEVA-PORTADA-ASIGNADA] Nueva portada asignada - ID: {}", imagenId);
                
                // Actualizar fecha de modificación del mueble
                muebleContenedor.setFechaModificacionMueble(
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                
                // Guardar el mueble actualizado
                muebleRepository.save(muebleContenedor);
                
                // Crear respuesta
                Map<String, Object> response = new HashMap<>();
                response.put("mensaje", "Imagen de portada cambiada exitosamente");
                response.put("nuevaPortadaId", imagenId);
                response.put("muebleId", muebleContenedor.getId());
                response.put("muebleNombre", muebleContenedor.getNombreMueble());
                if (portadaAnterior != null) {
                    response.put("portadaAnteriorId", portadaAnterior.getId());
                }
                
                logger.info("✅ [ESTABLECER-PORTADA] Imagen de portada cambiada exitosamente");
                logger.info("📊 [RESULTADO] Mueble: {} - Nueva portada ID: {} - Portada anterior ID: {}", 
                        muebleContenedor.getNombreMueble(), imagenId, 
                        portadaAnterior != null ? portadaAnterior.getId() : "ninguna");
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                logger.error("❌ [ESTABLECER-PORTADA] Error interno: {}", e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error interno del servidor\"}");
            }
        }

        @Override
        public ResponseEntity<?> eliminarImagen(Long imagenId) {
            logger.info("\n=== [ELIMINAR-IMAGEN] INICIANDO ELIMINACIÓN DEFINITIVA DE IMAGEN ===");
            logger.info("Imagen ID: {}", imagenId);
            
            try {
                // Buscar la imagen
                MuebleImagenes imagen = muebleImagenesService.findById(imagenId);
                if (imagen == null) {
                    logger.warn("⚠️ [IMAGEN-NO-ENCONTRADA] Imagen con ID {} no existe", imagenId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"Imagen no encontrada\"}");
                }
                boolean eraPortada = imagen.isEsPortada();
                logger.info("✅ [IMAGEN-ENCONTRADA] Imagen encontrada - ID: {}, Es portada: {}", imagenId, eraPortada);

                // Buscar el mueble que contiene esta imagen (consulta directa, sin recorrer todo el catálogo)
                Long muebleId = muebleRepository.findMuebleIdByImagenId(imagenId);
                Mueble muebleContenedor = muebleId != null ? muebleRepository.findById(muebleId).orElse(null) : null;

                if (muebleContenedor != null) {
                    logger.info("✅ [MUEBLE-ENCONTRADO] Imagen pertenece al mueble: {} (ID: {})", 
                            muebleContenedor.getNombreMueble(), muebleContenedor.getId());
                    
                    // Remover la imagen de la lista del mueble
                    muebleContenedor.getImagenes().removeIf(img -> img.getId().equals(imagenId));
                    
                    // Si era la portada, verificar si quedan más imágenes para asignar nueva portada
                    if (eraPortada && !muebleContenedor.getImagenes().isEmpty()) {
                        // Asignar la primera imagen restante como nueva portada
                        MuebleImagenes nuevaPortada = muebleContenedor.getImagenes().get(0);
                        nuevaPortada.setEsPortada(true);
                        muebleImagenesService.save(nuevaPortada);
                        logger.info("🔄 [NUEVA-PORTADA] Nueva portada asignada - ID: {}", nuevaPortada.getId());
                    }
                    
                    // Actualizar fecha de modificación del mueble
                    muebleContenedor.setFechaModificacionMueble(
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    
                    // Guardar el mueble actualizado
                    muebleRepository.save(muebleContenedor);
                }
                
                // Eliminar definitivamente la imagen de la base de datos
                muebleImagenesService.delete(imagenId);
                
                // Crear respuesta
                Map<String, Object> response = new HashMap<>();
                response.put("mensaje", "Imagen eliminada exitosamente");
                response.put("imagenId", imagenId);
                response.put("eraPortada", eraPortada);
                if (muebleContenedor != null) {
                    response.put("muebleId", muebleContenedor.getId());
                    response.put("imagenesRestantes", muebleContenedor.getImagenes().size());
                    response.put("nuevaPortadaAsignada", eraPortada && !muebleContenedor.getImagenes().isEmpty());
                }
                
                logger.info("✅ [ELIMINAR-IMAGEN] Imagen eliminada definitivamente");
                if (muebleContenedor != null) {
                    logger.info("📊 [RESULTADO] Mueble actualizado - Imágenes restantes: {}", 
                            muebleContenedor.getImagenes().size());
                }
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                logger.error("❌ [ELIMINAR-IMAGEN] Error interno: {}", e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error interno del servidor\"}");
            }
        }

        @Override
        public ResponseEntity<?> obtenerMueblesDadosDeBaja(int pageNumber) {
            logger.info("\n=== [OBTENER-MUEBLES-DADOS-DE-BAJA] INICIANDO CONSULTA ===");
            logger.info("Página solicitada: {}", pageNumber);
            
            try {
                // Configurar paginación
                int pageSize = 9; // Mismo tamaño que otros endpoints
                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                
                // Obtener muebles dados de baja
                Page<Mueble> mueblesDadosDeBaja = muebleRepository.findByFechaBajaMuebleIsNotNull(pageable);
                
                if (mueblesDadosDeBaja.isEmpty()) {
                    logger.info("📋 [SIN-RESULTADOS] No se encontraron muebles dados de baja en la página: {}", pageNumber);
                    Map<String, Object> response = new HashMap<>();
                    response.put("muebles", List.of());
                    response.put("totalPages", 0);
                    response.put("totalElements", 0);
                    response.put("currentPage", pageNumber);
                    response.put("hasNext", false);
                    response.put("hasPrevious", false);
                    return ResponseEntity.ok(response);
                }
                
                Map<Long, Object[]> portadas = portadasDe(mueblesDadosDeBaja.getContent());
                // Convertir a DTOs con imagen de portada
                List<CatalogoMuebleDTO> mueblesDTO = mueblesDadosDeBaja.getContent().stream()
                    .map(mueble -> {
                        CatalogoMuebleDTO dto = CatalogoMuebleDTO.builder()
                            .id(mueble.getId())
                            .nombreMueble(mueble.getNombreMueble())
                            .colorMueble(mueble.getColorMueble())
                            .descripcion(mueble.getDescripcion())
                            .fechaAltaMueble(mueble.getFechaAltaMueble())
                            .fechaModificacionMueble(mueble.getFechaModificacionMueble())
                            .nombreCategoria(mueble.getCategoria() != null ? mueble.getCategoria().getNombreCategoria() : "SIN_CATEGORIA")
                            .build();
                        
                        dto.setImagenPortada(portadaBase64(portadas.get(mueble.getId())));
                        
                        return dto;
                    })
                    .collect(Collectors.toList());
                
                // Crear respuesta paginada
                Map<String, Object> response = new HashMap<>();
                response.put("muebles", mueblesDTO);
                response.put("totalPages", mueblesDadosDeBaja.getTotalPages());
                response.put("totalElements", mueblesDadosDeBaja.getTotalElements());
                response.put("currentPage", pageNumber);
                response.put("hasNext", mueblesDadosDeBaja.hasNext());
                response.put("hasPrevious", mueblesDadosDeBaja.hasPrevious());
                
                logger.info("✅ [OBTENER-MUEBLES-DADOS-DE-BAJA] Consulta exitosa");
                logger.info("📊 [RESULTADOS] Página: {}, Elementos: {}, Total páginas: {}", 
                    pageNumber, mueblesDTO.size(), mueblesDadosDeBaja.getTotalPages());
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                logger.error("❌ [OBTENER-MUEBLES-DADOS-DE-BAJA] Error interno: {}", e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error interno del servidor\"}");
            }
        }
    }
