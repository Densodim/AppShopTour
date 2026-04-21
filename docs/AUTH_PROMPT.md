# Задача: Аутентификация и авторизация в Ktor сервер (TDD + i18n + DRY)

## Контекст проекта

Ktor сервер (Kotlin) с такой структурой:
- `Application.kt` — точка входа, `/api/v1`
- `api/UsersRoute.kt` — GET /users, GET /users/{id}
- `database/table/UsersTable.kt` — id (UUID), name, email, passwordHash,
  preferredCurrency, preferredLanguage (default "ru"), themeMode, createdAt, updatedAt
- `shared/src/commonMain/` — общий KMP модуль (Android + iOS + Desktop)
- `shared/src/commonMain/data/remote/HttpClientFactory.kt` — Ktor клиент

Зависимости уже есть: Ktor, Exposed ORM, HikariCP, Flyway, PostgreSQL/H2,
kotlinx.serialization, `ktor-server-test-host`, `kotlin-test-junit`.

Добавить в `server/build.gradle.kts`:
```kotlin
implementation("dev.whyoleg.cryptography:cryptography-core:0.6.0")
implementation("dev.whyoleg.cryptography:cryptography-provider-jdk:0.6.0")
implementation("io.ktor:ktor-server-auth:$ktor_version")
implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
implementation("io.ktor:ktor-server-status-pages:$ktor_version")
implementation("io.ktor:ktor-server-rate-limit:$ktor_version")
```

Добавить в `shared/build.gradle.kts` (commonMain):
```kotlin
implementation("io.ktor:ktor-client-auth:$ktor_version")
```

---

## Правила разработки (применять ко всему ниже)

### TDD — Red → Green → Refactor
Для каждого компонента:
1. **Red** — напиши тест, убедись что он падает
2. **Green** — минимальный код чтобы тест прошёл
3. **Refactor** — улучши без изменения поведения

Тесты идут в `server/src/test/`. Каждый файл тестов создаётся **до** реализации.

**Объясни один раз:**
- Что такое TDD и почему тест сначала меняет дизайн кода?
- Почему тест, который сразу проходит — бесполезен?
- Что означает Red-Green-Refactor на практике?

### DRY — не повторяй
- Общий setup тестов — в `AuthTestBase`
- Общий код роутов — в extension-функции на `ApplicationCall`
- Ошибки — через `sealed class AuthError`, не строки
- Локализация — через один вызов `call.lang()`
- Обработка ошибок — через `StatusPages`, не `try-catch` в каждом роуте

---

## Шаг 1: Иерархия ошибок — `AuthError`

Создай `auth/AuthError.kt`:

```kotlin
sealed class AuthError : Exception() {
    data object InvalidCredentials : AuthError()
    data object EmailAlreadyExists : AuthError()
    data object TokenInvalid : AuthError()
    data object TokenExpired : AuthError()
    data class ValidationFailed(val fields: List<String>) : AuthError()
}
```

Маппинг ошибки → ключ локализации:
```kotlin
fun AuthError.toMessageKey(): I18n.MessageKey = when (this) {
    is AuthError.InvalidCredentials -> I18n.MessageKey.INVALID_CREDENTIALS
    is AuthError.EmailAlreadyExists -> I18n.MessageKey.EMAIL_ALREADY_EXISTS
    is AuthError.TokenInvalid       -> I18n.MessageKey.TOKEN_INVALID
    is AuthError.TokenExpired       -> I18n.MessageKey.TOKEN_EXPIRED
    is AuthError.ValidationFailed   -> I18n.MessageKey.VALIDATION_FAILED
}
```

Маппинг ошибки → HTTP статус (единое место, используется в `StatusPages`):
```kotlin
fun AuthError.toHttpStatus(): HttpStatusCode = when (this) {
    is AuthError.InvalidCredentials -> HttpStatusCode.Unauthorized
    is AuthError.EmailAlreadyExists -> HttpStatusCode.Conflict
    is AuthError.ValidationFailed   -> HttpStatusCode.BadRequest
    is AuthError.TokenInvalid,
    is AuthError.TokenExpired       -> HttpStatusCode.Unauthorized
}
```

**Объясни:**
- Почему `sealed class` вместо строковых кодов ошибок?
- Почему `ValidationFailed` содержит `List<String>`, а не одну строку?
- Что такое Railway-Oriented Programming и как `Result<T>` связан с `AuthError`?

---

## Шаг 2: Локализация — `I18n`

Создай `auth/I18n.kt`. Три языка: `en` (глобальный стандарт), `ru` (дефолт проекта), `es` (второй по числу носителей).

```kotlin
object I18n {
    enum class Lang { EN, RU, ES }

    enum class MessageKey {
        INVALID_CREDENTIALS, EMAIL_ALREADY_EXISTS,
        TOKEN_INVALID, TOKEN_EXPIRED,
        VALIDATION_FAILED, LOGOUT_SUCCESS, INTERNAL_ERROR,
        RATE_LIMIT_EXCEEDED
    }

    fun resolve(acceptLanguage: String?, jwtLang: String? = null): Lang {
        val raw = jwtLang ?: acceptLanguage?.split(",")?.firstOrNull()?.take(2)
        return when (raw?.lowercase()) {
            "ru" -> Lang.RU
            "es" -> Lang.ES
            else -> Lang.EN
        }
    }

    fun message(key: MessageKey, lang: Lang): String =
        messages[key]?.get(lang) ?: messages[key]?.get(Lang.EN) ?: key.name

    private val messages: Map<MessageKey, Map<Lang, String>> = mapOf(
        MessageKey.INVALID_CREDENTIALS to mapOf(
            Lang.EN to "Invalid email or password",
            Lang.RU to "Неверный email или пароль",
            Lang.ES to "Correo o contraseña incorrectos"
        ),
        MessageKey.EMAIL_ALREADY_EXISTS to mapOf(
            Lang.EN to "Email is already registered",
            Lang.RU to "Email уже зарегистрирован",
            Lang.ES to "El correo ya está registrado"
        ),
        MessageKey.TOKEN_INVALID to mapOf(
            Lang.EN to "Token is invalid",
            Lang.RU to "Токен недействителен",
            Lang.ES to "El token no es válido"
        ),
        MessageKey.TOKEN_EXPIRED to mapOf(
            Lang.EN to "Token has expired",
            Lang.RU to "Срок действия токена истёк",
            Lang.ES to "El token ha expirado"
        ),
        MessageKey.VALIDATION_FAILED to mapOf(
            Lang.EN to "Validation error",
            Lang.RU to "Ошибка валидации",
            Lang.ES to "Error de validación"
        ),
        MessageKey.LOGOUT_SUCCESS to mapOf(
            Lang.EN to "Logged out successfully",
            Lang.RU to "Выход выполнен успешно",
            Lang.ES to "Sesión cerrada correctamente"
        ),
        MessageKey.INTERNAL_ERROR to mapOf(
            Lang.EN to "Internal server error",
            Lang.RU to "Внутренняя ошибка сервера",
            Lang.ES to "Error interno del servidor"
        ),
        MessageKey.RATE_LIMIT_EXCEEDED to mapOf(
            Lang.EN to "Too many requests, please try again later",
            Lang.RU to "Слишком много запросов, попробуйте позже",
            Lang.ES to "Demasiadas solicitudes, intente más tarde"
        ),
    )
}
```

**Объясни:**
- Что такое i18n (internationalization) vs l10n (localization)?
- Почему `Accept-Language` — стандарт HTTP (RFC 7231)?
- Почему fallback на `en`, а не на `ru`?
- Почему `jwtLang` имеет приоритет над `Accept-Language`?

### Тесты I18n — пишем ПЕРВЫМИ (`I18nTest.kt`)

```kotlin
class I18nTest {
    @Test fun `resolve RU for Accept-Language ru`() =
        assertEquals(Lang.RU, I18n.resolve("ru-RU,ru;q=0.9"))

    @Test fun `resolve ES for Accept-Language es`() =
        assertEquals(Lang.ES, I18n.resolve("es-ES,es;q=0.9"))

    @Test fun `fallback EN for unknown language`() =
        assertEquals(Lang.EN, I18n.resolve("fr-FR"))

    @Test fun `fallback EN for null`() =
        assertEquals(Lang.EN, I18n.resolve(null))

    @Test fun `jwtLang takes priority over Accept-Language`() =
        assertEquals(Lang.RU, I18n.resolve(acceptLanguage = "en", jwtLang = "ru"))

    @Test fun `all MessageKeys have non-blank translations for all languages`() {
        MessageKey.entries.forEach { key ->
            Lang.entries.forEach { lang ->
                assertTrue(I18n.message(key, lang).isNotBlank(), "Missing: $key/$lang")
            }
        }
    }
}
```

---

## Шаг 3: Extension-функции на `ApplicationCall` — единая точка ответа

Создай `api/CallExtensions.kt`:

```kotlin
fun ApplicationCall.lang(): I18n.Lang {
    val jwtLang = principal<JWTPrincipal>()?.payload?.getClaim("lang")?.asString()
    return I18n.resolve(
        acceptLanguage = request.headers[HttpHeaders.AcceptLanguage],
        jwtLang = jwtLang
    )
}

suspend fun ApplicationCall.respondError(error: AuthError) {
    val lang = lang()
    val message = I18n.message(error.toMessageKey(), lang)
    val body = if (error is AuthError.ValidationFailed) {
        mapOf("error" to message, "details" to error.fields)
    } else {
        mapOf("error" to message)
    }
    respond(error.toHttpStatus(), body)
}
```

**Объясни:**
- Что такое extension-функции в Kotlin и почему они помогают с DRY?
- Почему `respondError` принимает `AuthError`, а не строку?

---

## Шаг 4: `StatusPages` — глобальная обработка ошибок

Добавь в `Application.kt` **до** `routing {}`:

```kotlin
install(StatusPages) {
    exception<AuthError> { call, error ->
        call.respondError(error)
    }

    status(HttpStatusCode.TooManyRequests) { call, _ ->
        val lang = call.lang()
        call.respond(
            HttpStatusCode.TooManyRequests,
            mapOf("error" to I18n.message(I18n.MessageKey.RATE_LIMIT_EXCEEDED, lang))
        )
    }

    exception<Throwable> { call, _ ->
        val lang = call.lang()
        call.respond(
            HttpStatusCode.InternalServerError,
            mapOf("error" to I18n.message(I18n.MessageKey.INTERNAL_ERROR, lang))
        )
    }
}
```

Теперь роуты просто `throw AuthError.InvalidCredentials` — никаких `try-catch`.

**Объясни:**
- Что такое централизованная обработка ошибок и почему это важно?
- Почему мы не возвращаем стек-трейс клиенту в production?
- Что такое принцип Fail Fast?

### Тесты StatusPages — пишем ПЕРВЫМИ (`StatusPagesTest.kt`)

```kotlin
class StatusPagesTest : AuthTestBase() {

    @Test fun `AuthError maps to correct HTTP status`() = authTest {
        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"nobody@test.com","password":"wrong"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test fun `error response body contains error key`() = authTest {
        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"nobody@test.com","password":"wrong"}""")
        }
        assertTrue(response.bodyAsText().contains("\"error\""))
    }

    @Test fun `error message localized via Accept-Language`() = authTest {
        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.AcceptLanguage, "ru")
            setBody("""{"email":"nobody@test.com","password":"wrong"}""")
        }
        assertTrue(response.bodyAsText().contains("Неверный email или пароль"))
    }

    @Test fun `internal error does not leak stack trace`() = authTest {
        val response = client.get("/api/v1/test-error")
        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertFalse(response.bodyAsText().contains("Exception"))
        assertFalse(response.bodyAsText().contains("at com.example"))
    }
}
```

---

## Шаг 5: `AuthService` интерфейс — отделяем роуты от БД

Создай `auth/AuthService.kt`:

```kotlin
interface AuthService {
    suspend fun register(name: String, email: String, password: String): AuthResponse
    suspend fun login(email: String, password: String): AuthResponse
    suspend fun refresh(refreshToken: String): AuthResponse
    suspend fun logout(userId: String)
}

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse? = null
)
```

Создай `auth/AuthServiceImpl.kt` — реализация с Exposed + PasswordHasher + JwtService.
Роут получает `AuthService` через параметр:

```kotlin
fun Route.authRoutes(authService: AuthService) {
    post("/register") {
        val req = call.receive<RegisterRequest>()
        val errors = req.validate()
        if (errors.isNotEmpty()) throw AuthError.ValidationFailed(errors)
        call.respond(HttpStatusCode.Created, authService.register(req.name, req.email, req.password))
    }
}
```

Создай `auth/FakeAuthService.kt` для тестов (без БД):

```kotlin
class FakeAuthService : AuthService {
    var shouldFailLogin = false
    var registeredEmails = mutableSetOf<String>()

    override suspend fun register(name: String, email: String, password: String): AuthResponse {
        if (email in registeredEmails) throw AuthError.EmailAlreadyExists
        registeredEmails += email
        return AuthResponse(accessToken = "fake-access", refreshToken = "fake-refresh")
    }

    override suspend fun login(email: String, password: String): AuthResponse {
        if (shouldFailLogin) throw AuthError.InvalidCredentials
        return AuthResponse(accessToken = "fake-access", refreshToken = "fake-refresh")
    }

    override suspend fun refresh(refreshToken: String): AuthResponse {
        if (refreshToken == "invalid") throw AuthError.TokenInvalid
        return AuthResponse(accessToken = "new-fake-access", refreshToken = "new-fake-refresh")
    }

    override suspend fun logout(userId: String) {}
}
```

**Объясни:**
- Что такое Dependency Injection и почему интерфейс важнее конкретной реализации?
- Почему `FakeAuthService` позволяет тестировать роуты без запущенной БД?
- Что такое принцип Dependency Inversion (буква D в SOLID)?

### Тесты AuthService — пишем ПЕРВЫМИ (`AuthServiceTest.kt`)

```kotlin
class AuthServiceTest : AuthTestBase() {

    private val fakeAuth = FakeAuthService()

    @Test fun `register returns tokens`() = runTest {
        val result = fakeAuth.register("Test", "test@test.com", "pass123")
        assertTrue(result.accessToken.isNotBlank())
    }

    @Test fun `register throws on duplicate email`() = runTest {
        fakeAuth.register("Test", "dup@test.com", "pass123")
        assertFailsWith<AuthError.EmailAlreadyExists> {
            fakeAuth.register("Test2", "dup@test.com", "pass123")
        }
    }

    @Test fun `login throws InvalidCredentials when shouldFailLogin`() = runTest {
        fakeAuth.shouldFailLogin = true
        assertFailsWith<AuthError.InvalidCredentials> {
            fakeAuth.login("test@test.com", "wrong")
        }
    }

    @Test fun `refresh throws TokenInvalid for invalid token`() = runTest {
        assertFailsWith<AuthError.TokenInvalid> {
            fakeAuth.refresh("invalid")
        }
    }
}
```

---

## Шаг 6: Rate Limiting — защита от брутфорса

Добавь в `Application.kt`:

```kotlin
install(RateLimit) {
    register(RateLimitName("auth")) {
        rateLimiter(limit = 5, refillPeriod = 1.minutes)
        requestKey { call -> call.request.origin.remoteHost }
    }
}
```

В `AuthRoute.kt` оборачивай только чувствительные эндпоинты:
```kotlin
rateLimit(RateLimitName("auth")) {
    post("/login") { ... }
    post("/register") { ... }
    post("/refresh") { ... }
}
post("/logout") { ... } // logout вне лимита — безопасная операция
```

**Объясни:**
- Что такое брутфорс атака и почему `/login` — главная цель?
- Почему лимитируем по IP, а не по email?
- Что такое `refillPeriod` (token bucket алгоритм)?
- Почему `StatusPages` обрабатывает `TooManyRequests` — связь с шагом 4?

### Тесты Rate Limiting — пишем ПЕРВЫМИ (`RateLimitTest.kt`)

```kotlin
class RateLimitTest : AuthTestBase() {

    @Test fun `login blocked after 5 failed attempts`() = authTest {
        val body = """{"email":"nobody@test.com","password":"wrong"}"""
        repeat(5) {
            client.post("/api/v1/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        assertEquals(HttpStatusCode.TooManyRequests, response.status)
    }

    @Test fun `rate limit error message is localized`() = authTest {
        val body = """{"email":"nobody@test.com","password":"wrong"}"""
        repeat(5) {
            client.post("/api/v1/auth/login") {
                contentType(ContentType.Application.Json); setBody(body)
            }
        }
        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.AcceptLanguage, "ru")
            setBody(body)
        }
        assertTrue(response.bodyAsText().contains("Слишком много запросов"))
    }

    @Test fun `logout is not rate limited`() = authTest {
        repeat(10) {
            val response = client.post("/api/v1/auth/logout") {
                header(HttpHeaders.Authorization, "Bearer fake")
            }
            assertNotEquals(HttpStatusCode.TooManyRequests, response.status)
        }
    }
}
```

---

## Шаг 7: Хеширование паролей — `PasswordHasher`

Добавь миграцию `V2__add_password_salt.sql`:
```sql
ALTER TABLE users ADD COLUMN password_salt VARCHAR(255);
```

Создай `auth/PasswordHasher.kt`:

```kotlin
object PasswordHasher {
    private val provider = CryptographyProvider.Default
    private const val ITERATIONS = 600_000
    private const val KEY_BYTES = 32
    private const val SALT_BYTES = 16

    data class HashedPassword(val hash: String, val salt: String)

    suspend fun hash(password: String): HashedPassword {
        val salt = CryptographyRandom.nextBytes(SALT_BYTES)
        val derived = derive(password, salt)
        return HashedPassword(
            hash = Base64.getEncoder().encodeToString(derived),
            salt = Base64.getEncoder().encodeToString(salt)
        )
    }

    suspend fun verify(password: String, storedHash: String, storedSalt: String): Boolean {
        val salt = Base64.getDecoder().decode(storedSalt)
        val derived = derive(password, salt)
        return MessageDigest.isEqual(derived, Base64.getDecoder().decode(storedHash))
    }

    private suspend fun derive(password: String, salt: ByteArray): ByteArray =
        provider.get(PBKDF2)
            .secretDerivation(SHA256, ITERATIONS, KEY_BYTES, salt)
            .deriveSecret(password.encodeToByteArray())
}
```

**Объясни:**
- Почему PBKDF2 вместо MD5/SHA256?
- Что такое `iterations = 600_000` (NIST 2024)?
- Почему `salt` случайный и хранится открыто?
- Что такое timing attack и почему `MessageDigest.isEqual()` защищает?

### Тесты PasswordHasher — пишем ПЕРВЫМИ (`PasswordHasherTest.kt`)

```kotlin
class PasswordHasherTest {
    @Test fun `hash produces non-empty result`() = runTest {
        val r = PasswordHasher.hash("secret123")
        assertTrue(r.hash.isNotBlank() && r.salt.isNotBlank())
    }

    @Test fun `same password produces different hashes`() = runTest {
        assertNotEquals(PasswordHasher.hash("secret123").hash, PasswordHasher.hash("secret123").hash)
    }

    @Test fun `verify correct password`() = runTest {
        val (hash, salt) = PasswordHasher.hash("secret123")
        assertTrue(PasswordHasher.verify("secret123", hash, salt))
    }

    @Test fun `verify wrong password`() = runTest {
        val (hash, salt) = PasswordHasher.hash("secret123")
        assertFalse(PasswordHasher.verify("wrong", hash, salt))
    }
}
```

---

## Шаг 8: JWT — `JwtService`

Создай `auth/JwtService.kt`:

```kotlin
object JwtService {
    private val secret = System.getenv("JWT_SECRET")
        ?: if (System.getenv("KTOR_ENV") == "production") error("JWT_SECRET not set") else "dev-secret"
    private const val ISSUER = "appshoptour"
    private const val AUDIENCE = "appshoptour-users"
    private val ACCESS_TTL = 15 * 60 * 1000L
    val REFRESH_TTL = 30L * 24 * 60 * 60 * 1000L

    val verifier: JWTVerifier = JWT.require(Algorithm.HMAC256(secret))
        .withIssuer(ISSUER).withAudience(AUDIENCE).build()

    fun generateAccessToken(userId: String, lang: String): String =
        buildToken(ACCESS_TTL) {
            withClaim("userId", userId)
            withClaim("lang", lang)
        }

    fun generateRefreshToken(userId: String): String =
        buildToken(REFRESH_TTL) { withClaim("userId", userId) }

    private fun buildToken(ttl: Long, block: JWTCreator.Builder.() -> Unit): String =
        JWT.create()
            .withIssuer(ISSUER).withAudience(AUDIENCE)
            .withExpiresAt(Date(System.currentTimeMillis() + ttl))
            .apply(block)
            .sign(Algorithm.HMAC256(secret))
}
```

**Объясни:** почему JWT, access vs refresh TTL, почему `lang` в JWT, структура токена.

### Тесты JwtService — пишем ПЕРВЫМИ (`JwtServiceTest.kt`)

```kotlin
class JwtServiceTest {
    @Test fun `access token has 3 parts`() =
        assertEquals(3, JwtService.generateAccessToken("id", "ru").split(".").size)

    @Test fun `token contains userId and lang`() {
        val decoded = JWT.decode(JwtService.generateAccessToken("user-1", "es"))
        assertEquals("user-1", decoded.getClaim("userId").asString())
        assertEquals("es", decoded.getClaim("lang").asString())
    }

    @Test fun `verifier accepts generated token`() {
        assertDoesNotThrow { JwtService.verifier.verify(JwtService.generateAccessToken("id", "en")) }
    }

    @Test fun `different users get different tokens`() =
        assertNotEquals(
            JwtService.generateAccessToken("user-1", "ru"),
            JwtService.generateAccessToken("user-2", "ru")
        )
}
```

---

## Шаг 9: Таблица refresh токенов

`V3__create_refresh_tokens.sql`:
```sql
CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMP NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    revoked     BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
```

`RefreshTokensTable.kt` — Exposed-таблица по аналогии с `UsersTable.kt`.

**Объясни:** почему `token_hash`, зачем `revoked`, зачем `ON DELETE CASCADE`, зачем индекс.

---

## Шаг 10: AuthRoute

Создай `api/AuthRoute.kt` как extension на `Route`. Принимает `AuthService` параметром.
Роуты просто `throw AuthError` — `StatusPages` из шага 4 перехватывает автоматически.

```kotlin
fun Route.authRoutes(authService: AuthService) {
    route("/auth") {
        rateLimit(RateLimitName("auth")) {
            post("/register") {
                val req = call.receive<RegisterRequest>()
                val errors = req.validate()
                if (errors.isNotEmpty()) throw AuthError.ValidationFailed(errors)
                call.respond(HttpStatusCode.Created, authService.register(req.name, req.email, req.password))
            }

            post("/login") {
                val req = call.receive<LoginRequest>()
                call.respond(authService.login(req.email, req.password))
            }

            post("/refresh") {
                val req = call.receive<RefreshRequest>()
                call.respond(authService.refresh(req.refreshToken))
            }
        }

        authenticate("auth-jwt") {
            post("/logout") {
                val userId = call.principal<JWTPrincipal>()!!
                    .payload.getClaim("userId").asString()
                authService.logout(userId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
```

**Объясни:**
- Почему login возвращает одно сообщение для двух случаев (user enumeration attack)?
- Что такое Refresh Token Rotation?

---

## Шаг 11: Client-side interceptor — автоматический refresh токена (KMP)

Создай `shared/src/commonMain/.../data/remote/TokenStorage.kt`:

```kotlin
interface TokenStorage {
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun saveTokens(accessToken: String, refreshToken: String)
    suspend fun clear()
}
```

Реализации через `expect/actual`:
- Android — `EncryptedSharedPreferences`
- iOS — `Keychain`

Создай `shared/src/commonMain/.../data/remote/AuthInterceptor.kt`:

```kotlin
class AuthInterceptor(
    private val tokenStorage: TokenStorage,
    private val baseUrl: String
) {
    fun install(client: HttpClient) {
        client.plugin(HttpSend).intercept { request ->
            val originalCall = execute(request)

            if (originalCall.response.status != HttpStatusCode.Unauthorized
                || request.url.encodedPath.contains("/auth/")) {
                return@intercept originalCall
            }

            val refreshToken = tokenStorage.getRefreshToken()
                ?: return@intercept originalCall

            val refreshed = tryRefresh(client, refreshToken)
                ?: return@intercept originalCall

            tokenStorage.saveTokens(refreshed.accessToken, refreshed.refreshToken)
            request.headers[HttpHeaders.Authorization] = "Bearer ${refreshed.accessToken}"
            execute(request)
        }
    }

    private suspend fun tryRefresh(client: HttpClient, refreshToken: String): AuthResponse? =
        runCatching {
            client.post("$baseUrl/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("refresh_token" to refreshToken))
            }.body<AuthResponse>()
        }.getOrNull()
}
```

Обнови `HttpClientFactory.kt`:
```kotlin
fun createHttpClient(tokenStorage: TokenStorage, baseUrl: String): HttpClient = HttpClient {
    // ... существующая настройка ...
    install(HttpSend)
}.also { AuthInterceptor(tokenStorage, baseUrl).install(it) }
```

**Объясни:**
- Что такое `HttpSend` плагин и как он перехватывает запросы?
- Почему проверяем `request.url.encodedPath.contains("/auth/")` — что произойдёт без этого?
- Почему токены хранятся в `EncryptedSharedPreferences` / `Keychain`, а не обычном хранилище?
- Что такое `expect/actual` в KMP и зачем он нужен для `TokenStorage`?

### Тесты AuthInterceptor — пишем ПЕРВЫМИ (`AuthInterceptorTest.kt`)

```kotlin
class AuthInterceptorTest {

    private val fakeStorage = FakeTokenStorage()

    @Test fun `401 triggers refresh and retries original request`() = runTest {
        fakeStorage.refreshToken = "valid-refresh"
        var requestCount = 0

        val client = createMockClient { request ->
            requestCount++
            when {
                request.url.encodedPath.contains("/auth/refresh") ->
                    respondJson("""{"access_token":"new-access","refresh_token":"new-refresh"}""")
                requestCount == 1 -> respond("", HttpStatusCode.Unauthorized)
                else -> respond("OK", HttpStatusCode.OK)
            }
        }

        AuthInterceptor(fakeStorage, "http://test").install(client)
        val response = client.get("http://test/api/v1/users")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(2, requestCount)
        assertEquals("new-access", fakeStorage.accessToken)
    }

    @Test fun `no refresh token returns original 401`() = runTest {
        fakeStorage.refreshToken = null
        val client = createMockClient { respond("", HttpStatusCode.Unauthorized) }
        AuthInterceptor(fakeStorage, "http://test").install(client)

        assertEquals(HttpStatusCode.Unauthorized, client.get("http://test/api/v1/users").status)
    }

    @Test fun `auth endpoints are not intercepted`() = runTest {
        var refreshCalled = false
        val client = createMockClient { request ->
            if (request.url.encodedPath.contains("/auth/refresh")) refreshCalled = true
            respond("", HttpStatusCode.Unauthorized)
        }
        AuthInterceptor(fakeStorage, "http://test").install(client)

        client.post("http://test/api/v1/auth/login")
        assertFalse(refreshCalled)
    }
}

class FakeTokenStorage : TokenStorage {
    var accessToken: String? = "access"
    var refreshToken: String? = null
    override suspend fun getAccessToken() = accessToken
    override suspend fun getRefreshToken() = refreshToken
    override suspend fun saveTokens(a: String, r: String) { accessToken = a; refreshToken = r }
    override suspend fun clear() { accessToken = null; refreshToken = null }
}
```

---

## Шаг 12: JWT middleware в `Application.kt`

```kotlin
install(Authentication) {
    jwt("auth-jwt") {
        verifier(JwtService.verifier)
        validate { credential ->
            if (credential.payload.getClaim("userId").asString() != null)
                JWTPrincipal(credential.payload)
            else null
        }
        challenge { _, _ ->
            val lang = call.lang()
            call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to I18n.message(I18n.MessageKey.TOKEN_INVALID, lang))
            )
        }
    }
}
```

---

## Шаг 13: Базовый класс тестов — `AuthTestBase`

```kotlin
abstract class AuthTestBase {

    protected fun authTest(block: suspend ApplicationTestBuilder.() -> Unit) =
        testApplication {
            application { module() }
            block()
        }

    protected suspend fun ApplicationTestBuilder.registerUser(
        email: String = "test@test.com",
        password: String = "password123",
        lang: String = "en"
    ): String {
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.AcceptLanguage, lang)
            setBody("""{"name":"Test","email":"$email","password":"$password"}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
        return // распарсить access_token из response
    }
}
```

### Интеграционные тесты (`AuthRouteTest.kt`)

```kotlin
class AuthRouteTest : AuthTestBase() {

    @Test fun `register returns 201 with tokens`() = authTest {
        assertTrue(registerUser().isNotBlank())
    }

    @Test fun `register 409 on duplicate email`() = authTest {
        registerUser(email = "dup@test.com")
        val r = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"X","email":"dup@test.com","password":"password123"}""")
        }
        assertEquals(HttpStatusCode.Conflict, r.status)
    }

    @Test fun `login same error for wrong email and wrong password`() = authTest {
        registerUser(email = "user@test.com")
        val wrongEmail = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"nobody@test.com","password":"pass"}""")
        }
        val wrongPass = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"user@test.com","password":"wrongpass"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, wrongEmail.status)
        assertEquals(wrongEmail.bodyAsText(), wrongPass.bodyAsText())
    }

    @Test fun `protected route 401 without token`() = authTest {
        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/v1/users").status)
    }

    @Test fun `protected route 200 with valid token`() = authTest {
        val token = registerUser()
        val r = client.get("/api/v1/users") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, r.status)
    }
}
```

---

## Итоговая структура файлов

```
server/src/main/kotlin/com/example/appshoptour/
├── Application.kt                     (обновить: StatusPages, RateLimit, Auth, authRoutes())
├── api/
│   ├── AuthRoute.kt                   (новый)
│   ├── CallExtensions.kt              (новый — lang(), respondError())
│   ├── UsersRoute.kt                  (обновить: authenticate{})
│   └── dto/
│       ├── AuthRequest.kt             (новый)
│       ├── AuthResponse.kt            (новый)
│       └── UserResponse.kt            (существующий)
├── auth/
│   ├── AuthError.kt                   (новый — sealed class + toHttpStatus/toMessageKey)
│   ├── AuthService.kt                 (новый — интерфейс + FakeAuthService)
│   ├── AuthServiceImpl.kt             (новый — реализация с Exposed)
│   ├── I18n.kt                        (новый)
│   ├── JwtService.kt                  (новый)
│   └── PasswordHasher.kt              (новый)
└── database/table/
    ├── UsersTable.kt                  (обновить: passwordSalt)
    └── RefreshTokensTable.kt          (новый)

shared/src/commonMain/.../data/remote/
├── HttpClientFactory.kt               (обновить: подключить AuthInterceptor)
├── AuthInterceptor.kt                 (новый)
└── TokenStorage.kt                    (новый — интерфейс)

shared/src/androidMain/.../data/remote/
└── TokenStorageImpl.android.kt        (новый — EncryptedSharedPreferences)

shared/src/iosMain/.../data/remote/
└── TokenStorageImpl.ios.kt            (новый — Keychain)

server/src/test/kotlin/com/example/appshoptour/
├── AuthTestBase.kt                    (новый — общий setup)
├── I18nTest.kt                        (новый)
├── PasswordHasherTest.kt              (новый)
├── JwtServiceTest.kt                  (новый)
├── StatusPagesTest.kt                 (новый)
├── AuthServiceTest.kt                 (новый)
├── RateLimitTest.kt                   (новый)
└── AuthRouteTest.kt                   (новый)

shared/src/commonTest/.../data/remote/
└── AuthInterceptorTest.kt             (новый)

server/src/main/resources/db/migration/
├── V1__create_users.sql
├── V2__add_password_salt.sql          (новый)
└── V3__create_refresh_tokens.sql      (новый)
```

---

## Итоговые вопросы для понимания

**TDD**
1. Что такое Red-Green-Refactor на практике?
2. Почему тест, который сразу проходит — бесполезен?

**Архитектура**
3. Что такое Dependency Inversion (SOLID) и как `AuthService` интерфейс его реализует?
4. Чем отличается аутентификация от авторизации?
5. Что такое Railway-Oriented Programming и как `Result<T>` связан с `AuthError`?

**Безопасность**
6. Почему HTTPS обязателен при работе с токенами?
7. Что такое OWASP Top 10 и какие пункты закрыты этой реализацией?
8. Почему брутфорс на `/login` опасен и как Rate Limiting защищает?
9. Что такое timing attack и почему `MessageDigest.isEqual()` защищает?
10. Почему login возвращает одно сообщение для двух случаев (user enumeration)?

**KMP**
11. Что такое `expect/actual` и зачем для `TokenStorage`?
12. Почему токены хранятся в `EncryptedSharedPreferences` / `Keychain`, а не обычном хранилище?
13. Как `HttpSend` interceptor работает и почему нужна защита от рекурсии на `/auth/` путях?

**i18n**
14. Что такое i18n vs l10n?
15. Почему fallback на `en`, а не на `ru`?
