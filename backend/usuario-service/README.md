# usuario-service

![usuario-service CI](https://github.com/OviedoEmiliano/sistema-reservas-microservicios/actions/workflows/usuario-service-ci.yml/badge.svg)

Microservicio de gestión de usuarios del sistema de Reservas. Implementa el CRUD completo sobre la entidad `Usuario`, con validaciones, mapeo DTO ↔ Entidad vía MapStruct, y manejo global de excepciones.

## Estado

🚧 **En fase final de cierre:** CRUD funcional, hashing de contraseñas con BCrypt, documentación interactiva con Swagger UI, tests unitarios (Mockito), tests de capa web (`@WebMvcTest`), tests de integración con contenedor real (`Testcontainers`) y pipeline de CI activo en GitHub Actions. Pendiente incorporar los últimos tests de casos borde para `PUT`/`PATCH` (Paso C) y la actualización documental final (Paso D).

---

## Stack específico de este servicio

* **Lenguaje y Framework:** Java 25 / Spring Boot 4.1.1 (Spring Web MVC)
* **Persistencia:** Spring Data JPA + PostgreSQL (driver oficial)
* **Seguridad:** Spring Security Crypto (`BCryptPasswordEncoder`)
* **Mapeo:** MapStruct 1.5.5 (generación en tiempo de compilación)
* **Boilerplate:** Lombok
* **Validación:** Jakarta Bean Validation (`spring-boot-starter-validation`)
* **Documentación:** Springdoc OpenAPI 2.8.5 (Swagger UI)
* **Manejo de Errores:** `@RestControllerAdvice` con respuestas estándar `ProblemDetail` (RFC 7807)
* **Testing:** JUnit 5, Mockito, AssertJ, MockMvc y Testcontainers (PostgreSQL 16 Alpine)
* **CI/CD:** GitHub Actions

## Configuración

### Base de datos

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/reservas_usuarios_db
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}
```

Requiere la variable de entorno `DB_PASSWORD` seteada con la contraseña real de tu instancia de PostgreSQL. La base `reservas_usuarios_db` debe existir; 
las tablas se crean/actualizan automáticamente (`spring.jpa.hibernate.ddl-auto=update`).

### Puerto

El servicio corre por defecto en `8081`.

## Cómo correrlo

```bash
cd backend/usuario-service
./mvnw spring-boot:run
```


## Documentación interactiva (Swagger / OpenAPI)
Con el microservicio levantado, la especificación OpenAPI y la interfaz de pruebas están disponibles en:

Swagger UI: http://localhost:8081/swagger-ui/index.html

Especificación OpenAPI (JSON): http://localhost:8081/v3/api-docs

## Tests

```bash
cd backend/usuario-service
./mvnw test
```

Permite visualizar esquemas, restricciones de validación y ejecutar peticiones directamente contra el servidor local.

* **Unitarios (`UsuarioServiceTest`):** Prueban la lógica de negocio de forma aislada (normalización de email, hashing de contraseñas, validación de duplicados y llamadas al mapper) utilizando Mockito. No requieren base de datos.
* **Capa Web (`UsuarioControllerWebTest`):** Pruebas de integración web slice con `@WebMvcTest` y `MockMvc`. Validan filtros de ordenamiento, códigos HTTP y serialización de errores RFC 7807 (`ProblemDetail`).
* **Integración con BD Real (`UsuarioControllerIntegrationTest`):** Pruebas *end-to-end* con `@SpringBootTest` y **Testcontainers** (`PostgreSQLContainer("postgres:16-alpine")`), levantando un contenedor Docker efímero con `@ServiceConnection` para garantizar fidelidad con la base de datos real.
* **CI:** Pipeline automatizado en GitHub Actions (`.github/workflows/usuario-service-ci.yml`) que compila y ejecuta toda la suite en cada push y pull request.

*(Nota: La suite de integración con Testcontainers está configurada con `@Testcontainers(disabledWithoutDocker = true)`, lo que permite ejecutar el proyecto de forma segura en entornos locales que no dispongan de Docker. En esos casos, la capa de integración se omite y solo se ejecuta el entorno web slice y unitario. Testcontainers se activa automáticamente al integrarse en GitHub Actions).*

## Modelo de datos

### Entidad `Usuario`

| Campo                | Tipo            | Restricciones                                                                          |
|----------------------|-----------------|----------------------------------------------------------------------------------------|
| `id`                 | `Long`          | Clave primaria, autoincremental (IDENTITY)                                             |
| `nombre`             | `String`        | Not Null, longitud máxima 50                                                           |
| `apellido`           | `String`        | Not Null, longitud máxima 50                                                           |
| `email`              | `String`        | Not Null, único, normalizado a minúsculas, longitud máxima 100                         |
| `password`           | `String`        | Not Null, encriptado con BCrypt (límite de entrada: 72 bytes)                          |
| `nroCelular`         | `String`        | Not Null, solo dígitos (entre 8 y 15 caracteres), col. máx. 20                         |
| `rol`                | `Rol` (enum)    | Not Null, persistido como STRING (ROLE_CLIENTE, ROLE_ADMIN) `ROLE_CLIENTE` por defecto |
| `fechaCreacion`      | `LocalDateTime` | @CreationTimestamp, inmutable (updatable = false)                                      |
| `fechaActualizacion` | `LocalDateTime` | @UpdateTimestamp                                                                       |

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

## Manejo de errores (RFC 7807)

Los errores se centralizan en `GlobalExceptionHandler` utilizando el formato estándar `ProblemDetail`:

| Situación | Código HTTP | Título del Problema |
|---|---|---|
| Validación de campos (`@Valid` fallido) | `400 Bad Request` | Error de validación (detalle con mapa de errores por campo) |
| Parámetro de orden fuera de la lista blanca | `400 Bad Request` | Parámetro de orden inválido |
| Formato JSON o ID malformado | `400 Bad Request` | Bad Request |
| Usuario no encontrado por ID | `404 Not Found` | Usuario no encontrado |
| Email duplicado al registrar | `409 Conflict` | Email ya registrado |
| Excepción interna no controlada | `500 Internal Server Error` | Error interno (sanitizado sin filtrar datos de la BD) |
Manejado centralizadamente en `GlobalExceptionHandler` vía `@RestControllerAdvice`, con excepciones de dominio propias (`EmailYaRegistradoException`, `UsuarioNoEncontradoException`).

## Decisiones de diseño destacadas

* **Seguridad en contraseñas:** Inyección de `PasswordEncoder` configurado con `BCryptPasswordEncoder`. La contraseña se valida en longitud (máximo 72 bytes por límite algorítmico de BCrypt) y se hashea antes de persistir.
* **Separación estricta de DTOs:** Se utiliza un DTO específico por cada caso de uso (`UsuarioRequestDTO`, `UsuarioResponseDTO`, `UsuarioUpdateDTO`, `UsuarioPatchDTO`), previniendo sobreescrituras accidentales de campos sensibles como `email`, `password` o `rol`.
* **Semántica PUT vs. PATCH:** `PUT` exige el reemplazo íntegro de campos de contacto; `PATCH` permite actualización selectiva ignorando nulos en tiempo de compilación con MapStruct (`@BeanMapping(nullValuePropertyMappingStrategy = IGNORE)`).
* **Protección contra inyecciones en ordenamiento:** Se aplica una lista blanca de atributos permitidos (`CAMPOS_ORDENABLES`) en `listarUsuarios`, impidiendo ordenar o filtrar por campos internos o sensibles.
* **Documentación sin acoplamiento:** Integración limpia de Springdoc OpenAPI usando `@ParameterObject` para que Swagger UI exponga `page`, `size` y `sort` como parámetros desacoplados sin envoltorios JSON complejos.

## Próximos pasos para este servicio

- [x] Encriptar `password` con BCrypt (`PasswordEncoder`)
- [x] Tests unitarios del `UsuarioService` (Mockito)
- [x] Integración continua con GitHub Actions
- [x] Tests de integración con Testcontainers (vía CI) y `@SpringBootTest`
- [x] Documentación interactiva con `springdoc-openapi` (Swagger UI)
- [ ] Tests faltantes (Paso C):
    - [ ] `PUT` y `PATCH` con celular inválido (`400 Bad Request`)
    - [ ] `PUT` con ID inexistente (`404 Not Found`)
- [ ] Actualización final (Paso D):
    - [ ] Sincronizar estado en el `README.md` principal de la raíz del repositorio y documento de repaso