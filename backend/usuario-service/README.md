# usuario-service

![usuario-service CI](https://github.com/OviedoEmiliano/sistema-reservas-microservicios/actions/workflows/usuario-service-ci.yml/badge.svg)

Microservicio de gestión de usuarios del sistema de Reservas. Implementa el CRUD completo sobre la entidad `Usuario`, con validaciones, mapeo DTO ↔ Entidad vía MapStruct, y manejo global de excepciones.

## Estado

✅ CRUD completo, con tests unitarios del service (JUnit 5 + Mockito) y CI con GitHub Actions.

⚠️ **Pendiente:** las contraseñas se persisten actualmente **en texto plano**. La encriptación con BCrypt (`PasswordEncoder`) está identificada y planificada, pero se decidió posponerla porque la seguridad del sistema completo se va a manejar a través de un API Gateway con JWT — el diseño final de autenticación se resolverá en conjunto con esa capa antes de cerrar este punto. No usar este servicio con datos reales hasta resolverlo.

## Stack específico de este servicio

- Spring Boot 4.1.1 (Java 25)
- Spring Data JPA + PostgreSQL
- MapStruct 1.5.5 — mapeo entre `Usuario` y sus DTOs
- Lombok
- Bean Validation (`jakarta.validation`)
- `@RestControllerAdvice` para manejo centralizado de excepciones
- Testing: JUnit 5, Mockito, AssertJ (Testcontainers previsto para integración)
- CI: GitHub Actions

## Configuración

### Base de datos

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/reservas_usuarios_db
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}
```

Requiere la variable de entorno `DB_PASSWORD` seteada con la contraseña real de tu instancia de PostgreSQL. La base `reservas_usuarios_db` debe existir; las tablas se crean/actualizan automáticamente (`spring.jpa.hibernate.ddl-auto=update`).

### Puerto

El servicio corre por defecto en `8081`.

## Cómo correrlo

```bash
cd backend/usuario-service
./mvnw spring-boot:run
```

## Tests

```bash
cd backend/usuario-service
./mvnw test
```

- **Unitarios** (`UsuarioServiceTest`): prueban la lógica de negocio de `UsuarioService` de forma aislada, con el repository y el mapper mockeados (Mockito). No requieren base de datos.
- **Integración**: previstos con Testcontainers (PostgreSQL real en contenedor) y MockMvc. Se ejecutarán en CI (GitHub Actions), que dispone de Docker.
- **CI**: cada push que modifica este servicio compila y ejecuta los tests en GitHub Actions (`.github/workflows/usuario-service-ci.yml`).

## Modelo de datos

### Entidad `Usuario`

| Campo | Tipo | Restricciones |
|---|---|---|
| `id` | `Long` | autogenerado |
| `nombre` | `String` | requerido, máx. 50 |
| `apellido` | `String` | requerido, máx. 50 |
| `email` | `String` | requerido, único, máx. 100 |
| `password` | `String` | requerido *(⚠️ texto plano por ahora)* |
| `nroCelular` | `String` | requerido, máx. 20 |
| `rol` | `Rol` (enum) | `ROLE_CLIENTE` por defecto |
| `fechaCreacion` | `LocalDateTime` | autogenerado, no actualizable |
| `fechaActualizacion` | `LocalDateTime` | autogenerado |

### Enum `Rol`

```java
ROLE_CLIENTE, ROLE_ADMIN
```

## Endpoints

Base path: `/api/usuarios`

| Método | Endpoint | Body | Descripción |
|---|---|---|---|
| `POST` | `/api/usuarios` | `UsuarioRequestDTO` | Crea un usuario. Devuelve `201` + header `Location`. Rechaza con `409` si el email ya existe. |
| `GET` | `/api/usuarios/{id}` | — | Obtiene un usuario por id. `404` si no existe. |
| `GET` | `/api/usuarios?page=&size=&sort=` | — | Listado paginado. Tamaño de página por defecto: 20, ordenado por `id`. |
| `PUT` | `/api/usuarios/{id}` | `UsuarioUpdateDTO` | Actualización completa (nombre, apellido, celular). Todos los campos son obligatorios. |
| `PATCH` | `/api/usuarios/{id}` | `UsuarioPatchDTO` | Actualización parcial — solo se modifican los campos enviados. |
| `DELETE` | `/api/usuarios/{id}` | — | Elimina un usuario. Devuelve `204`. `404` si no existe. |

### Ejemplo — crear usuario

```http
POST /api/usuarios
Content-Type: application/json

{
  "nombre": "Juan",
  "apellido": "Perez",
  "email": "juan.perez@test.com",
  "password": "password123",
  "nroCelular": "1122334455"
}
```

Respuesta `201 Created`:

```json
{
  "id": 1,
  "nombre": "Juan",
  "apellido": "Perez",
  "email": "juan.perez@test.com",
  "nroCelular": "1122334455",
  "rol": "ROLE_CLIENTE",
  "fechaCreacion": "2026-09-27T10:15:30"
}
```

El campo `password` nunca se expone en las respuestas.

## Manejo de errores

| Situación | Código HTTP |
|---|---|
| Email duplicado al crear | `409 Conflict` |
| Usuario no encontrado (`GET`/`PUT`/`PATCH`/`DELETE`) | `404 Not Found` |
| Validación de campos fallida (`@Valid`) | `400 Bad Request` |

Manejado centralizadamente en `GlobalExceptionHandler` vía `@RestControllerAdvice`, con excepciones de dominio propias (`EmailYaRegistradoException`, `UsuarioNoEncontradoException`).

## Decisiones de diseño

- **DTOs separados por operación** (`UsuarioRequestDTO`, `UsuarioResponseDTO`, `UsuarioUpdateDTO`, `UsuarioPatchDTO`): evita exponer campos sensibles (password) y permite reglas de validación distintas según el contexto (por ejemplo, el email no es editable vía `PUT`/`PATCH`).
- **PUT vs. PATCH diferenciados semánticamente**: `PUT` exige todos los campos editables; `PATCH` acepta actualización parcial, ignorando campos nulos en el mapeo (`@BeanMapping(nullValuePropertyMappingStrategy = IGNORE)` en MapStruct).
- **MapStruct sobre mapeo manual**: generación de mappers en tiempo de compilación, sin costo de reflection en runtime.
- **Excepciones de dominio + `@RestControllerAdvice`**: la capa de servicio expresa errores de negocio y el handler global los traduce a códigos HTTP, sin `try/catch` en los controllers.
- **Rol como enum persistido como `STRING`**: evita la corrupción de datos si se reordena el enum.

## Próximos pasos para este servicio

- [x] Encriptar `password` con BCrypt (`PasswordEncoder`), en conjunto con el diseño de autenticación vía API Gateway + JWT
- [x] Tests unitarios del `UsuarioService` (Mockito)
- [x] Integración continua con GitHub Actions
- [ ] Tests de integración del controller (`@SpringBootTest` + MockMvc + Testcontainers)
- [ ] Documentación interactiva con springdoc-openapi (Swagger)