# Endpoints de Filtrado - Muebles

## Endpoint de Filtrado Disponible

### Filtrar por Nombre o Color (GET)
**Endpoint:** `GET /api/v1/mueble/filtrar/buscar/{filtro}/{pageNumber}`

**Descripción:** Busca muebles que contengan el filtro especificado en el nombre O en el color del mueble. Solo devuelve muebles activos (no dados de baja). Incluye paginación para manejar grandes cantidades de resultados.

**Parámetros:**
- `filtro`: Texto a buscar en el nombre o color del mueble (case insensitive)
- `pageNumber`: Número de página (0-based, comenzando en 0)

**Ejemplo de uso:**
```
GET /api/v1/mueble/filtrar/buscar/mesa/0
GET /api/v1/mueble/filtrar/buscar/roble/1
GET /api/v1/mueble/filtrar/buscar/marrón/0
```

**Respuesta exitosa:**
```json
{
    "content": [
        {
            "id": 1,
            "nombreMueble": "Mesa de Comedor",
            "colorMueble": "Marrón",
            "dimension": "160x90x75",
            "tipoMadera": "ROBLE",
            "precio": 2500.00,
            "descripcion": "Mesa de comedor en roble macizo",
            "fechaAltaMueble": "2025-01-15 10:00:00",
            "fechaModificacionMueble": null,
            "nombreCategoria": "Comedor",
            "imagenPortada": "base64EncodedImageString..."
        }
    ],
    "totalElements": 25,
    "totalPages": 3,
    "currentPage": 0,
    "hasNext": true,
    "hasPrevious": false
}
```

**Respuesta de error (filtro vacío):**
```json
{
    "error": "El filtro no puede estar vacío"
}
```

## Características del Filtrado

### Paginación:
- **Elementos por página:** 10 muebles
- **Página inicial:** 0 (zero-based)
- **Información incluida:** Total de elementos, total de páginas, página actual, navegación

### Optimización y Respuesta:
- **Consulta optimizada:** Usa query nativa para mejor rendimiento
- **Imagen:** Solo devuelve imagen de portada (base64 encoded)
- **DTO:** Usa CatalogoMuebleDTO para respuesta ligera
- **Fechas:** Formateadas como strings (yyyy-MM-dd HH:mm:ss)

### Busqueda:
- **Nombre:** Busca en el campo `nombreMueble`
- **Color:** Busca en el campo `colorMueble`
- **Operador:** OR (busca en nombre O color)
- **Tipo:** Búsqueda parcial (LIKE %filtro%)
- **Case:** Insensitive (no distingue mayúsculas/minúsculas)

### Filtros aplicados:
- **Solo muebles activos:** `fechaBajaMueble IS NULL`
- **Incluye relaciones:** Solo categoría (nombreCategoria)
- **Ordenamiento:** Por nombre del mueble (ASC)

## Casos de uso:

### Búsqueda por nombre:
```
GET /filtrar/buscar/mesa/0     → Encuentra "Mesa de Comedor", "Mesa de Centro", etc. (página 1)
GET /filtrar/buscar/silla/1    → Encuentra "Silla de Oficina", "Silla Ergonómica", etc. (página 2)
GET /filtrar/buscar/comedor/0  → Encuentra muebles con "comedor" en el nombre (página 1)
```

### Búsqueda por color:
```
GET /filtrar/buscar/negro/0    → Encuentra muebles de color negro (página 1)
GET /filtrar/buscar/blanco/0   → Encuentra muebles de color blanco (página 1)
GET /filtrar/buscar/marrón/0   → Encuentra muebles de color marrón (página 1)
```

### Búsqueda combinada:
```
GET /filtrar/buscar/roble/0    → Puede encontrar:
                               - "Mesa de Roble" (por nombre)
                               - Muebles con color "Roble Natural" (por color)
```

## Implementación técnica:

### Query SQL generada:
```sql
SELECT m.* FROM mueble m 
WHERE (m.nombre_mueble LIKE '%filtro%' OR m.color_mueble LIKE '%filtro%') 
AND m.fecha_baja IS NULL 
ORDER BY m.nombre_mueble ASC
```

### Validaciones:
- ✅ Filtro no puede estar vacío o null
- ✅ Se aplica trim() al filtro
- ✅ Solo devuelve muebles activos
- ✅ Manejo de errores con logging

## Endpoints eliminados:

### ❌ Endpoints removidos:
- `GET /filtrar/nombre/{nombre}` - Filtro solo por nombre
- `GET /filtrar/color/{color}` - Filtro solo por color  
- `GET /filtrar/avanzado?nombre=X&color=Y` - Filtro por nombre Y color

### ✅ Endpoint mantenido:
- `GET /filtrar/buscar/{filtro}` - Filtro general por nombre O color

## Razón del cambio:
Se simplificó la API manteniendo solo el filtro más útil y versátil, que permite buscar tanto por nombre como por color con una sola consulta, reduciendo la complejidad del sistema y facilitando el uso desde el frontend.
