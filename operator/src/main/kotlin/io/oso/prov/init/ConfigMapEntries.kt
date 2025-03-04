package io.oso.prov.init

import io.oso.common.k8s.ConfigMapEntry

object ConfigMaps {
    fun initScript(): ConfigMapEntry {
        val content = this::class.java
            .getResourceAsStream("prov/init.sh")
            ?.use { it.bufferedReader().readText() }
            ?: error("/prov/init/init.sh not found")
        return ConfigMapEntry("init.sh", content)
    }
}