package io.oso.common.k8s.volumes

import io.fabric8.kubernetes.api.model.Volume
import io.fabric8.kubernetes.api.model.VolumeBuilder
import io.fabric8.kubernetes.api.model.VolumeMount
import io.fabric8.kubernetes.api.model.VolumeMountBuilder

data class EmptyDirVolume(override val name: String, val mountPath: String): VolumeLike {
    override fun mount(): VolumeMount = VolumeMountBuilder()
        .withName(name)
        .withMountPath(mountPath)
        .build()

    override fun volume(): Volume = VolumeBuilder()
        .withName(name)
        .withNewEmptyDir()
        .endEmptyDir()
        .build()
}

