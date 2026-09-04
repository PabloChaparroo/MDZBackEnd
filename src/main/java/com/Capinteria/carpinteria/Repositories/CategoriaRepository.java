package com.Capinteria.carpinteria.Repositories;

import com.Capinteria.carpinteria.Entity.Categoria;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends BaseRepository<Categoria,Long> {
    
    @Query("SELECT COUNT(m) FROM Mueble m WHERE m.categoria.id = :categoriaId")
    long countMueblesByCategoria(@Param("categoriaId") Long categoriaId);
    
    @Query("SELECT c FROM Categoria c WHERE c.id = :id AND c.fechaBajaCategoria IS NULL")
    Categoria findByIdAndNotDeleted(@Param("id") Long id);
    
    boolean existsByNombreCategoria(String nombreCategoria);
    
    // Verificar si existe una categoría activa (no dada de baja) con el mismo nombre
    @Query("SELECT COUNT(c) > 0 FROM Categoria c WHERE c.nombreCategoria = :nombreCategoria AND c.fechaBajaCategoria IS NULL")
    boolean existsByNombreCategoriaAndNotDeleted(@Param("nombreCategoria") String nombreCategoria);
    
    @Query("SELECT COUNT(c) > 0 FROM Categoria c WHERE c.nombreCategoria = :nombreCategoria AND c.id != :id")
    boolean existsByNombreCategoriaAndIdNot(@Param("nombreCategoria") String nombreCategoria, @Param("id") Long id);
    
    // Verificar si existe otra categoría activa (no dada de baja) con el mismo nombre, excluyendo la actual
    @Query("SELECT COUNT(c) > 0 FROM Categoria c WHERE c.nombreCategoria = :nombreCategoria AND c.id != :id AND c.fechaBajaCategoria IS NULL")
    boolean existsByNombreCategoriaAndIdNotAndNotDeleted(@Param("nombreCategoria") String nombreCategoria, @Param("id") Long id);
}
