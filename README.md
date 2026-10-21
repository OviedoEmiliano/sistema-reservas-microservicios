# Sistema de Reservas — Arquitectura de Microservicios

![usuario-service CI](https://github.com/OviedoEmiliano/sistema-reservas-microservicios/actions/workflows/usuario-service-ci.yml/badge.svg)

Proyecto backend de práctica orientado a portfolio, construido con Spring Boot y arquitectura de microservicios. El objetivo es implementar un sistema de gestión de usuarios y turnos/reservas, con un API Gateway como punto de entrada único.

## Estado del proyecto

🚧 **En desarrollo activo.**

| Servicio | Estado | Descripción |
|---|---|---|
| [`usuario-service`](backend/usuario-service/README.md) | ✅ Completo | CRUD de usuarios con contraseñas hasheadas (BCrypt), documentación Swagger, tests unitarios, de capa web y de integración, y CI |
| `turno-service` | 🚧 Scaffolding inicial | Lógica core de turnos/reservas — próximo paso |
| `api-gateway` | 🚧 Scaffolding inicial | Punto de entrada único, ruteo hacia los microservicios |
| `frontend` (React) | ⏳ Pendiente | Se desarrollará una vez completo el backend |

## Arquitectura

```
Cliente → API Gateway → usuario-service (puerto 8081)
                       → turno-service  (puerto 8082)
```

Cada microservicio tiene su propia base de datos PostgreSQL, siguiendo el patrón *database per service*.

## Stack técnico

- **Java 25** / **Spring Boot 4.1.1**
- **Spring Data JPA** + **PostgreSQL**
- **MapStruct** — mapeo entre entidades y DTOs
- **Lombok** — reducción de boilerplate
- **Bean Validation** (`spring-boot-starter-validation`)
- **Spring Security Crypto** — hashing de contraseñas con BCrypt (sin activar Spring Security completo)
- **Springdoc OpenAPI** — documentación interactiva con Swagger UI
- Manejo global de excepciones con `@RestControllerAdvice` y respuestas `ProblemDetail` (RFC 9457)
- **Testing:** JUnit 5, Mockito, AssertJ, MockMvc (`@WebMvcTest`) y Testcontainers
- **CI:** GitHub Actions
- **Maven** como gestor de dependencias y build

## Estructura del repositorio

```
Reservas/
├── .github/workflows/     # CI con GitHub Actions
├── backend/
│   ├── usuario-service/
│   ├── turno-service/
│   └── api-gateway/
├── frontend/              (próximamente)
└── README.md
```

## Cómo correr el proyecto

### Requisitos previos

- Java 25
- Maven (o usar el wrapper `mvnw` incluido en cada servicio)
- PostgreSQL corriendo localmente
- Variable de entorno `DB_PASSWORD` configurada con la contraseña de tu usuario de PostgreSQL
- Docker (opcional): solo para correr localmente los tests de integración con Testcontainers

### Bases de datos necesarias

Cada servicio espera su propia base ya creada en PostgreSQL:

| Servicio | Base de datos |
|---|---|
| `usuario-service` | `reservas_usuarios_db` |
| `turno-service` | `reservas_turnos_db` |

Hibernate se encarga de crear/actualizar las tablas automáticamente (`ddl-auto=update`).

### Levantar un servicio

```bash
cd backend/usuario-service
./mvnw spring-boot:run
```

`usuario-service` corre en el puerto `8081`.

### Correr los tests

```bash
cd backend/usuario-service
./mvnw test
```

Los tests unitarios y de capa web no requieren base de datos. Los de integración usan Testcontainers y necesitan Docker: si no está disponible, se omiten localmente y se ejecutan en CI (GitHub Actions).

## Documentación de la API

Con `usuario-service` levantado, Swagger UI está disponible en:

- Swagger UI: http://localhost:8081/swagger-ui/index.html
- Especificación OpenAPI (JSON): http://localhost:8081/v3/api-docs

## Endpoints disponibles (usuario-service)

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/usuarios` | Crear un usuario |
| `GET` | `/api/usuarios/{id}` | Obtener un usuario por id |
| `GET` | `/api/usuarios?page=&size=&sort=` | Listado paginado (orden restringido a una lista de campos permitidos) |
| `PUT` | `/api/usuarios/{id}` | Actualización completa |
| `PATCH` | `/api/usuarios/{id}` | Actualización parcial |
| `DELETE` | `/api/usuarios/{id}` | Eliminar un usuario |

Detalle completo (DTOs, validaciones, errores y decisiones de diseño) en el [README de `usuario-service`](backend/usuario-service/README.md).

## Próximos pasos

- [x] Tests unitarios de `usuario-service` (JUnit + Mockito)
- [x] Integración continua con GitHub Actions
- [x] Tests de integración (`@SpringBootTest` + `MockMvc` + Testcontainers)
- [x] Tests de capa web (`@WebMvcTest`)
- [x] Encriptación de contraseñas con BCrypt
- [x] Documentación interactiva con Swagger UI
- [ ] Desarrollo completo de `turno-service`
- [ ] Configuración del `api-gateway` con ruteo hacia ambos servicios
- [ ] Autenticación y autorización con JWT, validado en el Gateway y en cada servicio
- [ ] Migraciones de base de datos con Flyway
- [ ] Reporte de cobertura con JaCoCo
- [ ] Frontend en React consumiendo la API

## Autor

Emiliano Oviedo