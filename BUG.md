# BUG.md — Referencia interna para el formador

> **AVISO — SÓLO PARA EL FORMADOR.** Si eres alumno, deja de leer y sigue el módulo del sitio. Este documento arruina el ejercicio si lo lees antes de intentar resolverlo con Claude.

## Síntoma

`POST /users` con un payload sintácticamente válido (por ejemplo `{"name":"Ana Torres","email":"ana.torres@example.com"}`) responde `HTTP 500` con un `NullPointerException` en el log.

Comando de repro:

```bash
curl -i -X POST http://localhost:8080/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"Ana Torres","email":"ana.torres@example.com"}'
```

## Ubicación de la causa raíz

Fichero: `src/main/java/com/sngular/formacion/usercrud/user/UserMapper.java`
Método: `toEntity(CreateUserRequest request)`

Código actual (buggy):

```java
public User toEntity(CreateUserRequest request) {
    User user = new User();
    user.setName(request.name());
    return user;    // Falta la asignación de email.
}
```

## Punto donde estalla

Fichero: `src/main/java/com/sngular/formacion/usercrud/user/UserService.java`
Método: `private void normalizeEmail(User user)`
Línea: `user.setEmail(user.getEmail().toLowerCase(Locale.ROOT));`

Aquí `user.getEmail()` es `null` (nunca lo asignó el mapper) y `.toLowerCase(...)` sobre `null` lanza el `NullPointerException`. La traza señala al service; la causa está en el mapper.

## Mecanismo

El controller pasa la validación Jakarta (el DTO tiene `@NotBlank`, `@Email`, `@Size`), por lo que el payload correcto llega al service. El service pide al mapper una `User` a partir del DTO. El mapper omite `entity.setEmail(request.email())` — probablemente por un despiste al escribirlo a mano. La normalización posterior en el service invoca un método sobre el `email` asumiendo no-null y produce el NPE. Efecto sorpresa: el error parece del service pero es del mapper.

## Fix esperado

Añadir la línea que falta en `UserMapper.toEntity(CreateUserRequest)`:

```java
public User toEntity(CreateUserRequest request) {
    User user = new User();
    user.setName(request.name());
    user.setEmail(request.email());   // ← línea añadida
    return user;
}
```

## Comandos de validación del fix

Antes del fix (estado actual de esta feature):

```bash
./mvnw test -Dtest=UserMapperUnitTest
# → verde. El test afirma que entity.getEmail() es null (documenta el bug).
```

Después del fix (fuera de alcance de esta feature; se hará en el módulo de debugging):

1. Reescribir `UserMapperUnitTest.toEntity_currentlyOmitsEmail_bugKnown` para afirmar `assertThat(entity.getEmail()).isEqualTo("ana.torres@example.com")` (renombrar a `toEntity_assignsAllFields`).
2. Reescribir `UserControllerCreateBugIT.createUser_currentlyThrowsNPE_bugKnown` para hacer `POST /users` y `.andExpect(status().isCreated())` (renombrar a `createUser_returns201WithLocationHeader`).
3. `./mvnw verify` debe quedar verde.

## Guía de la sesión de depuración con Claude

Preguntas guía que el formador puede lanzar durante el ejercicio:

1. "¿Qué endpoint falla? ¿Con qué código HTTP? ¿Con qué payload?"
2. "Enseña a Claude el log completo del NPE. ¿Qué línea señala?"
3. "Lee el método que señala la traza. ¿Es esa la causa o sólo el síntoma?"
4. "Recorre el flujo hacia atrás desde ahí. ¿Qué asume esa línea sobre el estado del objeto?"
5. "Compara la construcción del `User` en el mapper con los campos del DTO. ¿Falta algo?"
6. "Propón un fix mínimo. ¿Qué test lo verificaría?"

Cuándo revelar la causa: sólo si el alumno se estanca >20 min sin llegar al mapper. Si Claude propone `Optional` o comprobaciones defensivas antes de leer el mapper, redirigir hacia lectura del código, no hacia parches defensivos.
