package io.oso.common.k8s.volumes

import io.fabric8.kubernetes.api.model.Volume
import io.fabric8.kubernetes.api.model.VolumeMount

interface VolumeLike {
    val name: String
    fun mount(): VolumeMount
    fun volume(): Volume
}