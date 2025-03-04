package io.oso.api

import io.fabric8.kubernetes.api.model.Namespaced
import io.fabric8.kubernetes.client.CustomResource
import io.fabric8.kubernetes.model.annotation.Group
import io.fabric8.kubernetes.model.annotation.Version
import io.fabric8.kubernetes.model.annotation.Plural
import io.fabric8.kubernetes.model.annotation.Singular

@Group("oso.io")
@Version("v1")
@Plural("clusters")
@Singular("cluster")
class OSCluster: CustomResource<OSClusterSpec, OSClusterState>(), Namespaced

data class OSClusterSpec(
    val engineVersion: String,
    val masterNodes: NodesSpec = NodesSpec(
        count = 0,
        size = NodeSize.SMALL,
        disk = DiskSpec.MASTER_DEFAULT,
    ),
    val dataNodes: NodesSpec = NodesSpec(
        count = 1,
        size = NodeSize.SMALL,
        disk = DiskSpec.DATA_DEFAULT,
    ),
    val security: SecuritySpec = SecuritySpec(),
)

data class NodesSpec(
    val count: Int,
    val size: NodeSize,
    val dedicatedNode: Boolean = false,
    val disk: DiskSpec,
)

enum class NodeSize {
    SMALL,
    MEDIUM,
    LARGE,
    LARGE_X2,
    LARGE_X4,
    LARGE_X8,
    LARGE_X12,
    LARGE_X16,
}

data class DiskSpec(
    val size: Int,
    val iops: Int? = null,
    val throughput: Int? = null,
) {
    companion object {
        val MASTER_DEFAULT = DiskSpec(size = 10)
        val DATA_DEFAULT = DiskSpec(size = 100)
    }
}

data class SecuritySpec(
    val auth: AuthenticationSpec = AuthenticationSpec(),
    val roles: Map<String, RoleSpec> = emptyMap(),
    val users: Map<String, UserSpec> = emptyMap(),
)

data class AuthenticationSpec(
    val basic: Boolean = true,
    val internalProxies: String? = null,
)

data class RoleSpec(
    val clusterPermissions: Set<String> = emptySet(),
    val indexPermissions: List<IndexPermissionSpec> = emptyList(),
)

data class IndexPermissionSpec(
    val indexPatterns: Set<String>,
    val dls: String = "",
    val fls: Set<String> =  emptySet(),
    val maskedFields: Set<String> = emptySet(),
    val allowedActions: Set<String>,
)

data class UserSpec(
    val passwordHash: String,
    val backendRoles: Set<String> = emptySet(),
)

data class OSClusterStatus(
    val lifecycleState: OSClusterLifecycleState,
    val state: OSClusterState,
)

enum class OSClusterLifecycleState {
    CREATING,
    CREAT_FAILED,
    UPDATING,
    UPDATING_FAILED,
    DELETING,
    DELETING_FAILED,
}

enum class OSClusterState {
    RED,
    YELLOW,
    GREEN,
}