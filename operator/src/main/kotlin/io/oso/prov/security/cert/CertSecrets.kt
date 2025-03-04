package io.oso.prov.security.cert

import io.fabric8.kubernetes.api.model.Secret
import io.fabric8.kubernetes.api.model.SecretBuilder
import java.security.PrivateKey
import java.security.cert.X509Certificate

object CertSecrets {
    fun ca(cert: X509Certificate, privateKey: PrivateKey): Secret {
        return SecretBuilder()
            .withNewMetadata()
            .withName("os-ca")
            .addToLabels(
                mapOf(
                    "ios.oso/secret" to "os-ca",
                )
            )
            .endMetadata()
            .addToData(
                mapOf(
                    "ca.crt" to cert.toPem(),
                    "ca.key" to privateKey.toPem(),
                )
            )
            .build()
    }

    fun tlsCert(ca: X509Certificate, caKey: PrivateKey, cert: X509Certificate, privateKey: PrivateKey): Secret {
        return SecretBuilder()
            .withNewMetadata()
            .withName("os-tls-cert")
            .addToLabels(
                mapOf(
                    "ios.oso/secret" to "os-tls-cert",
                )
            )
            .endMetadata()
            .addToData(
                mapOf(
                    "tls.crt" to cert.toPem(),
                    "tls.key" to privateKey.toPem(),
                    "ca.crt" to ca.toPem(),
                )
            )
            .build()
    }

    fun httpsCert(ca: X509Certificate, caKey: PrivateKey, cert: X509Certificate, privateKey: PrivateKey): Secret {
        return SecretBuilder()
            .withNewMetadata()
            .withName("os-https-cert")
            .addToLabels(
                mapOf(
                    "ios.oso/secret" to "os-https-cert",
                )
            )
            .endMetadata()
            .addToData(
                mapOf(
                    "https.crt" to cert.toPem(),
                    "https.key" to privateKey.toPem(),
                    "ca.crt" to ca.toPem(),
                )
            )
            .build()
    }
}