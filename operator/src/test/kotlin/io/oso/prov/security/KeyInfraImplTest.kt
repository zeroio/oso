package io.oso.prov.security

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.oso.prov.security.cert.KeyInfraImpl
import io.oso.prov.security.cert.toPem
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder
import org.junit.jupiter.api.Test
import java.security.KeyPair
import java.time.Duration
import java.time.Instant
import java.util.Date

class KeyInfraImplTest {
    private val sut: KeyInfraImpl = KeyInfraImpl()

    @Test
    fun `generateKeyPair should generate a valid KeyPair`() {
        val keyPair: KeyPair = sut.generateKeyPair()

        keyPair.public shouldNotBe null
        keyPair.private shouldNotBe null
    }

    @Test
    fun `generateRootCert should generate a valid certificate`() {
        val keyPair: KeyPair = sut.generateKeyPair()
        val validFrom = Instant.ofEpochSecond(Instant.now().epochSecond)
        val duration = Duration.ofDays(365)

        val cert = sut.generateRootCert(keyPair, "CN=Test CA", validFrom, duration)

        val issuer = cert.issuerX500Principal
        issuer.name shouldBe "CN=Test CA"
        val subject = cert.subjectX500Principal
        subject.name shouldBe "CN=Test CA"
        cert.notBefore shouldBe Date.from(validFrom)
        cert.notAfter shouldBe Date.from(validFrom.plus(duration))
        cert.verify(keyPair.public)
    }

    @Test
    fun `generateCert should generate a valid certificate`() {
        val rootKeyPair: KeyPair = sut.generateKeyPair()
        val rootCert = sut.generateRootCert(rootKeyPair, "CN=Test CA")
        val keyPair: KeyPair = sut.generateKeyPair()
        val validFrom = Instant.ofEpochSecond(Instant.now().epochSecond)
        val duration = Duration.ofDays(365)

        val cert =
            sut.generateCert(rootCert, rootKeyPair.private, keyPair, "CN=Test CA", "CN=Test", validFrom, duration)

        val issuer = cert.issuerX500Principal
        issuer.name shouldBe "CN=Test CA"
        val subject = cert.subjectX500Principal
        subject.name shouldBe "CN=Test"
        cert.notBefore shouldBe Date.from(validFrom)
        cert.notAfter shouldBe Date.from(validFrom.plus(duration))
        cert.verify(rootKeyPair.public)
    }

}