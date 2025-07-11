package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.Entity.Categoria;
import org.springframework.http.ResponseEntity;


public interface CategoriaService extends BaseService<Categoria, Long> {
    
    /**
     * Realiza baja lógica de una categoría
     * @param id ID de la categoría a dar de baja
     * @return true si se dio de baja exitosamente
     * @throws RuntimeException si la categoría no existe, ya está dada de baja o tiene muebles asociados
     */
    boolean bajaLogica(Long id);
    
    /**
     * Crea una nueva categoría estableciendo automáticamente la fecha de alta
     * @param categoria Categoría a crear
     * @return Categoría creada con fecha de alta establecida
     */
    Categoria crearCategoria(Categoria categoria);
    
    /**
     * Modifica una categoría estableciendo automáticamente la fecha de modificación
     * @param id ID de la categoría a modificar
     * @param categoriaActualizada Datos de la categoría actualizada
     * @return Categoría modificada con fecha de modificación establecida
     */
    Categoria modificarCategoria(Long id, Categoria categoriaActualizada);
    
    /**
     * Actualiza una categoría existente estableciendo automáticamente la fecha de modificación
     * @param id ID de la categoría a actualizar
     * @param categoria Datos actualizados de la categoría
     * @return Categoría actualizada con fecha de modificación establecida
     * @throws RuntimeException si la categoría no existe o está dada de baja
     */
    Categoria updateCategoria(Long id, Categoria categoria);
    
    /**
     * Obtiene todas las categorías activas con fechas formateadas para el frontend
     * @return ResponseEntity con lista de categorías formateadas
     */
    ResponseEntity<?> obtenerCategoriasFormateadas();
    
    /**
     * Obtiene todas las categorías dadas de baja con fechas formateadas para el frontend
     * @return ResponseEntity con lista de categorías dadas de baja formateadas
     */
    ResponseEntity<?> obtenerCategoriasDadasDeBaja();
}
