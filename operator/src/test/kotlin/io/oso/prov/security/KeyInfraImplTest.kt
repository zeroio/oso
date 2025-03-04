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
    fun `generateCA should generate a valid certificate`() {
        val keyPair: KeyPair = sut.generateKeyPair()
        val validFrom = Instant.ofEpochSecond(Instant.now().epochSecond)
        val duration = Duration.ofDays(365)

        val certWithKey = sut.generateCA(keyPair, "Test CA", validFrom, duration)
        certWithKey.key shouldContain "BEGIN PRIVATE KEY"
        certWithKey.key shouldBe keyPair.private.toPem()
        val pemObject = PEMParser(certWithKey.cert.reader()).readPemObject()
        pemObject.type shouldBe "CERTIFICATE"
        val certHolder = X509CertificateHolder(pemObject.content)
        certHolder.issuer shouldBe X500Name("CN=Test CA")
        certHolder.subject shouldBe X500Name("CN=Test CA")
        certHolder.notBefore shouldBe Date.from(validFrom)
        certHolder.notAfter shouldBe Date.from(validFrom.plus(duration))
        val verifier = JcaContentVerifierProviderBuilder().build(keyPair.public)
        certHolder.isSignatureValid(verifier) shouldBe true

    }
}