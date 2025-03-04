package io.oso.prov.init

import io.fabric8.kubernetes.api.model.Container
import io.fabric8.kubernetes.api.model.ContainerBuilder
import io.fabric8.kubernetes.api.model.VolumeMountBuilder
import io.oso.common.k8s.volumes.SecretVolume

class Containers(
    private val transportCertsVol: SecretVolume,
    private val nodeLabelsAsAnnotations: Set<String>,
) {
    fun init(): Container {
        val volMounts = (listOf(transportCertsVol.mount()) + SharedVolumes.mounts).toMutableList()
        if (nodeLabelsAsAnnotations.isNotEmpty()) {
            volMounts += VolumeMountBuilder()
                .withName("node-labels")
                .withMountPath("/mnt/oso/node-labels")
                .withReadOnly()
                .build()
        }
        return ContainerBuilder().build()
    }
}