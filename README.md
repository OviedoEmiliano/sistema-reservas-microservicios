# Sistema de Reservas — Arquitectura de Microservicios

Proyecto backend de práctica orientado a portfolio, construido con Spring Boot y arquitectura de microservicios. El objetivo es implementar un sistema de gestión de usuarios y turnos/reservas, con un API Gateway como punto de entrada único.

## Estado del proyecto

🚧 **En desarrollo activo.**

| Servicio | Estado | Descripción |
|---|---|---|
| `usuario-service` | ✅ CRUD completo | Alta, baja, consulta (individual y paginada) y actualización (total/parcial) de usuarios |
| `turno-service` | 🚧 En desarrollo | Lógica core de turnos/reservas — próximo paso |
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
- Manejo global de excepciones con `@RestControllerAdvice`
- **Maven** como gestor de dependencias y build

## Estructura del repositorio

```
Reservas/
├── backend/
│   ├── usuario-service/
│   ├── turno-service/
│   └── api-gateway/
├── frontend/          (próximamente)
└── README.md
```

## Cómo correr el proyecto

### Requisitos previos

- Java 25
- Maven (o usar el wrapper `mvnw` incluido en cada servicio)
- PostgreSQL corriendo localmente
- Variable de entorno `DB_PASSWORD` configurada con la contraseña de tu usuario de PostgreSQL

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

## Endpoints disponibles (usuario-service)

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/usuarios` | Crear un usuario |
| `GET` | `/api/usuarios/{id}` | Obtener un usuario por id |
| `GET` | `/api/usuarios?page=&size=&sort=` | Listado paginado |
| `PUT` | `/api/usuarios/{id}` | Actualización completa |
| `PATCH` | `/api/usuarios/{id}` | Actualización parcial |
| `DELETE` | `/api/usuarios/{id}` | Eliminar un usuario |

## Próximos pasos

- [ ] Tests unitarios (JUnit + Mockito) y de integración (`@SpringBootTest` + `MockMvc`)
- [ ] Encriptación de contraseñas con BCrypt
- [ ] Desarrollo completo de `turno-service`
- [ ] Configuración del `api-gateway` con ruteo hacia ambos servicios
- [ ] Autenticación y autorización con JWT, validado en el Gateway y en cada servicio
- [ ] Frontend en React consumiendo la API

## Autor

Emiliano Oviedo 