# usuario-service

![usuario-service CI](https://github.com/OviedoEmiliano/sistema-reservas-microservicios/actions/workflows/usuario-service-ci.yml/badge.svg)

Microservicio de gestión de usuarios del sistema de Reservas. Implementa el CRUD completo sobre la entidad `Usuario`, con validaciones, mapeo DTO ↔ Entidad vía MapStruct, hashing de contraseñas y manejo global de excepciones.

## Estado

✅ **Funcional y cubierto por tests:** CRUD completo, hashing de contraseñas con BCrypt, documentación interactiva con Swagger UI, tests unitarios (Mockito), tests de capa web (`@WebMvcTest`), tests de integración con contenedor real (Testcontainers) y pipeline de CI en GitHub Actions.

---

## Stack específico de este servicio

* **Lenguaje y framework:** Java 25 / Spring Boot 4.1.1 (Spring Web MVC)
* **Persistencia:** Spring Data JPA + PostgreSQL (driver oficial)
* **Seguridad:** Spring Security Crypto (`BCryptPasswordEncoder`), sin activar Spring Security completo
* **Mapeo:** MapStruct 1.5.5 (generación en tiempo de compilación)
* **Boilerplate:** Lombok
* **Validación:** Jakarta Bean Validation (`spring-boot-starter-validation`)
* **Documentación:** Springdoc OpenAPI (Swagger UI)
* **Manejo de errores:** `@RestControllerAdvice` con respuestas `ProblemDetail` (RFC 9457, que reemplaza a la RFC 7807)
* **Testing:** JUnit 5, Mockito, AssertJ, MockMvc y Testcontainers (PostgreSQL 16 Alpine)
* **CI/CD:** GitHub Actions
* **Migraciones:** Flyway (SQL versionado), con Hibernate en modo \validate``

## Configuración

### Base de datos

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/reservas_usuarios_db
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}
```

Requiere la variable de entorno `DB_PASSWORD` seteada con la contraseña real de tu instancia de PostgreSQL. 
La base reservas_usuarios_db debe existir y estar vacía; las tablas las crea Flyway al arrancar. 
Hibernate solo valida el esquema (spring.jpa.hibernate.ddl-auto=validate).
### Puerto

El servicio corre por defecto en `8081`.

## Cómo correrlo

```bash
cd backend/usuario-service
./mvnw spring-boot:run
```

## Documentación interactiva (Swagger / OpenAPI)

Con el microservicio levantado, la especificación OpenAPI y la interfaz de pruebas están disponibles en:

* Swagger UI: http://localhost:8081/swagger-ui/index.html
* Especificación OpenAPI (JSON): http://localhost:8081/v3/api-docs

Permite visualizar esquemas y restricciones de validación, y ejecutar peticiones directamente contra el servidor local.

## Migraciones de base de datos (Flyway)

Los scripts están en `src/main/resources/db/migration` y se aplican en orden al arrancar la aplicación, antes de que Hibernate valide el esquema.

* Nombre: `V<versión>__<descripción>.sql` (dos guiones bajos).
* **Una migración ya aplicada no se edita:** Flyway guarda su checksum y la aplicación se niega a arrancar si cambia. Los cambios se hacen con una migración nueva.
* Cada cambio en una entidad exige su migración; si falta, `ddl-auto=validate` impide que la aplicación arranque.
* El constraint de email único se llama `uk_usuarios_email`, y el service lo usa para distinguir un email duplicado de otras violaciones de integridad.
* `ck_usuarios_rol` restringe el rol en la base: si se agrega un valor al enum `Rol`, hace falta una migración que actualice ese `CHECK`.

## Tests

```bash
cd backend/usuario-service
./mvnw test
```

* **Unitarios (`UsuarioServiceTest`):** prueban la lógica de negocio de forma aislada (normalización de email, hashing de contraseñas, detección de duplicados y llamadas al mapper) con Mockito. No requieren base de datos.
* **Capa web (`UsuarioControllerWebTest`):** pruebas de *slice* con `@WebMvcTest` y `MockMvc`, con el service mockeado. Validan códigos HTTP, validación de entrada, restricción de campos ordenables y serialización de errores `ProblemDetail`. No requieren base de datos ni Docker.
* **Integración con BD real (`UsuarioControllerIntegrationTest`):** pruebas con `@SpringBootTest` y **Testcontainers** (`PostgreSQLContainer("postgres:16-alpine")`), con un contenedor efímero conectado mediante `@ServiceConnection`, para garantizar fidelidad con la base de datos real.
* **CI:** pipeline en GitHub Actions (`.github/workflows/usuario-service-ci.yml`) que compila y ejecuta toda la suite en cada push y pull request.

> **Nota:** la suite de integración usa `@Testcontainers(disabledWithoutDocker = true)`. En entornos sin Docker esos tests se omiten (figuran como *skipped*) y solo corren los unitarios y los de capa web. Con Docker disponible, por ejemplo en GitHub Actions, se ejecutan automáticamente.

## Modelo de datos

### Entidad `Usuario`

| Campo                | Tipo            | Restricciones                                                                                                            |
|----------------------|-----------------|--------------------------------------------------------------------------------------------------------------------------|
| `id`                 | `Long`          | Clave primaria, autoincremental (IDENTITY)                                                                               |
| `nombre`             | `String`        | Not Null, longitud máxima 50                                                                                             |
| `apellido`           | `String`        | Not Null, longitud máxima 50                                                                                             |
| `email`              | `String`        | Not Null, único, normalizado a minúsculas, longitud máxima 100, constraint: uk_usuarios_email                            |
| `password`           | `String`        | Not Null, hasheado con BCrypt (la entrada admite entre 8 caracteres y 72 bytes)                                          |
| `nroCelular`         | `String`        | Not Null, solo dígitos (entre 8 y 15 caracteres), columna máx. 20                                                        |
| `rol`                | `Rol` (enum)    | Not Null, persistido como STRING (`ROLE_CLIENTE`, `ROLE_ADMIN`), por defecto `ROLE_CLIENTE`, constraint: ck_usuarios_rol |
| `fechaCreacion`      | `LocalDateTime` | `@CreationTimestamp`, inmutable (`updatable = false`)                                                                    |
| `fechaActualizacion` | `LocalDateTime` | `@UpdateTimestamp`                                                                                                       |

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
| `GET` | `/api/usuarios?page=&size=&sort=` | — | Listado paginado. Tamaño de página por defecto: 20, ordenado por `id`. Solo se puede ordenar por `id`, `nombre`, `apellido`, `email` y `fechaCreacion`; otro campo devuelve `400`. |
| `PUT` | `/api/usuarios/{id}` | `UsuarioUpdateDTO` | Actualización completa (nombre, apellido, celular). Todos los campos son obligatorios. |
| `PATCH` | `/api/usuarios/{id}` | `UsuarioPatchDTO` | Actualización parcial: solo se modifican los campos enviados. Los campos informados no pueden estar vacíos. |
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

### Ejemplo — error de validación

```json
{
  "type": "about:blank",
  "title": "Error de validación",
  "status": 400,
  "detail": "Uno o más campos no son válidos",
  "instance": "/api/usuarios",
  "errores": {
    "nombre": ["El nombre es obligatorio"],
    "password": ["La contraseña debe tener al menos 8 caracteres"]
  }
}
```

## Manejo de errores

Los errores se centralizan en `GlobalExceptionHandler` y usan el formato estándar `ProblemDetail` (`application/problem+json`):

| Situación | Código HTTP | Título |
|---|---|---|
| Validación de campos (`@Valid` fallido) | `400 Bad Request` | Error de validación (con los mensajes agrupados por campo) |
| Parámetro de orden fuera de la lista permitida | `400 Bad Request` | Parámetro de orden inválido |
| JSON malformado o id con formato inválido | `400 Bad Request` | Bad Request |
| Ruta inexistente | `404 Not Found` | Not Found |
| Usuario no encontrado por id | `404 Not Found` | Usuario no encontrado |
| Método HTTP no soportado | `405 Method Not Allowed` | Method Not Allowed |
| Content-Type no soportado | `415 Unsupported Media Type` | Unsupported Media Type |
| Email duplicado al registrar (incluye registros simultáneos) | `409 Conflict` | Email ya registrado |
| Excepción interna no controlada | `500 Internal Server Error` | Error interno (mensaje genérico; el detalle se registra solo en el log) |

El handler extiende `ResponseEntityExceptionHandler` para respetar el código correcto de los errores propios de Spring MVC, y usa excepciones de dominio propias (`EmailYaRegistradoException`, `UsuarioNoEncontradoException`, `CampoOrdenInvalidoException`).

## Decisiones de diseño destacadas

* **Contraseñas con BCrypt:** se inyecta la interfaz `PasswordEncoder` (implementada con `BCryptPasswordEncoder`), así el algoritmo se decide en un solo lugar. La contraseña se valida en longitud (BCrypt solo admite hasta 72 bytes, por lo que el límite se controla en bytes y no en caracteres) y se hashea antes de persistir, después de comprobar que el email no esté duplicado.
* **Email normalizado:** se aplica `trim` y minúsculas antes de consultar duplicados y guardar, para que `Juan@x.com` y `juan@x.com` no sean usuarios distintos.
* **Unicidad bajo concurrencia: ahora el service comprueba el nombre del constraint (uk_usuarios_email) y las demás violaciones de integridad se propagan como error 500 en vez de disfrazarse de 409
* **DTOs separados por operación** (`UsuarioRequestDTO`, `UsuarioResponseDTO`, `UsuarioUpdateDTO`, `UsuarioPatchDTO`): evita exponer campos sensibles y previene sobreescrituras accidentales de `email`, `password` o `rol`.
* **PUT vs. PATCH:** `PUT` exige todos los campos editables; `PATCH` permite actualización parcial ignorando nulos en el mapeo (`@BeanMapping(nullValuePropertyMappingStrategy = IGNORE)` en MapStruct) y rechaza valores vacíos o en blanco en los campos informados.
* **Orden restringido por lista blanca:** el parámetro `sort` solo acepta los campos de `CAMPOS_ORDENABLES`. Evita un `500` ante campos inexistentes y que se pueda ordenar por campos sensibles como `password`, cuyo orden filtraría información sobre el hash.
* **Excepciones de dominio + `@RestControllerAdvice`:** la capa de servicio expresa errores de negocio y el handler global los traduce a HTTP, sin `try/catch` en los controllers.
* **Documentación sin acoplamiento:** Springdoc OpenAPI con `@ParameterObject`, para que Swagger UI exponga `page`, `size` y `sort` como parámetros independientes.
* **Rol como enum persistido como `STRING`:** evita la corrupción de datos si se reordena el enum.

## Próximos pasos para este servicio

- [x] Encriptar `password` con BCrypt (`PasswordEncoder`)
- [x] Tests unitarios del `UsuarioService` (Mockito)
- [x] Integración continua con GitHub Actions
- [x] Tests de capa web (`@WebMvcTest`) y de integración con Testcontainers
- [x] Documentación interactiva con `springdoc-openapi` (Swagger UI)
- [x] Tests de casos borde de `PUT` y `PATCH` (celular inválido y usuario inexistente)
- [x] Migraciones de base de datos con Flyway (y constraint único con nombre explícito)
- [ ] Reporte de cobertura con JaCoCo
- [ ] Autenticación y emisión de JWT (junto con el API Gateway)