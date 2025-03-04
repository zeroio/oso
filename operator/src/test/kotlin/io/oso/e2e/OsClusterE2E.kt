package io.oso.e2e

import io.fabric8.kubernetes.api.model.NamespaceBuilder
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import org.junit.jupiter.api.Test

class OsClusterE2E {
    private val client = KubernetesClientBuilder().build()

    @Test
    fun `can create cluster`() {
        val namespace = NamespaceBuilder()
            .withNewMetadata()
                .withName("test-cluster")
            .endMetadata()
            .build()

        client.namespaces().resource(namespace).serverSideApply()
    }
}