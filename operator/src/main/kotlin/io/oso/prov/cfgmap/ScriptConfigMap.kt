package io.oso.prov.cfgmap

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.ConfigMapBuilder
import io.javaoperatorsdk.operator.api.config.informer.Informer
import io.javaoperatorsdk.operator.api.reconciler.Context
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent
import io.oso.api.OSCluster
import io.oso.common.k8s.ConfigMaps.addEntry
import io.oso.prov.init.ConfigMaps

@KubernetesDependent(informer = Informer(labelSelector = "oso.io/config-map=script"))
class ScriptConfigMap: CRUDKubernetesDependentResource<ConfigMap, OSCluster>(ConfigMap::class.java, NAME) {
    override fun desired(primary: OSCluster, context: Context<OSCluster>): ConfigMap =
        ConfigMapBuilder()
            .withNewMetadata()
            .withName(NAME)
            .addToLabels("oso.io/config-map", "script")
            .endMetadata()
            .addEntry(ConfigMaps.initScript())
            .build()

    companion object {
        const val NAME: String = "os-script"
    }
}