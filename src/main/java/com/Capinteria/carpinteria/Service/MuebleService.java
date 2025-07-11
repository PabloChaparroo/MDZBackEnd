package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.Entity.Mueble;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MuebleService extends BaseService<Mueble, Long> {

    // Método para obtener los primeros 9 muebles con paginación
    Page<Mueble> getFirst9Muebles(Pageable pageable);

    // Método para crear un mueble con imágenes (versión actualizada con categoriaId)
    ResponseEntity<String> createMueble(String muebleJson, Long categoriaId, List<MultipartFile> files, Integer portadaIndex);

    // Método para actualizar imágenes de un mueble específico
    ResponseEntity<?> updateMuebleImages(Long id, MultipartFile[] files, Integer portadaIndex);

    // Método para actualizar imágenes de todos los muebles
    ResponseEntity<?> updateAllMueblesImages(MultipartFile[] files, Integer portadaIndex);

    // Método para obtener muebles paginados
    ResponseEntity<?> getMueblesPaginated(int pageNumber);
    
    // Método para obtener todos los muebles activos paginados (sin filtro por categoría)
    ResponseEntity<?> obtenerTodosMuebles(int pageNumber);
    
    // Método súper optimizado para catálogo filtrado por categoría
    ResponseEntity<?> getCatalogoMueblesPorCategoria(int pageNumber, Long categoriaId);

    /**
     * Realiza baja lógica de un mueble
     * @param id ID del mueble a dar de baja
     * @return true si se dio de baja exitosamente
     * @throws RuntimeException si el mueble no existe o ya está dado de baja
     */
    boolean bajaLogica(Long id);
    
    /**
     * Obtiene todas las imágenes de un mueble específico
     * @param muebleId ID del mueble
     * @return ResponseEntity con las imágenes en formato Base64 para el frontend
     */
    ResponseEntity<?> obtenerImagenesMueble(Long muebleId);
    
    /**
     * Modifica un mueble estableciendo automáticamente la fecha de modificación
     * @param id ID del mueble a modificar
     * @param muebleActualizado Datos del mueble actualizado
     * @return ResponseEntity con el mueble modificado
     */
    ResponseEntity<?> modificarMueble(Long id, Mueble muebleActualizado);
    
    /**
     * Filtra muebles por nombre O color (búsqueda parcial) con paginación
     * @param filtro Texto a buscar en nombre o color del mueble
     * @param pageNumber Número de página (0-based)
     * @return ResponseEntity con página de muebles filtrados
     */
    ResponseEntity<?> filtrarPorNombreOColor(String filtro, int pageNumber);
    
    /**
     * Obtiene catálogo de muebles con objetos completos incluyendo todas las imágenes
     * @param pageNumber Número de página (0-based)
     * @return ResponseEntity con página de muebles completos con todas sus imágenes
     */
    ResponseEntity<?> getCatalogoMueblesCompleto(int pageNumber);

    /**
     * Agrega nuevas imágenes a un mueble existente
     * @param muebleId ID del mueble al que agregar las imágenes
     * @param files Lista de archivos de imagen a agregar
     * @param portadaIndex Índice opcional para establecer una nueva imagen como portada
     * @return ResponseEntity con el resultado de la operación
     */
    ResponseEntity<?> agregarImagenesMueble(Long muebleId, List<MultipartFile> files, Integer portadaIndex);

    /**
     * Establece una imagen específica como portada del mueble
     * @param imagenId ID de la imagen a establecer como portada
     * @return ResponseEntity con el resultado de la operación
     */
    ResponseEntity<?> establecerImagenPortada(Long imagenId);

    /**
     * Elimina definitivamente una imagen de la base de datos
     * @param imagenId ID de la imagen a eliminar
     * @return ResponseEntity con el resultado de la operación
     */
    ResponseEntity<?> eliminarImagen(Long imagenId);

    /**
     * Obtiene todos los muebles dados de baja con paginación
     * @param pageNumber Número de página (0-based)
     * @return ResponseEntity con página de muebles dados de baja
     */
    ResponseEntity<?> obtenerMueblesDadosDeBaja(int pageNumber);
}
