package io.oso.prov.init

import io.fabric8.kubernetes.api.model.Volume
import io.fabric8.kubernetes.api.model.VolumeBuilder
import io.fabric8.kubernetes.api.model.VolumeMount
import io.fabric8.kubernetes.api.model.VolumeMountBuilder
import io.oso.prov.OS_HOME

enum class SharedVolumes(
    private val volName: String,
    private val path: String,
) {
    Bin("bin", "$OS_HOME/bin"),
    Plugin("plugins", "$OS_HOME/plugins"),
    Config("config", "$OS_HOME/config");

    fun mount(): VolumeMount = VolumeMountBuilder()
        .withName(volName)
        .withMountPath(path)
        .build()

    fun volume(): Volume = VolumeBuilder()
        .withName(volName)
        .withNewEmptyDir()
        .endEmptyDir()
        .build()

    companion object {
        val mounts = entries.map { it.mount() }
        val volumes = entries.map { it.volume() }
    }
}