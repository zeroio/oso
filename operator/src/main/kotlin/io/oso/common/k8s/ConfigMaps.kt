package io.oso.common.k8s

import io.fabric8.kubernetes.api.model.ConfigMapBuilder

data class ConfigMapEntry(val name: String, val value: String)

object ConfigMaps {
    fun ConfigMapBuilder.addEntry(entry: ConfigMapEntry): ConfigMapBuilder {
        return this.addToData(entry.name, entry.value)
    }
}
