package io.oso.common.k8s.volumes

import io.fabric8.kubernetes.api.model.KeyToPath
import io.fabric8.kubernetes.api.model.KeyToPathBuilder
import io.fabric8.kubernetes.api.model.Volume
import io.fabric8.kubernetes.api.model.VolumeBuilder
import io.fabric8.kubernetes.api.model.VolumeMount
import io.fabric8.kubernetes.api.model.VolumeMountBuilder

data class SecretVolume(
    override val name: String,
    val mountPath: String,
    val secretName: String,
    val items: List<KeyToPath> = emptyList(),
    val subPath: String? = null,
    val defaultMode: Int? = null
): VolumeLike {
    override fun mount(): VolumeMount = VolumeMountBuilder()
        .withName(name)
        .withMountPath(mountPath)
        .withSubPath(subPath)
        .withReadOnly()
        .build()

    override fun volume(): Volume = VolumeBuilder()
        .withName(name)
        .withNewSecret()
        .withSecretName(secretName)
        .withItems(items)
        .withDefaultMode(defaultMode)
        .endSecret()
        .build()

    companion object {
        fun volumeWithMountPath(
            secretName: String,
            name: String,
            mountPath: String,
        ) = SecretVolume(
            name = name,
            mountPath = mountPath,
            secretName = secretName,
        )

        fun volumeWithProjectedSecrets(
            secretName: String,
            name: String,
            mountPath: String,
            projectedSecrets: Collection<String>
        ) = SecretVolume(
            name = name,
            mountPath = mountPath,
            secretName = secretName,
            items = projectedSecrets.map { KeyToPathBuilder().withKey(it).withPath(it).build() }
        )
    }
}