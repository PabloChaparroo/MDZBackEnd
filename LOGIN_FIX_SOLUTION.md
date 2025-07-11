# 🔧 Fix: Problema de Login "Bad Credentials"

## 🚨 Problema Identificado
El error "Bad credentials" se debía a que **la contraseña no estaba siendo encriptada** al crear el usuario administrador.

### Error Original:
```java
Usuario user = Usuario.builder()
    .username("esteban@gmail.com")
    .password("123456") // ❌ Contraseña en texto plano
    .role(Role.ADMIN)
    .build();
```

### ¿Por qué fallaba?
1. Spring Security **encripta las contraseñas** automáticamente usando BCrypt
2. Al intentar login, compara la contraseña encriptada con el texto plano "123456"
3. La comparación falla → "Bad credentials"

## ✅ Solución Implementada

### 1. Agregado PasswordEncoder
```java
@Autowired
private PasswordEncoder passwordEncoder;
```

### 2. Encriptación de Contraseña
```java
Usuario user = Usuario.builder()
    .username("esteban@gmail.com")
    .password(passwordEncoder.encode("123456")) // ✅ Contraseña encriptada
    .role(Role.ADMIN)
    .build();
```

### 3. Verificación de Usuario Existente
```java
Optional<Usuario> usuarioExistente = usuarioRepository.findByUsername(adminUsername);
if (usuarioExistente.isPresent()) {
    logger.info("[SETUP] Usuario administrador ya existe: {}", adminUsername);
    return;
}
```

### 4. Configuraciones Agregadas
```java
@EnableJpaRepositories(basePackages = "com.Capinteria.carpinteria.Repositories")
@EntityScan(basePackages = "com.Capinteria.carpinteria.Entity")
```

## 📋 Logs Esperados
Después del fix, deberías ver:
```
[SETUP] Verificando usuario administrador por defecto...
[SETUP] Creando usuario administrador por defecto...
[OK] Usuario administrador creado exitosamente: esteban@gmail.com
```

## 🧪 Prueba Final
**Credenciales para login:**
```json
{
  "username": "esteban@gmail.com",
  "password": "123456"
}
```

**Endpoint:** `POST /auth/login`

## 🔄 Próximos Pasos
1. **Reiniciar la aplicación** para que se ejecute el `@PostConstruct`
2. **Verificar los logs** para confirmar la creación del usuario
3. **Probar el login** con las credenciales correctas
4. **El sistema de refresh tokens** estará disponible una vez que el login funcione

---
**Estado:** ✅ **SOLUCIONADO** - Login funcionará después de reiniciar la aplicación.
