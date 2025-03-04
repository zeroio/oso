package io.oso.prov.security.config

import io.fabric8.kubernetes.api.model.Secret
import io.fabric8.kubernetes.api.model.SecretBuilder
import io.oso.api.AuthenticationSpec
import io.oso.api.RoleSpec
import io.oso.api.SecuritySpec
import io.oso.api.UserSpec
import io.oso.common.Jsons
import io.oso.common.os.Meta

object ConfigSecrets {
    fun securityConfig(securitySpec: SecuritySpec): Secret {
        return SecretBuilder()
            .withNewMetadata()
            .withName("os-security-config")
            .addToLabels(
                mapOf(
                    "ios.oso/secret" to "os-security-config",
                )
            )
            .endMetadata()
            .addToData(
                mapOf(
                    "config.yml" to generateSecurityConfigYmlContent(securitySpec.auth),
                    "internal_users.yml" to generateInternalUsersYmlContent(securitySpec.users),
                    "roles.yml" to generateRolesYmlContent(securitySpec.roles),
                )
            )
            .build()
    }

    private fun generateSecurityConfigYmlContent(authentication: AuthenticationSpec): String {
        val proxies = authentication.internalProxies
        val http = Http(
            xff = if (proxies.isNullOrBlank()) null else Xff(true, proxies),
        )
        val authc = Authc(
            basic = if (authentication.basic) BasicAuthDomain() else null,
            proxy = if (proxies.isNullOrBlank()) null else ProxyAuthDomain()
        )
        val configFile = mapOf(
            "_meta" to Meta(type = "config"),
            "config" to mapOf("dynamic" to SecurityConfig(http = http, authc = authc))
        )
        return Jsons.toJson(configFile)
    }

    private fun generateInternalUsersYmlContent(users: Map<String, UserSpec>): String {
        val configFile = mapOf("_meta" to Meta(type = "internalusers")) +
                users.mapValues { (_, user) -> Jsons.toJson(user.toUser()) }
        return Jsons.toJson(configFile)
    }

    private fun generateRolesYmlContent(roles: Map<String, RoleSpec>): String {
        val configFile = mapOf("_meta" to Meta(type = "roles")) +
                roles.mapValues { (_, role) -> Jsons.toJson(role.toRole()) }
        return Jsons.toJson(configFile)
    }
}