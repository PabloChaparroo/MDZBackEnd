# 🔍 DEBUGGING - PROBLEMA 403 CON PUT

## 🚨 **Problema Actual**
- `PUT http://localhost:8080/api/v1/mueble` → 403 Forbidden
- Usando token de ADMIN válido
- Otros métodos funcionan

## 🔧 **Pasos para Debugging**

### 1. **Verificar Token y Role**
```bash
# Hacer login y verificar el token
POST /auth/login
{
    "username": "esteban@gmail.com", 
    "password": "123456"
}

# Respuesta esperada:
{
    "token": "eyJ..."
}
```

### 2. **Decodificar Token JWT**
Ir a [jwt.io](https://jwt.io) y pegar el token para verificar:
- `sub`: debe ser el username
- `authorities`: debe contener "ADMIN"

### 3. **Verificar Logs**
Buscar en los logs de la aplicación:
- Errores de autenticación
- Mensajes de autorización
- Stack traces relacionados con Security

### 4. **Probar Endpoint Específico**
En lugar de la ruta base, probar:
```bash
PUT /api/v1/mueble/modificar/1
```

### 5. **Verificar Headers**
Asegurar que el header esté correcto:
```
Authorization: Bearer TU_TOKEN_AQUI
Content-Type: application/json
```

## 🔧 **Configuración Actual de Seguridad**
```java
// Ruta base PUT - ADMIN
.requestMatchers(new AntPathRequestMatcher("/api/v1/mueble", "PUT")).hasAuthority("ADMIN")
// Sub-rutas PUT - ADMIN  
.requestMatchers(new AntPathRequestMatcher("/api/v1/mueble/**", "PUT")).hasAuthority("ADMIN")
```

## ⚠️ **Posibles Causas**
1. **Token expirado o malformado**
2. **Role no es exactamente "ADMIN"**
3. **Header Authorization mal formateado**
4. **Endpoint PUT base no existe en el controlador**
5. **Orden de las reglas de seguridad**

## 🚀 **Siguiente Paso**
Verificar si existe un endpoint PUT en la ruta base `/api/v1/mueble` en el controlador.
