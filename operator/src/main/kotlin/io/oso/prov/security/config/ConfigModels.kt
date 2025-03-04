package io.oso.prov.security.config

import com.squareup.moshi.Json

data class SecurityConfig(
    val http: Http = Http(),
    val authc: Authc = Authc()
)

data class Http(
    @Json(name = "anonymous_auth_enabled")
    val anonymousAuthEnabled: Boolean = false,
    val xff: Xff? = null
)

data class Xff(
    val enabled: Boolean = false,
    val internalProxies: String = "127\\.0\\.0\\.1",
)

data class Authc(
    val proxy: ProxyAuthDomain? = null,
    val basic: BasicAuthDomain? = BasicAuthDomain(),
)

data class ProxyAuthDomain(
    @Json(name = "http_enabled")
    val httpEnabled: Boolean = true,
    val order: Int = 0,
    @Json(name = "http_authenticator")
    val httpAuthenticator: ProxyAuthenticator = ProxyAuthenticator(),
) {
    @Json(name = "authentication_backend")
    val authenticationBackend: NoopAuthBackend = NoopAuthBackend()
}

data class ProxyAuthenticator(
    val config: ProxyConfig = ProxyConfig()
) {
    val type: String = "proxy"
    val challenge: Boolean = false
}

data class ProxyConfig(
    @Json(name = "user_header")
    val userHeader: String = "x-proxy-user",
    @Json(name = "roles_header")
    val rolesHeader: String = "x-proxy-roles",
)

data class BasicAuthDomain(
    @Json(name = "http_enabled")
    val httpEnabled: Boolean = true,
    @Json(name = "transport_enabled")
    val transportEnabled: Boolean = true,
    val order: Int = 10,
    @Json(name = "http_authenticator")
    val httpAuthenticator: BasicAuthenticator = BasicAuthenticator(),
) {
    @Json(name = "authentication_backend")
    val authenticationBackend: InternalAuthBackend = InternalAuthBackend()
}

class BasicAuthenticator {
    val type: String = "basic"
    val challenge: Boolean = true
}

class InternalAuthBackend {
    val type: String = "internal"
}

class NoopAuthBackend {
    val type: String = "noop"
}

