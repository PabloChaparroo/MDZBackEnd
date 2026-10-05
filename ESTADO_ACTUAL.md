# Estado actual del sistema — MDZ Muebles

> Este documento se actualiza cada vez que se implementa o modifica algo relevante en el backend o el frontend. Sirve como fuente de verdad del estado del proyecto para retomar trabajo sin tener que releer todo el código.

Última actualización: 2026-10-05

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

## 5.1 Datos de prueba (seed)

`src/main/java/com/Capinteria/carpinteria/Seed/DataSeeder.java` — carga 8 categorías (Comedor, Dormitorio, Living, Cocina, Oficina, Infantil, Jardín, Baño) con 13 muebles cada una (~104 en total), cada uno con nombre/color/descripción variados y una imagen de portada generada en el momento (no depende de archivos externos: dibuja un rectángulo de color sólido por categoría con el nombre del producto vía `Graphics2D`, lo codifica a PNG).

- Solo se activa con el perfil `seed`: `mvnw spring-boot:run "-Dspring-boot.run.profiles=local,seed"`.
- Es idempotente: si ya hay 100+ muebles en la base, no hace nada. Correrlo de nuevo no duplica datos.
- Reutiliza categorías existentes por nombre si ya estaban creadas (no crea duplicados).
- Verificado (2026-10-05): 104 muebles creados correctamente, imágenes PNG válidas, catálogo público y admin mostrando todo bien paginado.

---

## 6. Convenciones de trabajo con el usuario

- Commitear al terminar cada tarea/feature (no amontonar cambios sin commitear).
- Este archivo (`ESTADO_ACTUAL.md`) se debe mantener actualizado a medida que se implementan cosas nuevas — agregar una entrada o actualizar la sección correspondiente en vez de reescribir todo el documento.
- Verificación de cambios de UI/CSS: usar Playwright de forma efímera (instalar, probar con capturas y medición de `getBoundingClientRect`, desinstalar y limpiar al terminar) en vez de confiar solo en lectura de código.

---

## 7. Rediseño visual (en curso, iniciado 2026-10-03)

El usuario trajo un diseño de referencia hecho con otra IA (Lovable, stack TanStack Start + Tailwind + shadcn, repo `github.com/PabloChaparroo/luminous-living-spaces`) y pidió aplicar esa misma estética (fondo oscuro, tipografía Space Grotesk/DM Sans, acentos dorados) a este proyecto, agregando además modo claro, mientras se conservan los módulos de Catálogo, Admin y Perfil.

**Decisión de alcance** (confirmada con el usuario): el tema nuevo se aplica a **todo el sitio**, no solo a la Home. El formulario de contacto sigue usando el backend propio (`ConsultaService`), no el `mailto:` del diseño de referencia.

**Estrategia técnica**: como el repo de referencia usa Tailwind/shadcn (stack distinto al de este frontend, que es React + React-Bootstrap), no se portaron las dependencias — se extrajo la paleta de colores (variables CSS en `oklch`, con un set para oscuro y un set `.mdz-light` para claro) y se reescribió el CSS a mano con clases propias, todas prefijadas `mdz-` para no chocar con el CSS legado de Catálogo/Admin/Perfil.

**Hecho (fase 1 — fundación + Home)**:
- `src/styles/mdz-theme.css`: variables de color/tipografía + fuentes (DM Sans, Space Grotesk). Aplica `background`/`color` globalmente en `body`, por eso todo el sitio ya hereda el tema aunque sus propios componentes no estén rediseñados todavía.
- `src/styles/mdz-site.css`: todas las clases `mdz-*` del header, footer y home (botones, hero, grilla de productos, sección "nosotros", proceso, contacto).
- `src/hooks/useMdzTheme.ts`: toggle claro/oscuro, clase `mdz-light` en `<html>`, persistido en `localStorage` (`mdz-theme`).
- `Header.tsx`/`Footer.tsx`: reescritos con el nuevo diseño. El header dejó de ser `position: fixed` (ahora `sticky`), lo que de raíz elimina la clase de bugs de "header tapa el contenido en mobile" que se venía parcheando antes.
- `HomePage.tsx`: reescrita siguiendo la estructura del diseño de referencia (hero con carrusel real de fotos del taller, franja de beneficios, destacados del catálogo con datos reales de la API en vez de productos de ejemplo hardcodeados, sección "la madera es el comienzo", pasos del proceso de trabajo, formulario de contacto que sigue creando Cliente+Consulta vía la API).
- Imágenes del diseño de referencia copiadas a `src/assets/mdz-design/` (se usan como fallback/demo si hiciera falta, pero la Home usa principalmente fotos reales del taller).
- Verificado con Playwright en desktop y mobile, modo claro y oscuro: Home, Catálogo, "El taller" — nada roto, el resto de las páginas hereda el fondo/tipografía nuevos pero conserva su estilo propio (gold/negro) encima.

**Hecho (fase 2 — Catálogo, 2026-10-05)**:
- `CatalogoMueble.tsx` reescrito reutilizando los componentes `mdz-*` ya creados en la fase 1 (`mdz-product-grid`, `mdz-badge`, `mdz-filters`/`mdz-btn-filter`, `mdz-product-arrow`). Se agregaron a `mdz-site.css` las piezas nuevas que faltaban: `mdz-page-header` (encabezado de página interna), `mdz-search` (buscador tipo pill), `mdz-select` (dropdown de categoría en mobile), `mdz-empty-state`/`mdz-loading-state`.
- Se mantiene toda la lógica existente intacta (búsqueda con debounce, filtro por categoría, paginación con el componente `Pagination` ya existente, deep-link de categoría desde `ViewMueble`).
- `CatalogoMueble.css` (el archivo viejo) NO se tocó ni se borró porque `AbmCategoriaTabla.tsx` (Admin) todavía lo usa para su propio banner — se deja para la fase 3.
- De paso se corrigió un import de imágenes roto en `HomePage.tsx` (`racks`/`fotoAbout` apuntaban a archivos inexistentes por una edición externa al archivo).
- Verificado con Playwright: desktop claro/oscuro, búsqueda en vivo, mobile con el select de categorías — todo funcionando.

**Hecho (fase 3 — El taller, Admin y Perfil, 2026-10-05)**:
- `QuienesSomos.tsx`: reescrita con el sistema `mdz-*`. Se agregaron componentes nuevos reutilizables a `mdz-site.css`: `mdz-card`/`mdz-card-grid` (servicios), `mdz-value-grid` (valores), `mdz-timeline` (historia), `mdz-stat-row` (estadísticas), `mdz-map`, `mdz-social-row`, `mdz-cta` (banda final).
- `AdministrarSolicitud.tsx` (consultas) y `AbmCategoriaTabla.tsx` (categorías y muebles): reescritas reutilizando `mdz-page-header`, `mdz-filters`/`mdz-btn-filter` (tabs/chips), `mdz-search`. Se agregó un sistema de tabla propio (`mdz-table-wrap`, `mdz-table`, `mdz-pill` para estados, `mdz-icon-btn` para acciones, `mdz-empty-table`) que reemplaza las tablas Bootstrap `professional-table` viejas. Toda la lógica de datos (paginación, filtros, búsqueda, copiar links de redes) quedó intacta, solo cambió el markup/CSS.
- `PerfilUsuario.tsx`: tarjeta de perfil nueva (`mdz-profile-card`, avatar con iniciales, grilla de datos) y modal de confirmación de logout propio (`mdz-confirm-overlay`/`mdz-confirm-card`) en vez del modal custom viejo.
- Modales compartidos (`CategoriaModal`, `CatalogoModalMueble`, `ModalGestionImagenes`, `ImagenesModal`, `ResponderConsultaModal`, `VerMuebleModal`): se les agregó el tema `mdz-modal-content` (fondo/texto/inputs oscuros o claros según el tema activo) vía la prop `contentClassName` de React-Bootstrap `Modal`, sin reescribir toda su lógica interna.
- CSS legado eliminado por quedar sin uso: `AbmCategoriaTabla.css`, `AdministrarSolicitud.css`, `CatalogoMueble.css`, `QuienesSomos.css`, `PerfilUsuario_new.css`.
- Verificado con Playwright (con un token simulado en localStorage para ver el shell de las páginas admin sin backend autenticado real): desktop y mobile, claro y oscuro, en El taller, Admin Categorías (incluida la tabla de muebles de una categoría), Admin Consultas y Perfil.

**Hecho (fase 4 — Ficha de producto / ViewMueble, 2026-10-05)**:
- `ViewMueble.tsx` reescrita con el sistema `mdz-*`: galería principal + miniaturas (`mdz-gallery-main`/`mdz-gallery-thumbs`), specs (`mdz-spec-row`), formulario de consulta reutilizando `mdz-contact-form` (misma lógica: crea Cliente+Consulta asociada a ese mueble puntual vía `ConsultaService.crearConsulta`), sidebar con rating/features/redes/categorías/pasos (`mdz-sidebar-card`), modal de imagen a pantalla completa (`mdz-fullscreen-overlay`) y modal de "consulta enviada" (`mdz-confirm-overlay`).
- Se simplificó el efecto de zoom-al-pasar-el-mouse sobre la imagen principal (tenía console.logs de debug y manipulaba el DOM directamente de forma poco confiable) a favor de click-para-pantalla-completa, que ya existía y es más predecible. Si en algún momento se quiere un zoom hover de vuelta, hay que reimplementarlo sobre `.mdz-gallery-main img`.
- Se eliminó `ViewMueble.css` por quedar sin uso.
- Verificado con Playwright: navegando real desde el catálogo (con el `state` de React Router que esta página necesita), desktop/mobile, claro/oscuro, miniaturas, pantalla completa.

**Hecho (fase 5 — pulido final de modales, 2026-10-05)**:
- `CatalogoModalMueble.tsx`, `ModalGestionImagenes.tsx`, `ImagenesModal.tsx`: los `<Button>`/`<span class="badge">` de Bootstrap que quedaban con estilo por defecto (blanco/gris) dentro de los modales se reemplazaron por `mdz-btn`/`mdz-pill`/`mdz-icon-btn`.
- `ModalGestionImagenes.css` tenía colores hardcodeados pensados solo para fondo claro (blancos, grises de Bootstrap) que no se adaptaban al modal oscuro; se agregó un bloque de overrides scoped a `.mdz-modal-content` que los reemplaza por las variables del tema sin tocar el resto del archivo (layout, animaciones, responsive).
- Verificado con Playwright: modal "Crear nuevo mueble" (con los campos de Instagram/Facebook), modal de selección de imágenes y modal de gestión de imágenes de un mueble existente, en claro y oscuro.

**Con esto el rediseño visual está completo en todo el sitio** (Home, Header, Footer, Catálogo, ficha de producto, El taller, Admin, Perfil, y todos los modales) con una única paleta, tipografía (DM Sans/Space Grotesk) y toggle claro/oscuro compartidos, consistente de punta a punta.
