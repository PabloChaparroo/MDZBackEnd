# Documentación de Endpoints - Solicitar Visita

## Endpoints Disponibles

### 1. Crear Consulta (POST)
**Endpoint:** `POST /api/v1/solicitarVisita/crear-consulta`

**Descripción:** Crea una nueva consulta con cliente y solicitud de visita. Si el cliente ya existe (por email), se reutiliza. Si no existe, se crea uno nuevo.

**Cuerpo de la petición (JSON):**
```json
{
  "cliente": {
    "nombreCliente": "Juan",
    "apellidoCliente": "Pérez",
    "telefonoCliente": 123456789,
    "mailCliente": "juan.perez@email.com"
  },
  "fechaHoraAltaSolicitarVisita": "2025-01-15 14:30:00",
  "consultaSolicitarVisita": "Me interesa conocer más sobre los muebles de cocina disponibles"
}
```

**Respuesta exitosa:**
```json
{
  "mensaje": "Consulta creada exitosamente",
  "solicitudId": 123,
  "clienteId": 456,
  "clienteNuevo": true
}
```

**Características:**
- Verifica si el cliente existe por email antes de crear uno nuevo
- La solicitud no se asocia a ningún mueble específico (mueble será null)
- Transaccional: si algo falla, no se guarda nada

### 2. Obtener Consultas Paginadas (GET)
**Endpoint:** `GET /api/v1/solicitarVisita/obtener-consultas/{pageNumber}`

**Descripción:** Obtiene todas las consultas generales paginadas (solo aquellas que NO tienen un mueble asociado), ordenadas por fecha de creación (más recientes primero).

**Parámetros:**
- `pageNumber`: Número de página (0-indexed)
- Tamaño de página fijo: 20 elementos

**Filtros aplicados:**
- Solo consultas donde `mueble IS NULL` (consultas generales, no asociadas a muebles específicos)

**Ejemplo de uso:**
```
GET /api/v1/solicitarVisita/obtener-consultas/0
```

**Respuesta:**
```json
{
  "content": [
    {
      "id": 123,
      "fechaHoraAltaSolicitarVisita": "2025-01-15 14:30:00",
      "consultaSolicitarVisita": "Me interesa conocer más sobre los muebles de cocina disponibles",
      "cliente": {
        "id": 456,
        "nombreCliente": "Juan",
        "apellidoCliente": "Pérez",
        "telefonoCliente": 123456789,
        "mailCliente": "juan.perez@email.com",
        "estadoCliente": "PENDIENTE"
      },
      "mueble": null
    }
  ],
  "totalElements": 50,
  "totalPages": 3,
  "number": 0,
  "size": 20,
  "numberOfElements": 20,
  "first": true,
  "last": false
}
```

**Características:**
- Paginación de 20 elementos por página
- Ordenado por fecha de alta descendente (más recientes primero)
- Incluye toda la información del cliente asociado
- **SOLO consultas generales:** Campo mueble será null para todas las consultas devueltas
- **Filtro automático:** No devuelve consultas asociadas a muebles específicos

### 3. Obtener Solicitudes con Mueble Paginadas (GET)
**Endpoint:** `GET /api/v1/solicitarVisita/obtener-solicitudes-con-mueble/{pageNumber}`

**Descripción:** Obtiene todas las solicitudes de visita que SÍ tienen un mueble asociado, paginadas, ordenadas por fecha de creación (más recientes primero).

**Parámetros:**
- `pageNumber`: Número de página (0-indexed)
- Tamaño de página fijo: 20 elementos

**Filtros aplicados:**
- Solo solicitudes donde `mueble IS NOT NULL` (solicitudes asociadas a muebles específicos)

**Ejemplo de uso:**
```
GET /api/v1/solicitarVisita/obtener-solicitudes-con-mueble/0
```

**Respuesta:**
```json
{
  "content": [
    {
      "id": 124,
      "fechaHoraAltaSolicitarVisita": "2025-01-15 16:30:00",
      "consultaSolicitarVisita": "Me interesa este mueble específico",
      "cliente": {
        "id": 457,
        "nombreCliente": "María",
        "apellidoCliente": "García",
        "telefonoCliente": 987654321,
        "mailCliente": "maria.garcia@email.com",
        "estadoCliente": "PENDIENTE"
      },
      "mueble": {
        "id": 15,
        "nombreMueble": "Mesa de Comedor Roble",
        "colorMueble": "Marrón",
        "dimension": "160x90x75",
        "tipoMadera": "ROBLE",
        "precio": 2500.00,
        "descripcion": "Mesa de comedor en roble macizo para 6 personas",
        "categoria": {
          "id": 3,
          "nombreCategoria": "Comedor"
        }
      }
    }
  ],
  "totalElements": 30,
  "totalPages": 2,
  "number": 0,
  "size": 20,
  "numberOfElements": 20,
  "first": true,
  "last": false
}
```

**Características:**
- Paginación de 20 elementos por página
- Ordenado por fecha de alta descendente (más recientes primero)
- Incluye toda la información del cliente asociado
- **SOLO solicitudes con mueble:** Incluye información completa del mueble asociado
- **Filtro automático:** No devuelve consultas generales (sin mueble)

### 4. Marcar Solicitud como Finalizada (PUT)
**Endpoint:** `PUT /api/v1/solicitarVisita/marcar-finalizada/{solicitudId}`

**Descripción:** Permite al administrador marcar una solicitud como finalizada, cambiando el estado del cliente de `PENDIENTE` a `FINALIZADO`.

**Parámetros:**
- `solicitudId`: ID de la solicitud a finalizar

**Ejemplo de uso:**
```
PUT /api/v1/solicitarVisita/marcar-finalizada/123
```

**Respuesta exitosa:**
```json
{
  "mensaje": "Solicitud marcada como finalizada exitosamente",
  "solicitudId": 123,
  "clienteId": 456,
  "estadoAnterior": "PENDIENTE",
  "estadoActual": "FINALIZADO",
  "fechaFinalizacion": "2025-01-15T16:30:00"
}
```

**Respuesta de error (solicitud no encontrada):**
```json
{
  "error": "Solicitud no encontrada"
}
```

**Características:**
- **Solo para administradores:** Cambiar estado de cliente
- **Transaccional:** Si falla, no se realizan cambios
- **Auditoría:** Registra fecha de modificación en el cliente

### 5. Cambiar Estado de Solicitud (PUT)
**Endpoint:** `PUT /api/v1/solicitarVisita/{id}/estado`

**Descripción:** Cambia el estado de una solicitud específica a cualquier estado válido (PENDIENTE o FINALIZADO). Este endpoint es más genérico que el anterior y permite cambiar a cualquier estado válido.

**Parámetros:**
- `id`: ID de la solicitud

**Cuerpo de la petición (JSON):**
```json
{
  "estado": "FINALIZADO"
}
```

**Estados válidos:**
- `PENDIENTE`
- `FINALIZADO`

**Ejemplo de uso:**
```
PUT /api/v1/solicitarVisita/123/estado
Content-Type: application/json

{
  "estado": "FINALIZADO"
}
```

**Respuesta exitosa:**
```json
{
  "mensaje": "Estado de la solicitud cambiado exitosamente",
  "solicitudId": 123,
  "clienteId": 456,
  "estadoAnterior": "PENDIENTE",
  "estadoActual": "FINALIZADO",
  "fechaCambio": "2025-01-15T14:30:00"
}
```

**Respuesta cuando el estado ya es el solicitado:**
```json
{
  "mensaje": "El cliente ya tiene el estado solicitado",
  "solicitudId": 123,
  "clienteId": 456,
  "estadoActual": "FINALIZADO",
  "fechaConsulta": "2025-01-15T14:30:00"
}
```

**Errores posibles:**
- **400 Bad Request:** Estado no válido o solicitud sin cliente asociado
```json
{
  "error": "Estado no válido: INVALIDO. Estados válidos: PENDIENTE, FINALIZADO"
}
```

- **404 Not Found:** Solicitud no encontrada
```json
{
  "error": "Solicitud no encontrada"
}
```

**Características:**
- **Validación de estado:** Comprueba que el estado sea válido antes de procesarlo
- **Comparación inteligente:** No hace cambios si el estado ya es el solicitado
- **Flexibilidad:** Permite cambiar a cualquier estado válido
- **Transaccional:** Si falla, no se realizan cambios
- **Auditoría:** Registra fecha de modificación en el cliente
- **Validaciones:** Verifica que la solicitud y cliente existan

## Casos de Uso

### Crear Consulta
1. **Cliente nuevo:** Si el email no existe, se crea un nuevo cliente y la solicitud
2. **Cliente existente:** Si el email ya existe, se reutiliza el cliente existente y solo se crea la solicitud
3. **Manejo de errores:** Si algo falla, la transacción completa se revierte

### Obtener Consultas
1. **Paginación:** Ideal para mostrar consultas en interfaces paginadas
2. **Orden:** Las consultas más recientes aparecen primero
3. **Información completa:** Incluye datos del cliente para mostrar en la interfaz

### Obtener Solicitudes con Mueble
1. **Solicitudes específicas:** Muestra solicitudes asociadas a muebles concretos
2. **Información del mueble:** Incluye todos los datos del mueble (nombre, precio, categoría, etc.)
3. **Gestión de visitas:** Útil para el seguimiento de solicitudes de visita por muebles específicos
4. **Análisis de interés:** Permite ver qué muebles generan más solicitudes de visita

## Diferencias entre Endpoints

### `/crear-consulta` vs `/createSolicitarVisita`
- **crear-consulta:** Consultas generales sin mueble específico (mueble = null)
- **createSolicitarVisita:** Solicitudes asociadas a un mueble específico (mueble = objeto)

### `/obtener-consultas` vs `/obtener-solicitudes-con-mueble`
- **obtener-consultas:** Solo consultas generales (mueble IS NULL)
- **obtener-solicitudes-con-mueble:** Solo solicitudes con mueble (mueble IS NOT NULL)

### `/marcar-finalizada` vs `/{id}/estado`
- **marcar-finalizada:** Endpoint específico que siempre cambia el estado a FINALIZADO
- **/{id}/estado:** Endpoint genérico que permite cambiar a cualquier estado válido (PENDIENTE o FINALIZADO)

### Estados de Cliente - Flujo Completo
1. **Cliente hace consulta/solicitud:** Estado = `PENDIENTE`
2. **Administrador revisa:** Ve solicitudes en estado `PENDIENTE`
3. **Administrador gestiona:** Atiende la consulta del cliente
4. **Administrador finaliza:** 
   - Opción 1: Usa `PUT /marcar-finalizada/{id}` → Estado = `FINALIZADO`
   - Opción 2: Usa `PUT /{id}/estado` con `{"estado": "FINALIZADO"}` → Estado = `FINALIZADO`

## Endpoints por Rol

### Para Clientes/Frontend:
- `POST /crear-consulta` - Crear consultas generales
- `POST /createSolicitarVisita` - Crear solicitudes con mueble

### Para Administradores:
- `GET /obtener-consultas/{pageNumber}` - Ver consultas generales pendientes
- `GET /obtener-solicitudes-con-mueble/{pageNumber}` - Ver solicitudes con mueble pendientes
- `PUT /marcar-finalizada/{solicitudId}` - Marcar como finalizada (específico)
- `PUT /{id}/estado` - Cambiar estado (genérico)

## Estructura de Datos

### CrearConsultaDTO
- `cliente`: Objeto cliente completo
- `fechaHoraAltaSolicitarVisita`: Fecha y hora de la consulta
- `consultaSolicitarVisita`: Texto de la consulta del cliente

### CambiarEstadoDTO
- `estado`: String con el nuevo estado (`"PENDIENTE"` o `"FINALIZADO"`)

### Respuesta Paginada
- `content`: Array de objetos SolicitarVisita
- `totalElements`: Total de elementos en la BD
- `totalPages`: Total de páginas
- `number`: Número de página actual
- `size`: Tamaño de página
- `numberOfElements`: Elementos en la página actual
- `first`: Si es la primera página
- `last`: Si es la última página
