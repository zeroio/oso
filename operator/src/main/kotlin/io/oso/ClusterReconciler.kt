package io.oso

import io.javaoperatorsdk.operator.api.reconciler.Context
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration
import io.javaoperatorsdk.operator.api.reconciler.Reconciler
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl
import io.javaoperatorsdk.operator.api.reconciler.Workflow
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent
import io.oso.api.OSCluster
import io.oso.common.logger
import io.oso.prov.cfgmap.ScriptConfigMap

@Workflow(
    dependents = [
        Dependent(type = ScriptConfigMap::class, name = ScriptConfigMap.NAME),
    ]
)
@ControllerConfiguration
class ClusterReconciler: Reconciler<OSCluster> {
    override fun reconcile(cluster: OSCluster, context: Context<OSCluster>): UpdateControl<OSCluster> {
        log.debug("Reconciling cluster {}", cluster.metadata.name)
        return UpdateControl.noUpdate()
    }

    companion object {
        private val log = logger<ClusterReconciler>()
    }
}