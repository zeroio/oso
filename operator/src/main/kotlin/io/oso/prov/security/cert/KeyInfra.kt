package io.oso.prov.security.cert

import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter
import java.io.StringWriter
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.time.Duration
import java.time.Instant
import java.util.Date


interface KeyInfra {
    fun generateKeyPair(): KeyPair

    fun generateRootCert(
        key: KeyPair,
        name: String,
        validFrom: Instant = Instant.now(),
        expiration: Duration = CERT_EXPIRATION_365_DAYS,
    ): X509Certificate

    fun generateCert(
        rootCert: X509Certificate,
        rootPrivateKey: PrivateKey,
        key: KeyPair,
        issuer: String,
        subject: String,
        validFrom: Instant = Instant.now(),
        expiration: Duration = CERT_EXPIRATION_365_DAYS,
        dnsNames: Collection<String> = emptyList(),
        ipAddresses: Collection<String> = emptyList(),
    ): X509Certificate

    companion object {
        private val CERT_EXPIRATION_365_DAYS = Duration.ofDays(365)
    }
}

private fun ByteArray.toPem(type: String): String {
    StringWriter().use { writer ->
        PemWriter(writer).use { pemWriter ->
            pemWriter.writeObject(PemObject(type, this))
        }
        return writer.toString()
    }
}

fun Certificate.toPem(): String = encoded.toPem("CERTIFICATE")

fun PrivateKey.toPem(): String = encoded.toPem("PRIVATE KEY")

class KeyInfraImpl : KeyInfra {
    override fun generateKeyPair(): KeyPair {
        val keyGen = KeyPairGenerator.getInstance("RSA", PROVIDER)
        keyGen.initialize(2048, SecureRandom())
        return keyGen.generateKeyPair()
    }

    override fun generateRootCert(
        key: KeyPair,
        name: String,
        validFrom: Instant,
        expiration: Duration
    ): X509Certificate {
        // Building the cert
        val x500Name = X500Name(name)
        val rootSerialNum = BigInteger(SecureRandom().nextLong().toString())
        val certBuilder = JcaX509v3CertificateBuilder(
            x500Name,
            rootSerialNum,
            Date.from(validFrom),
            Date.from(validFrom.plus(expiration)),
            x500Name,
            SubjectPublicKeyInfo.getInstance(key.public.encoded),
        )
        // Add extension
        // Use BasicConstraints to say that this cert is a CA
        certBuilder.addExtension(Extension.basicConstraints, true, BasicConstraints(true))
        val rootCertExtUtils = JcaX509ExtensionUtils()
        certBuilder.addExtension(
            Extension.subjectKeyIdentifier,
            false,
            rootCertExtUtils.createSubjectKeyIdentifier(key.public)
        )

        // Sign the cert with the private key
        val signer = JcaContentSignerBuilder(SIGNATURE_ALGORITHM)
            .setProvider(PROVIDER)
            .build(key.private)
        val holder = certBuilder.build(signer)

        return JcaX509CertificateConverter().setProvider(PROVIDER).getCertificate(holder)
    }

    override fun generateCert(
        rootCert: X509Certificate,
        rootPrivateKey: PrivateKey,
        key: KeyPair,
        issuer: String,
        subject: String,
        validFrom: Instant,
        expiration: Duration,
        dnsNames: Collection<String>,
        ipAddresses: Collection<String>,
    ): X509Certificate {
        // Generating a CSR (Certificate Signing Request)
        // and signing it with the cert private key
        val p10Builder: PKCS10CertificationRequestBuilder =
            JcaPKCS10CertificationRequestBuilder(X500Name(subject), key.public)
        val csrSigner = JcaContentSignerBuilder(SIGNATURE_ALGORITHM)
            .setProvider(PROVIDER)
            .build(key.private)
        val csr = p10Builder.build(csrSigner)

        // Building the cert
        val serialNum = BigInteger(SecureRandom().nextLong().toString())
        val certBuilder = X509v3CertificateBuilder(
            X500Name(issuer),
            serialNum,
            Date.from(validFrom),
            Date.from(validFrom.plus(expiration)),
            csr.subject,
            csr.subjectPublicKeyInfo
        )
        // Add Extensions
        // Use BasicConstraints to say that this Cert is not a CA
        certBuilder.addExtension(Extension.basicConstraints, true, BasicConstraints(false));
        // Add Issuer cert identifier as Extension
        val certExtUtils = JcaX509ExtensionUtils()
        certBuilder.addExtension(Extension.authorityKeyIdentifier, false, certExtUtils.createAuthorityKeyIdentifier(rootCert))
        certBuilder.addExtension(Extension.subjectKeyIdentifier, false, certExtUtils.createSubjectKeyIdentifier(csr.subjectPublicKeyInfo))
        // Add DNS names and IP addresses as alternative names
        val alternativeDnsNames = dnsNames.map { GeneralName(GeneralName.dNSName, it) }
        val alternativeIPs = ipAddresses.map { GeneralName(GeneralName.iPAddress, it) }
        certBuilder.addExtension(
            Extension.subjectAlternativeName,
            false,
            DERSequence((alternativeDnsNames + alternativeIPs).toTypedArray())
        )

        // Sign the cert with root private key
        val signer = JcaContentSignerBuilder(SIGNATURE_ALGORITHM)
            .setProvider(PROVIDER)
            .build(rootPrivateKey)
        val holder = certBuilder.build(signer)

        return JcaX509CertificateConverter().setProvider(PROVIDER).getCertificate(holder)
    }

    companion object {
        private const val SIGNATURE_ALGORITHM = "SHA256WithRSA"
        private val PROVIDER = BouncyCastleFipsProvider()
    }
}