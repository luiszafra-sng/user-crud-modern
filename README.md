# user-crud-modern

> **Aviso al alumno.** Este proyecto contiene un bug intencional. Sigue el módulo del sitio; **NO abras `BUG.md`** si eres alumno — arruinará el ejercicio.

Aplicación de ejemplo de la formación *Claude Code + SDD (Speckit) para Java*. CRUD de usuarios sobre Spring Boot 4 + Java 21 + H2 en memoria. Se usa como caso de estudio para el módulo de depuración asistida por Claude.

Formación publicada en: https://luiszafra-sng.github.io/claude-code-sdd-java/

## Requisitos

- SDKMAN operativo (ver `docs/setup/`).
- Java 21 y Maven 3.9.x — se activan automáticamente a partir de `.sdkmanrc`.
- `curl` (recomendado) o `httpie` (opcional).

## Arranque

```bash
cd examples/user-crud-modern
sdk env install    # instala las versiones declaradas en .sdkmanrc
sdk env            # activa las versiones en la shell
./mvnw spring-boot:run
```

La aplicación levanta en `http://localhost:8080`.

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Consola H2: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:userdb`, user `sa`, password vacío)

Al arrancar, `UserSeeder` inserta dos usuarios de ejemplo (`seed@example.com` con id `1`, `second@example.com` con id `2`) para poder probar `GET`/`PUT`/`DELETE` sin depender del endpoint de creación.

Los errores de la API se serializan como `application/problem+json` siguiendo RFC 7807 (`ProblemDetail` de Spring 6/Boot 4), con dos propiedades extra: `code` (identificador estable, p. ej. `VALIDATION_ERROR`) y `fieldErrors` (sólo en validación).

## Endpoints — happy path

### Reproducir el bug (POST)

```bash
curl -i -X POST http://localhost:8080/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"Ana Torres","email":"ana.torres@example.com"}'
```

Resultado actual: `HTTP 500` con `NullPointerException` en el log. Ver la página del sitio *App de ejemplo → Reproducir el bug* para el paso a paso.

### Listar

```bash
curl -i http://localhost:8080/users
```

### Obtener por id

```bash
curl -i http://localhost:8080/users/1
# → HTTP 200 + UserResponse

curl -i http://localhost:8080/users/9999
# → HTTP 404 + ProblemDetail (RFC 7807) con propiedad extra code:"USER_NOT_FOUND"
```

### Actualizar

```bash
curl -i -X PUT http://localhost:8080/users/1 \
  -H 'Content-Type: application/json' \
  -d '{"name":"Seed Renombrado","email":"seed@example.com"}'
# → HTTP 200 + UserResponse actualizado
```

### Borrar

```bash
curl -i -X DELETE http://localhost:8080/users/1
# → HTTP 204
```

### Equivalentes `httpie`

```bash
http :8080/users
http :8080/users/1
http PUT :8080/users/1 name="Nuevo" email=seed@example.com
http DELETE :8080/users/1
```

### Validación (400)

```bash
curl -i -X POST http://localhost:8080/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"Ana","email":"no-es-un-email"}'
# → HTTP 400 + ProblemDetail (RFC 7807) con code:"VALIDATION_ERROR" y fieldErrors:[...]
```

### Conflicto de email en PUT (409)

Al intentar reasignar a un usuario un email ya en uso por otro:

```bash
curl -i -X PUT http://localhost:8080/users/2 \
  -H 'Content-Type: application/json' \
  -d '{"name":"Colisión","email":"seed@example.com"}'
# → HTTP 409 + ProblemDetail (RFC 7807) con code:"EMAIL_ALREADY_EXISTS"
```

## Tests

```bash
./mvnw test
```

Suite esperada (toda en verde):

- `UserMapperUnitTest.toEntity_currentlyOmitsEmail_bugKnown` — evidencia unitaria del bug.
- `UserControllerCreateBugIT.createUser_currentlyThrowsNPE_bugKnown` — evidencia integración del bug.
- `UserControllerHappyPathIT` — GET/PUT/DELETE en verde.
- `UserControllerValidationIT` — 400 en payloads inválidos.
- `UserControllerConflictIT` — 409 en PUT con email duplicado.

Los tests con sufijo `_bugKnown` documentan el bug intencional y se reescribirán a happy path cuando llegue el fix en el módulo de depuración.

## Referencia del formador

`BUG.md` (junto a este `README.md`) documenta la causa raíz, el mecanismo del NPE, el fix esperado y la guía de la sesión de depuración. **No leer si eres alumno.**
