# 📋 ENDPOINT: Muebles Dados de Baja

## 🔍 **Descripción**
Endpoint paginado para obtener únicamente los muebles que han sido dados de baja lógicamente del sistema.

## 🌐 **URL del Endpoint**
```
GET /api/v1/mueble/dados-de-baja/{pageNumber}
```

## 📊 **Parámetros**
- **pageNumber** (PathVariable): Número de página (empezando desde 0)

## 📤 **Respuesta Exitosa (200 OK)**
```json
{
    "muebles": [
        {
            "id": 1,
            "nombreMueble": "Mesa de Comedor",
            "colorMueble": "Nogal",
            "dimension": "150x90x75 cm",
            "tipoMadera": "ROBLE",
            "precio": 150000.00,
            "descripcion": "Mesa de comedor para 6 personas",
            "fechaAltaMueble": "2024-01-15 10:30:00",
            "fechaModificacionMueble": "2024-01-20 14:15:00",
            "nombreCategoria": "Mesas",
            "imagenPortada": "base64_imagen_aqui...",
            "imagenes": null
        }
    ],
    "totalPages": 5,
    "totalElements": 42,
    "currentPage": 0,
    "hasNext": true,
    "hasPrevious": false
}
```

## 📤 **Respuesta Sin Resultados (200 OK)**
```json
{
    "muebles": [],
    "totalPages": 0,
    "totalElements": 0,
    "currentPage": 0,
    "hasNext": false,
    "hasPrevious": false
}
```

## ❌ **Respuesta de Error (500)**
```json
{
    "error": "Error interno del servidor"
}
```

## 🔧 **Características**
- **Paginación**: 9 elementos por página
- **Filtrado**: Solo muebles con `fechaBajaMueble IS NOT NULL`
- **Ordenamiento**: Por fecha de baja (más recientes primero)
- **Imágenes**: Incluye solo la imagen de portada en formato Base64
- **Categoría**: Incluye el nombre de la categoría

## 🚀 **Ejemplo de Uso**
```bash
# Obtener primera página de muebles dados de baja
GET http://localhost:8080/api/v1/mueble/dados-de-baja/0

# Obtener segunda página
GET http://localhost:8080/api/v1/mueble/dados-de-baja/1
```

## 📝 **Casos de Uso**
1. **Administración**: Ver historial de muebles eliminados
2. **Recuperación**: Posible restauración de muebles dados de baja
3. **Auditoría**: Seguimiento de elementos eliminados del catálogo
4. **Reportes**: Generación de informes de productos descontinuados

## 🔄 **Relación con Otros Endpoints**
- **Complementa**: `/obtener-todos-muebles/{pageNumber}` (activos)
- **Opuesto**: Mientras uno muestra activos, este muestra dados de baja
- **Restauración**: Puede ser usado en conjunto con endpoints de restauración

## 🗃️ **Base de Datos**
- **Consulta**: `SELECT * FROM mueble WHERE fecha_baja IS NOT NULL ORDER BY fecha_baja DESC`
- **Índice recomendado**: `CREATE INDEX idx_fecha_baja ON mueble(fecha_baja)`

## ⚡ **Rendimiento**
- **Optimizado**: Usa paginación para evitar cargar todos los registros
- **Eficiente**: Solo carga imagen de portada (no todas las imágenes)
- **Rápido**: Consulta directa sin JOINs complejos
