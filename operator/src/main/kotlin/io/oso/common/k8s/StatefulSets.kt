package io.oso.common.k8s

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim
import io.fabric8.kubernetes.api.model.apps.StatefulSet
import io.fabric8.kubernetes.client.KubernetesClient

object StatefulSets {
    val StatefulSet.replicasCount: Int get() = this.spec.replicas ?: 0

    val StatefulSet.podNames: Sequence<String> get() = sequence {
        for (i in 0 until replicasCount) {
            yield("${metadata.name}-$i")
        }
    }

    fun StatefulSet.retrievePVCs(client: KubernetesClient): Map<String, PersistentVolumeClaim> {
        val ret = mutableMapOf<String, PersistentVolumeClaim>()
        for (pod in this.podNames) {
            for (claim in this.spec.volumeClaimTemplates) {
                if (claim.metadata.name.isEmpty()) {
                    continue
                }
                val pvcName = "${this.metadata.name}-${claim.metadata.name}"
                val pvc = client.persistentVolumeClaims().inNamespace(this.metadata.namespace).withName(pvcName).get()
                    ?: continue
                ret[pvcName] = pvc
            }
        }
        return ret
    }
}