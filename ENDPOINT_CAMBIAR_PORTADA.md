# Endpoint para Gestión de Portadas de Muebles

## Cambiar Imagen de Portada

**Endpoint:** `PUT /api/v1/mueble/imagen/{imagenId}/establecer-portada`

**Descripción:** Establece una imagen específica como portada del mueble. Si el mueble ya tiene otra imagen como portada, la desmarca automáticamente y asigna la nueva.

### Parámetros de Ruta
- `imagenId` (Long): ID de la imagen que se quiere establecer como portada

### Respuesta Exitosa (200 OK)
```json
{
  "mensaje": "Imagen de portada cambiada exitosamente",
  "nuevaPortadaId": 123,
  "muebleId": 45,
  "muebleNombre": "Mesa de Roble",
  "portadaAnteriorId": 121
}
```

### Casos Especiales

#### Si la imagen ya es portada (200 OK)
```json
{
  "mensaje": "La imagen ya es la portada actual",
  "imagenId": 123,
  "muebleId": 45
}
```

#### Si la imagen no existe (404 NOT FOUND)
```json
{
  "error": "Imagen no encontrada"
}
```

#### Si no se encuentra el mueble que contiene la imagen (404 NOT FOUND)
```json
{
  "error": "No se encontró el mueble que contiene esta imagen"
}
```

### Lógica de Funcionamiento

1. **Buscar la imagen:** Verifica que la imagen con el ID proporcionado existe
2. **Encontrar el mueble:** Localiza el mueble que contiene esta imagen
3. **Verificar estado actual:** Comprueba si la imagen ya es portada
4. **Desmarcar portada anterior:** Si existe otra imagen como portada, la desmarca (`esPortada = false`)
5. **Asignar nueva portada:** Marca la imagen especificada como portada (`esPortada = true`)
6. **Actualizar fecha:** Actualiza la fecha de modificación del mueble
7. **Guardar cambios:** Persiste todos los cambios en la base de datos

### Ejemplo de Uso
```bash
# Cambiar la imagen con ID 123 como nueva portada
curl -X PUT http://localhost:8080/api/v1/mueble/imagen/123/establecer-portada
```

### Logs Generados
- `[ESTABLECER-PORTADA]` - Información general del proceso
- `[IMAGEN-ENCONTRADA]` - Confirmación de que la imagen existe
- `[MUEBLE-ENCONTRADO]` - Identificación del mueble contenedor
- `[PORTADA-ANTERIOR-DESMARCADA]` - Desmarcado de portada anterior
- `[NUEVA-PORTADA-ASIGNADA]` - Asignación de nueva portada
- `[RESULTADO]` - Resumen final del cambio

### Consideraciones Técnicas
- El endpoint es idempotente: llamarlo múltiples veces con el mismo ID produce el mismo resultado
- Se actualiza automáticamente la fecha de modificación del mueble
- Se mantiene la integridad referencial entre muebles e imágenes
- El proceso es transaccional para evitar inconsistencias de datos
