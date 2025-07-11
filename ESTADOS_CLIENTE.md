# Estados de Cliente - EstadoCliente Enum

## Estados disponibles:

### 1. **PENDIENTE**
- **Descripción:** Cliente con consulta o solicitud pendiente de gestión
- **Uso:** Estado por defecto para nuevos clientes que hacen consultas/solicitudes
- **Cuándo se asigna:** Al crear un cliente nuevo o cuando hace una nueva consulta
- **Responsabilidad:** El administrador debe gestionar esta solicitud

### 2. **FINALIZADO**
- **Descripción:** Cliente cuya consulta/solicitud ya fue gestionada por el administrador
- **Uso:** Para marcar que la petición del cliente fue atendida y resuelta
- **Cuándo se asigna:** Cuando el administrador completa la gestión de la solicitud
- **Responsabilidad:** El administrador marca como finalizado después de atender

## Flujo de trabajo:

```
Cliente hace consulta/solicitud → PENDIENTE
                ↓
Administrador gestiona la petición
                ↓
Administrador marca como → FINALIZADO
```

## Implementación:

### En el Backend:
```java
public enum EstadoCliente {
    PENDIENTE, FINALIZADO
}
```

### Estado por defecto:
- **Nuevos clientes:** Automáticamente se asigna `PENDIENTE`
- **Clientes sin estado:** Se establece `PENDIENTE` como fallback
- **Gestión:** El administrador cambia a `FINALIZADO` cuando atiende la solicitud

### En solicitudes JSON:
```json
{
    "nombreCliente": "Juan",
    "apellidoCliente": "Pérez",
    "estadoCliente": "PENDIENTE"
}
```

## Manejo de errores:
- Si se envía un estado inválido, se rechaza la deserialización
- Si no se envía estado, se asigna `PENDIENTE` automáticamente
- Estados válidos: `"PENDIENTE"`, `"FINALIZADO"`

## Casos de uso:

### Para el Frontend:
1. **Al crear consulta/solicitud:** Enviar `"PENDIENTE"` o no enviar estado (se asigna automáticamente)
2. **Panel de administración:** Mostrar lista de clientes `PENDIENTE` para gestionar
3. **Marcar como atendido:** Cambiar estado a `FINALIZADO` cuando se resuelve la consulta

### Para el Administrador:
1. **Dashboard:** Ver todas las consultas/solicitudes en estado `PENDIENTE`
2. **Gestionar:** Atender la consulta del cliente
3. **Finalizar:** Marcar como `FINALIZADO` cuando se complete la atención
