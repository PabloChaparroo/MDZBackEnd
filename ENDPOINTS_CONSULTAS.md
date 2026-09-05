# Documentación de Endpoints - Consultas

Reemplaza al antiguo módulo "Solicitar Visita" (`ENDPOINTS_SOLICITAR_VISITA.md`, eliminado). El sistema ya no permite "solicitar visita": el cliente hace una **consulta** sobre un mueble puntual del catálogo. No existe más el concepto de estado del cliente (`EstadoCliente` fue eliminado).

## Endpoints disponibles

### 1. Crear Consulta (POST)
**Endpoint:** `POST /api/v1/consultas/crear`

**Descripción:** Crea una consulta de un cliente sobre un mueble específico. Si el cliente ya existe (por email), se reutiliza; si no, se crea uno nuevo.

**Cuerpo de la petición (JSON):**
```json
{
  "cliente": {
    "nombreCliente": "Juan",
    "apellidoCliente": "Pérez",
    "telefonoCliente": 123456789,
    "mailCliente": "juan.perez@email.com"
  },
  "muebleId": 15,
  "mensajeConsulta": "Me interesa este mueble, ¿lo hacen en otras medidas?"
}
```

**Respuesta exitosa:**
```json
{
  "mensaje": "Consulta creada exitosamente",
  "consultaId": 123,
  "clienteId": 456,
  "clienteNuevo": true
}
```

**Características:**
- Requiere `muebleId` de un mueble existente (400 si no existe).
- Verifica si el cliente existe por email antes de crear uno nuevo.
- Transaccional: si algo falla, no se guarda nada.

### 2. Obtener Consultas Paginadas (GET, solo ADMIN)
**Endpoint:** `GET /api/v1/consultas/obtener-consultas/{pageNumber}`

Todas las consultas, paginadas de a 20, ordenadas por fecha de alta descendente. Incluye el cliente y el mueble consultado completos.

### 3. Filtrar por nombre de cliente (GET, solo ADMIN)
**Endpoint:** `GET /api/v1/consultas/filtrar-por-nombre?nombre=Juan&pagina=0&tamanoPagina=20`

## Endpoints por rol

### Para clientes/frontend:
- `POST /api/v1/consultas/crear` — público, sin autenticación.

### Para administradores:
- `GET /api/v1/consultas/obtener-consultas/{pageNumber}`
- `GET /api/v1/consultas/filtrar-por-nombre`
- `GET /api/v1/consultas/{id}` y `GET /api/v1/consultas` (heredados de `BaseControllerImpl`)

## Estructura de datos

### CrearConsultaDTO
- `cliente`: objeto cliente (se crea o reutiliza por email)
- `muebleId`: id del mueble consultado (obligatorio)
- `mensajeConsulta`: texto de la consulta

### Entidad Consulta
- `id`, `mensajeConsulta`, `fechaHoraAltaConsulta`, `fechaHoraModificacionConsulta`
- `cliente` (`@ManyToOne`, obligatorio)
- `mueble` (`@ManyToOne`, obligatorio)

## Qué se eliminó respecto a "Solicitar Visita"

- El endpoint separado `createSolicitarVisita` (mueble opcional) — ahora `muebleId` es siempre obligatorio en `/crear`.
- `obtener-solicitudes-con-mueble` — ya no tiene sentido, todas las consultas tienen mueble.
- `marcar-finalizada` y `PUT /{id}/estado` — dependían de `EstadoCliente`, que fue eliminado. El cliente ya no tiene un estado de gestión.
