package io.oso.prov.security.config

import com.squareup.moshi.Json
import io.oso.api.IndexPermissionSpec
import io.oso.api.RoleSpec
import io.oso.api.UserSpec

data class User(
    val description: String? = null,
    @Json(name = "hash")
    val passwordHash: String,
    val reserved: Boolean = false,
    val hidden: Boolean = false,
    @Json(name = "backend_roles")
    val backendRoles: Set<String>,
    val attributes: Map<String, String> = emptyMap(),
    val static: Boolean = false,
)

fun UserSpec.toUser(): User = User(
        passwordHash = this.passwordHash,
        reserved = false,
        hidden = false,
        backendRoles = this.backendRoles,
    )

data class Role(
    val reserved: Boolean = false,
    val hidden: Boolean = false,
    val static: Boolean = false,
    @Json(name = "cluster_permissions")
    val clusterPermissions: Set<String> = emptySet(),
    @Json(name = "index_permissions")
    val indexPermissions: List<IndexPermission> = emptyList(),
    @Json(name = "tenant_permissions")
    val tenantPermissions: List<TenantPermission> = emptyList(),
)

fun RoleSpec.toRole(): Role = Role(
    reserved = false,
    hidden = false,
    clusterPermissions = this.clusterPermissions,
    indexPermissions = this.indexPermissions.map { it.toIndexPermission() },
)

data class IndexPermission(
    @Json(name = "index_patterns")
    val indexPatterns: Set<String>,
    val dls: String? = null,
    val fls: Set<String> = emptySet(),
    @Json(name = "masked_fields")
    val maskedFields: Set<String> = emptySet(),
    @Json(name = "allowed_actions")
    val allowedActions: Set<String> = emptySet(),
)

fun IndexPermissionSpec.toIndexPermission(): IndexPermission = IndexPermission(
    indexPatterns = this.indexPatterns,
    dls = this.dls,
    fls = this.fls,
    maskedFields = this.maskedFields,
    allowedActions = this.allowedActions,
)

data class TenantPermission(
    @Json(name = "index_patterns")
    val patterns: Set<String>,
    @Json(name = "allowed_actions")
    val allowedActions: Set<String> = emptySet(),
)

data class RolesMapping(
    val reserved: Boolean = false,
    val hidden: Boolean = false,
    @Json(name = "backend_roles")
    val backendRoles: Set<String> = emptySet(),
    val hosts: Set<String> = emptySet(),
    val users: Set<String> = emptySet(),
    @Json(name = "and_backend_roles")
    val andBackendRoles: Set<String> = emptySet(),
)

data class Tenant(
    val reserved: Boolean = false,
    val description: String? = null,
)