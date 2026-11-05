# Estado actual del sistema — MDZ Muebles

> Este documento se actualiza cada vez que se implementa o modifica algo relevante en el backend o el frontend. Sirve como fuente de verdad del estado del proyecto para retomar trabajo sin tener que releer todo el código.

Última actualización: 2026-11-05

---

## 1. Arquitectura general

- **Backend**: Java 17, Spring Boot 3.2.3, Spring Data JPA/Hibernate (`ddl-auto=validate`, el esquema lo maneja Flyway), Spring Security con JWT, Spring Mail.
  Ruta: `c:\Users\Pablo\Desktop\Desarrollo\MDZMuebles\BackEnd\MDZBackEnd`
- **Frontend**: React + TypeScript + Vite, React Router, React Bootstrap, Formik + Yup, react-toastify.
  Ruta: `c:\Users\Pablo\Desktop\Desarrollo\MDZMuebles\FrontEnd\MDZFrontEnd`
- **Base de datos**: PostgreSQL 16 (Docker, `compose.dev.yaml`, `localhost:5436`, base/usuario/clave `mdzmueblesbd`/`mdz`/`mdz`). Migraciones con Flyway en `src/main/resources/db/migration/`.
- **Mail de desarrollo**: Mailpit (SMTP fake) vía Docker. UI web: `http://localhost:8125`. SMTP en puerto `1125` (remapeado por conflicto con otro proyecto en la misma máquina).
- **Orquestación local**: `npm run dev` (script `scripts/dev.js`) levanta Docker (PostgreSQL + Mailpit) y después `mvnw spring-boot:run -Dspring-boot.run.profiles=local`.
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

`src/main/java/com/Capinteria/carpinteria/Seed/DataSeeder.java` — carga 98 muebles con fotos reales en 8 categorías (Cama, Lavatorios, Cocinas, Placares, Escritorios, Estantes, Bibliotecas, Racks), leyendo `src/main/resources/seed/muebles/` (`catalogo.csv` más un JPG por mueble).

- Solo se activa con el perfil `seed`: `mvnw spring-boot:run "-Dspring-boot.run.profiles=local,seed"`.
- Es idempotente: saltea los muebles que ya existen con el mismo nombre en su categoría. Correrlo de nuevo no duplica datos.
- Reutiliza categorías existentes por nombre si ya estaban creadas (no crea duplicados).
- Las fotos son de Wikimedia Commons, con autor y licencia en `seed/muebles/CREDITOS.md`. Son datos de prueba: algunas licencias exigen atribución, así que hay que revisarlas antes de usarlas en producción. Las fotos no son productos de MDZ; nombre, color y descripción son inventados a partir de cada foto.
- Las 104 placeholders del seeder anterior siguen en la base local: esta versión ya no las genera, pero tampoco las borra.
- Verificado (2026-11-01): 98 muebles creados con imagen en la base local.

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

**Hecho (fase 6 — Paginación, El taller con imagen, 2026-10-05)**:
- `Pagination.tsx`: rediseñado con el sistema `mdz-*` (`mdz-pagination`, `mdz-pagination-btn`, estado `is-active`). Antes tenía estilo propio viejo (tarjeta gris oscura, pill amarilla) que desentonaba; se usa en Catálogo y en las tablas de Admin. Se eliminó `Pagination.css`.
- `QuienesSomos.tsx`: la sección "Nuestra historia" (texto + estadísticas 15+/500+/100%) dejaba un hueco vacío a la derecha; se agregó la foto del taller (`fotoAbout.jpg`, ya usada en Home) al lado, con layout `mdz-about-text-stats` (texto+stats apilados a la izquierda, imagen a la derecha).

**Hecho (fase 7 — Menú mobile como drawer + animación entre páginas, 2026-10-05)**:
- `Header.tsx`: el menú mobile pasó de ser un dropdown que empujaba el contenido hacia abajo a un **drawer lateral que se desliza desde la izquierda** (`mdz-mobile-nav` + `mdz-mobile-backdrop`), con fondo oscuro detrás, bloqueo de scroll del body (`body.mdz-no-scroll`) y cierre al tocar el fondo, la X, o al navegar. Queda siempre montado en el DOM y se anima por `transform` + clase `is-open` (permite animar tanto apertura como cierre).
- `App.tsx`: se agregó `PageTransition` (wrapper con `key={location.pathname}` + clase `mdz-page-transition`) alrededor de `<AppRoutes/>`, que dispara una animación sutil de fade/slide (`mdz-page-enter`) cada vez que cambia de ruta. Respeta `prefers-reduced-motion`.

**Hecho (fase 8 — Detalle visual "diagonal", 2026-10-07)**:
- Al usuario le gustó el degradé del banner de Mi Perfil y pidió reutilizar ese detalle en los títulos de los modales. Se creó la clase reutilizable `.mdz-diagonal-detail` (degradé `--mdz-secondary` → `--mdz-accent` + textura de rayas diagonales sutiles vía `repeating-linear-gradient`), aplicada a:
  - El banner de Mi Perfil (de donde salió el detalle original).
  - Los `mdz-page-header` de Catálogo, Admin Categorías, Admin Consultas y El taller.
  - El `.modal-header` de **todos** los modales (vía `.mdz-modal-content .modal-header`, sin tocar cada modal individualmente ya que todos comparten esa clase).

### Backend — limpieza de warnings (2026-10-05/07)
- Se agregaron campos `linkInstagram`/`linkFacebook` opcionales a `Mueble` (ver sección 2), con su endpoint y formulario ya cubiertos en el admin.
- `DataSeeder` (ver 5.1) agregado y verificado con 104 muebles; reemplazado el 2026-11-01 por la versión con 98 muebles y fotos reales.
- Se resolvieron los 25 warnings de nulabilidad que aparecían en el panel "Problems" de VS Code:
  - 19 eran "unchecked conversion" (falsos positivos) causados por `java.compile.nullAnalysis.mode: "automatic"` en `.vscode/settings.json` (no versionado, en `.gitignore`) — se cambió a `"disabled"` localmente, y además se agregó `@SuppressWarnings("null")` a nivel de clase en `BaseSeriviceImpl`, `CategoriaServiceImpl`, `ConsultaServiceImpl`, `MuebleServiceImpl` y `JwtService` para que la corrección no dependa de que cada quien reinicie el Language Server.
  - 6 eran reales: `CorsConfig.addCorsMappings` y `JwtAuthenticationFilter.doFilterInternal` sobreescriben métodos de `WebMvcConfigurer`/`OncePerRequestFilter` cuyos parámetros están declarados `@NonNull` en la clase padre; se agregó la anotación correspondiente.
  - Importante: un primer intento de agregar `@NonNull` también a `BaseSeriviceImpl.findAll(Pageable)` generó un error nuevo ("Illegal redefinition of parameter") porque la interfaz `BaseService` no lo declara así — se revirtió ese cambio puntual.

**Hecho (pulido de la Home, 2026-11-01)**:
- `HomePage.tsx`: hero a pantalla completa con segundo botón e indicadores, destacados en mosaico con un mueble por categoría (antes eran solo los primeros 6 del catálogo), enlace al catálogo y aparición al hacer scroll con `IntersectionObserver` (`mdz-reveal`). Se quitó el filtro de categorías de la Home.
- `mdz-site.css`: estilos nuevos acotados a `.mdz-home`, sin cambios en otras páginas.
- Verificado con Playwright en desktop oscuro/claro y mobile.

**Hecho (Novedades, 2026-11-01)**:
- `Mueble.publicar` (boolean, por defecto false): se define solo al crear el mueble. La columna es `updatable = false`, así que una edición no puede cambiarlo.
- Backend: `GET /api/v1/mueble/novedades/{pageNumber}` es público, devuelve 9 por página, solo publicados y activos, del más nuevo al más viejo.
- Frontend: check "Publicar en Novedades" en el modal de alta, página `/novedades` (`Novedades.tsx`, clases `mdz-feed`/`mdz-post`) y enlace en el menú. Cada publicación muestra categoría, fecha de alta, foto, nombre y color.
- Los muebles existentes y los del seed quedan sin publicar.

**Hecho (aviso por mail de consultas, 2026-11-01)**:
- `EmailService.notificarNuevaConsulta`: cada consulta creada (general o de un mueble) avisa a `mdz.mail.admin`, con el cliente como Reply-To. Se envía en segundo plano; si falla, solo queda en el log.
- `application.properties` y `docker-compose.yml` toman el SMTP de `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` y `MAIL_ADMIN`. En local sigue yendo a Mailpit (http://localhost:8125).
- Verificado con Mailpit para ambos tipos de consulta. Falta configurar un SMTP real.

**Teléfono como texto (2026-11-01)**: `Cliente.telefonoCliente` pasó a `String` (columna `VARCHAR(30)`), porque 10 dígitos no entraban en `int` y fallaba la consulta. (Ese script manual V4 se eliminó al pasar a PostgreSQL + Flyway, ver abajo.)

**Optimización previa a producción (2026-11-01)**:
- Frontend: imágenes en WebP, rutas con carga diferida, `/perfil` y pantallas admin detrás de `PrivateRoute`, gzip y caché en nginx, sin archivos ni dependencias sin uso.
- Backend: `GET /api/v1/mueble/destacados` para la Home; `catalogo-completo` devuelve solo la portada; `GET /api/v1/mueble` pasó a ser solo ADMIN.
- El admin inicial usa `ADMIN_PASSWORD` (sin definir, no se crea). En local queda en `application-local.properties`.

**Mobile (2026-11-01)**: auditoría de todas las pantallas en 360 y 390px. Hasta 640px `.mdz-filters` queda oculto salvo con `.mdz-filters-scroll` (admin). Hasta 760px las tablas se muestran como tarjetas (`mdz-cards-categoria`, `mdz-cards-mueble`, `mdz-cards-consulta`, con etiquetas por posición de columna en CSS: si cambian las columnas hay que actualizarlas) y los campos usan 16px para evitar el zoom de iOS.

**Fotos (2026-11-01)**: `src/utils/comprimirImagen.ts` comprime en el navegador las fotos de más de 1 MB (lado mayor hasta 2000 px, JPEG 85%; PNG sigue en PNG). Mantiene la proporción y, si no puede achicarla, sube la original. El límite por archivo es 10 MB, igual que el backend. La galería de la ficha usa `object-fit: contain` (no recorta); portadas, Novedades y miniaturas siguen con `cover`.

**PostgreSQL + Flyway (2026-11-05)**: la base pasó de MySQL a PostgreSQL 16 y el esquema ahora lo maneja Flyway.
- `pom.xml`: `org.postgresql:postgresql` en lugar de `mysql-connector-j`, más `flyway-core` y `flyway-database-postgresql`. Flyway fijado en **10.22.0** (propiedad `flyway.version`; ver abajo).
- `ddl-auto=validate` en los dos perfiles: Hibernate ya no crea ni modifica tablas, solo verifica que coincidan con las entidades. **Cualquier cambio de entidad necesita una migración nueva** (`V2__....sql`); si falta, la app no arranca.
- `db/migration/V1__esquema_inicial.sql`: las 6 tablas, armadas a partir del DDL que genera Hibernate para Postgres. Se borraron los scripts manuales V1–V4 de MySQL (siguen en el historial de git). No se migraron datos.
- Puerto del host 5436, porque 5432 lo usa un Postgres nativo y 5434/5435 otros proyectos.
- `MuebleImagenes.imagenes`: columna `bytea` sin `@Lob`. Con `@Lob`, Hibernate usa `oid` en Postgres y la consulta nativa del catálogo devolvería un número en vez de la imagen.
- SQL nativo: el `LIKE` del buscador pasó a `ILIKE` (consulta nativa) y a `LOWER(...)` (JPQL), porque Postgres distingue mayúsculas y MySQL no. Además se corrigieron 3 consultas nativas que ya estaban rotas: `ClienteRepository.searchById` y `UsuarioRepository.findUserByUsername` (comparaban `= %:x%`) y `findClienteByUsername` (usaba la columna eliminada `estado_cliente`).
- **Pendiente**: `docker-compose.yml` (stack completo containerizado) sigue con MySQL y no funciona con este backend hasta actualizarlo. `application.properties` ya apunta a Postgres por variables `DB_*`.
- **Verificación completa (2026-11-05)**, contra Postgres y revisando la base después de cada operación:
  - Modificar categoría y mueble, agregar imagen, cambiar portada, borrar imagen, responder consulta (guarda la respuesta y el mail llega a Mailpit) y dar de baja un mueble (sale del catálogo y aparece en dados de baja): todo OK.
  - Baja de categoría: OK sobre una categoría vacía. Una categoría cuyos muebles ya están todos dados de baja **no se puede dar de baja**, porque `CategoriaRepository.countMuebles...` cuenta también los muebles dados de baja. Viene de antes, no lo causó Postgres. Pendiente decidir si se corrige.
  - Perfil por defecto (`application.properties` con `DB_HOST=localhost DB_PORT=5436 DB_NAME/DB_USER/DB_PASSWORD`): arranca y conecta. Flyway valida V1 y no aplica nada nuevo, y Hibernate valida el esquema.
  - Aviso de Flyway: 9.22.3 soporta oficialmente hasta PostgreSQL 15 y el contenedor usa 16. **Resuelto** con la subida a Flyway 10 (ver abajo).
  - Detalle aparte: `GET /api/v1/categoria/dadas-de-baja` devuelve el `ResponseEntity` serializado dentro del cuerpo (`{"headers":..,"body":[..]}`). Viene de antes.

**Flyway 10 (2026-11-05)**: se pasó de 9.22.3 (la que trae Spring Boot 3.2.3) a **10.22.0**, la última 10.x, porque 9.x no soporta PostgreSQL 16.
- `pom.xml`: propiedad `<flyway.version>10.22.0</flyway.version>`, que pisa la versión que gestiona Spring Boot, y la dependencia `flyway-database-postgresql` con `<version>${flyway.version}</version>`. Spring Boot 3.2 no gestiona ese módulo, así que la versión tiene que ir explícita.
- Verificado con `npm run dev` sobre la base con V1 ya aplicada: "Successfully validated 1 migration", "Schema public is up to date", Hibernate valida y la app arranca. Ya no aparece el warning "Flyway upgrade recommended". Ojo: en el perfil `local` el log de Flyway está en ERROR, así que para ver sus mensajes hay que correr con `LOGGING_LEVEL_ORG_FLYWAYDB=INFO`.
- Cuando se actualice Spring Boot a 3.3 o más (que ya trae Flyway 10), se pueden sacar la propiedad y la versión explícita.
