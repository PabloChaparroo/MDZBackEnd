# 🔐 CONFIGURACIÓN DE SEGURIDAD Y PERMISOS

## 📋 **Resumen de Permisos por Controlador**

### 🔓 **Rutas Públicas (Sin Autenticación)**
- **`/auth/**`** - Login, registro y autenticación

### 🪑 **MUEBLES (`/api/v1/mueble/**`)**
- **GET** 🟢 - **PÚBLICO** (Cualquier usuario puede ver el catálogo)
  - `/obtener-todos-muebles/{pageNumber}`
  - `/muebles-por-categoria/{pageNumber}/categoria/{categoriaId}`
  - `/catalogo-completo/{pageNumber}`
  - `/filtrar/buscar/{filtro}/{pageNumber}`
  - `/dados-de-baja/{pageNumber}`
  - `/{id}/imagenes`
  - Y todos los demás GETs

- **POST/PUT/DELETE** 🔴 - **SOLO ADMIN**
  - `POST /create` - Crear muebles
  - `PUT /modificar/{id}` - Modificar muebles
  - `DELETE /baja-logica/{id}` - Dar de baja muebles
  - `POST /{id}/agregar-imagenes` - Agregar imágenes
  - `PUT /imagen/{imagenId}/establecer-portada` - Cambiar portada
  - `DELETE /imagen/{imagenId}` - Eliminar imágenes

### 🏷️ **CATEGORÍAS (`/api/v1/categoria/**`)**
- **GET** 🟢 - **PÚBLICO** (Ver categorías disponibles)
- **POST/PUT/DELETE** 🔴 - **SOLO ADMIN** (Gestión de categorías)

### 🖼️ **IMÁGENES DE MUEBLES (`/api/v1/muebleImagenes/**`)**
- **GET** 🟢 - **PÚBLICO** (Ver imágenes)
- **POST/PUT/DELETE** 🔴 - **SOLO ADMIN** (Gestión de imágenes)

### 📅 **SOLICITAR VISITA (`/api/v1/solicitarVisita/**`)**
- **TODOS LOS MÉTODOS** 🔴 - **SOLO ADMIN**
  - GET, POST, PUT, DELETE - Gestión completa de visitas

### 👥 **CLIENTES (`/api/v1/cliente/**`)**
- **TODOS LOS MÉTODOS** 🔴 - **SOLO ADMIN**
  - GET, POST, PUT, DELETE - Gestión completa de clientes

### 👤 **USUARIOS (`/api/v1/usuarios/**`)**
- **TODOS LOS MÉTODOS** 🔴 - **SOLO ADMIN**
  - GET, POST, PUT, DELETE - Gestión completa de usuarios

## 🔑 **Roles Disponibles**
- **ADMIN** - Acceso completo al sistema
- **EMPLEADO** - Sin permisos específicos configurados (tratado como usuario autenticado)
- **CLIENTE** - Sin permisos específicos configurados (tratado como usuario autenticado)

## 🚀 **Casos de Uso**

### 👀 **Usuario Anónimo (Sin Login)**
```bash
# ✅ PERMITIDO - Ver catálogo
GET /api/v1/mueble/obtener-todos-muebles/0
GET /api/v1/mueble/filtrar/buscar/mesa/0
GET /api/v1/categoria

# ❌ PROHIBIDO - Modificar datos
POST /api/v1/mueble/create
PUT /api/v1/mueble/modificar/1
DELETE /api/v1/mueble/baja-logica/1
```

### 👑 **Usuario ADMIN**
```bash
# ✅ PERMITIDO - TODO
GET /api/v1/mueble/obtener-todos-muebles/0
POST /api/v1/mueble/create
PUT /api/v1/mueble/modificar/1
DELETE /api/v1/mueble/baja-logica/1
GET /api/v1/cliente
POST /api/v1/solicitarVisita
```

### 👷 **Usuario EMPLEADO/CLIENTE**
```bash
# ✅ PERMITIDO - Solo ver catálogo público
GET /api/v1/mueble/obtener-todos-muebles/0
GET /api/v1/categoria

# ❌ PROHIBIDO - Gestión administrativa
POST /api/v1/mueble/create
GET /api/v1/cliente
POST /api/v1/solicitarVisita
```

## 🔐 **Autenticación**

### **Login**
```bash
POST /auth/login
{
    "username": "admin@example.com",
    "password": "password123"
}
```

### **Usar Token**
```bash
GET /api/v1/mueble/dados-de-baja/0
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## ⚠️ **Códigos de Respuesta de Seguridad**
- **200** - Acceso permitido
- **401** - No autenticado (sin token o token inválido)
- **403** - No autorizado (token válido pero sin permisos)
- **404** - Recurso no encontrado

## 🛡️ **Configuración Técnica**
- **Filtro JWT**: Procesa tokens en cada request
- **Session Policy**: STATELESS (sin sesiones)
- **CORS**: Configurado para localhost:8080
- **CSRF**: Deshabilitado (API REST)

## 📝 **Notas Importantes**
1. Los usuarios deben hacer login para acceder a funcionalidades administrativas
2. El catálogo de muebles y categorías es público para facilitar la navegación
3. Toda la gestión de datos sensibles requiere permisos de ADMIN
4. Los tokens JWT tienen una duración limitada (configurada en JwtService)
