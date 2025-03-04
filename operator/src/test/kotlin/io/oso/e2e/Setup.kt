package io.oso.e2e

import io.fabric8.kubernetes.api.model.NamespaceBuilder
import io.fabric8.kubernetes.client.KubernetesClient
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object Setup {
    private val log = LoggerFactory.getLogger(Setup::class.java)
    private val DATE_FORMATER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneOffset.UTC)
    private val currentUser = System.getProperty("user.name")?: "user"
    private val runId: String = DATE_FORMATER.format(Instant.now()) + "-" + currentUser

    val namespacePrefix: String = "oso-e2e-$runId"

    fun KubernetesClient.createTestNamespace(testGroup: String): String {
        val namespaceName = "$namespacePrefix$testGroup"
        log.info("Creating/updating namespace $namespaceName")
        val namespace = NamespaceBuilder()
            .withNewMetadata()
            .withName(namespaceName)
            .endMetadata()
            .build()
        namespaces().resource(namespace).serverSideApply()
        return namespaceName
    }

    fun KubernetesClient.deleteTestNamespace(testGroup: String) {
        val namespaceName = "$namespacePrefix$testGroup"
        log.info("Deleting namespace $namespaceName")
        namespaces().withName(namespaceName).delete()
    }
}