package io.oso.common.k8s.volumes

import io.fabric8.kubernetes.api.model.Volume
import io.fabric8.kubernetes.api.model.VolumeBuilder
import io.fabric8.kubernetes.api.model.VolumeMount
import io.fabric8.kubernetes.api.model.VolumeMountBuilder

data class HostVolume(
    override val name: String,
    val mountPath: String,
    val readonly: Boolean = false,
    val type: String? = null,
): VolumeLike {
    override fun mount(): VolumeMount = VolumeMountBuilder()
        .withName(name)
        .withMountPath(mountPath)
        .withReadOnly(readonly)
        .build()

    override fun volume(): Volume = VolumeBuilder()
        .withName(name)
        .withNewHostPath()
        .withPath(mountPath)
        .withType(type)
        .endHostPath()
        .build()
}