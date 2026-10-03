# Estado actual del sistema — MDZ Muebles

> Este documento se actualiza cada vez que se implementa o modifica algo relevante en el backend o el frontend. Sirve como fuente de verdad del estado del proyecto para retomar trabajo sin tener que releer todo el código.

Última actualización: 2026-10-03

---

## 1. Arquitectura general

- **Backend**: Java 17, Spring Boot 3.2.3, Spring Data JPA/Hibernate (`ddl-auto=update` en perfil `local`), Spring Security con JWT, Spring Mail.
  Ruta: `c:\Users\Pablo\Desktop\Desarrollo\MDZMuebles\BackEnd\MDZBackEnd`
- **Frontend**: React + TypeScript + Vite, React Router, React Bootstrap, Formik + Yup, react-toastify.
  Ruta: `c:\Users\Pablo\Desktop\Desarrollo\MDZMuebles\FrontEnd\MDZFrontEnd`
- **Base de datos**: MySQL 8.0 (Docker, `compose.dev.yaml`), migraciones manuales en `src/main/resources/db/migration/`.
- **Mail de desarrollo**: Mailpit (SMTP fake) vía Docker. UI web: `http://localhost:8125`. SMTP en puerto `1125` (remapeado por conflicto con otro proyecto en la misma máquina).
- **Orquestación local**: `npm run dev` (script `scripts/dev.js`) levanta Docker (MySQL + Mailpit) y después `mvnw spring-boot:run -Dspring-boot.run.profiles=local`.
- **Rama de trabajo activa**: `deploy`.

---

## 2. Entidades principales (backend)

### Mueble
- Campos: `nombreMueble`, `colorMueble`, `descripcion`, `fechaAltaMueble`, `fechaModificacionMueble`, `fechaBajaMueble`.
- `linkInstagram` (opcional), `linkFacebook` (opcional) — **NUEVO** (2026-10-03): links directos a publicaciones en redes sociales. No requeridos al crear, editables después.
- Relación `ManyToOne` con `Categoria`.
- Relación `OneToMany` con `MuebleImagenes` (galería de imágenes, una marcada como portada vía `esPortada`).
- Ya NO tiene: `dimension`, `precio`, `tipoMadera` (eliminados en el refactor de Consulta).

### Categoria
- ABM simple con alta/baja/modificación.

### Cliente
- Campos: `nombreCliente`, `apellidoCliente`, `telefonoCliente`, `mailCliente`, fechas de alta/modificación/baja.
- Ya NO tiene `estadoCliente` (eliminado en el refactor de Consulta).

### Consulta (reemplaza a la vieja `SolicitarVisita`)
- Campos: `mensajeConsulta`, `fechaHoraAltaConsulta`, `fechaHoraModificacionConsulta`.
- `respuesta` (TEXT, opcional): texto de respuesta del admin a la consulta.
- Relación `ManyToOne` con `Cliente` (obligatoria: se crea un cliente al llenar el formulario).
- Relación `ManyToOne` con `Mueble` (**opcional**, `nullable = true`): si es `null` es una consulta general (ej. formulario de contacto de la Home); si tiene mueble, es una consulta puntual sobre ese producto.

### Usuario
- Login admin con JWT (access + refresh token).

---

## 3. Funcionalidades implementadas

### 3.1 Catálogo público
- Listado paginado de muebles por categoría o completo.
- Filtro/búsqueda por nombre o color.
- Vista de detalle de mueble (`ViewMueble`) con galería de imágenes y categorías relacionadas (links a `/catalogo` con el estado de la categoría preseleccionada).
- Formulario de consulta sobre un mueble puntual (crea `Cliente` + `Consulta` asociada a ese mueble).

### 3.2 Home
- Hero con carrusel de imágenes rotativo (bug de "la imagen deja de verse después de mucho tiempo" **resuelto**: se cambió el remount vía `key` por reinicio de animación vía `ref` + reflow, evitando el memory/DOM churn).
- Formulario de contacto general (crea `Cliente` + `Consulta` **sin** mueble asociado).

### 3.3 Panel de administración — Categorías y Muebles (`AbmCategoriaTabla`)
- ABM de categorías (alta, edición, baja lógica, listado con filtro de dadas de baja).
- ABM de muebles por categoría: alta (con carga de imágenes y selección de portada), edición, baja lógica, gestión de imágenes (agregar/eliminar/cambiar portada), búsqueda por nombre.
- Columna **"Redes"** (**NUEVO**, 2026-10-03): íconos de Instagram/Facebook que aparecen solo si el mueble tiene el link cargado. Al hacer clic se copia el link al portapapeles (no se abre la red social), con feedback visual (check verde momentáneo).
- Formulario de alta/edición de mueble: incluye ahora dos campos opcionales "Link de Instagram" y "Link de Facebook" (con validación de formato URL solo si se completan).

### 3.4 Panel de administración — Consultas (`AdministrarSolicitud`)
- Reemplaza al viejo módulo "Solicitar Visita".
- Dos pestañas: "Consultas de Muebles" (con `mueble` asociado) y "Consultas Generales" (sin mueble).
- Columna "Respuesta" con badge: `Pendiente` (ámbar) / `Respondida` (verde).
- Acciones por fila:
  - "Ver mueble" (solo si la consulta tiene mueble asociado): modal con datos e imagen del mueble consultado.
  - "Responder" / "Ver respuesta": modal para escribir y enviar una respuesta por mail (SMTP de prueba vía Mailpit) y guardarla en la consulta. Si el envío de mail falla, no se persiste la respuesta (el mail se manda antes de guardar).
- Búsqueda por nombre de cliente, paginado.

### 3.5 Autenticación
- Login con JWT (access + refresh token), perfil de usuario, rutas protegidas para el panel admin.

---

## 4. Decisiones de diseño relevantes

- **Consulta.mueble es opcional**: permite soportar tanto consultas puntuales sobre un producto del catálogo como consultas generales de contacto, sin duplicar entidades.
- **Respuesta a consulta**: se envía por mail ANTES de persistir en la base — si el SMTP falla, no queda una respuesta "fantasma" guardada sin haber llegado al cliente.
- **SMTP de desarrollo**: Mailpit (no Gmail), para no mandar mails reales mientras se prueba. Cambiar a un proveedor real es solo cuestión de reemplazar las 4 propiedades `spring.mail.*` en `application-local.properties` (ya documentado ahí mismo con comentario).
- **Links de redes sociales del mueble**: se decidió que el ícono en la tabla admin copie el link al portapapeles en vez de abrir la red social directamente (pedido explícito del usuario).

---

## 5. Problemas conocidos resueltos recientemente

- Bug de imágenes del hero que dejaban de renderizar tras mucho tiempo → resuelto (ver 3.2 Home).
- Catálogo mobile quedando tapado detrás del header fijo → resuelto ajustando `body { padding-top }` en los breakpoints `≤767.98px` y `≤991.98px` de `Header.css` para que coincida con la altura real del stack topbar+navbar.
- Botón "Mi Perfil" pegado a la izquierda en mobile (en vez de alineado a la derecha) → resuelto con `.topbar-social { margin-left: auto; }` dentro del media query mobile.
- Badge "Pendiente" con estilo gris apagado → cambiado a estilo ámbar (`bg-warning`) ya existente en el CSS.
- Bug preexistente en `obtenerImagenesMueble` que devolvía forma de respuesta inconsistente (objeto en vez de array cuando no había imágenes) → corregido.

---

## 6. Convenciones de trabajo con el usuario

- Commitear al terminar cada tarea/feature (no amontonar cambios sin commitear).
- Este archivo (`ESTADO_ACTUAL.md`) se debe mantener actualizado a medida que se implementan cosas nuevas — agregar una entrada o actualizar la sección correspondiente en vez de reescribir todo el documento.
- Verificación de cambios de UI/CSS: usar Playwright de forma efímera (instalar, probar con capturas y medición de `getBoundingClientRect`, desinstalar y limpiar al terminar) en vez de confiar solo en lectura de código.
