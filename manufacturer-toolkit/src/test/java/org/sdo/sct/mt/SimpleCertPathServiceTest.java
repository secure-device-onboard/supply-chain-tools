// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.sdo.sct.BouncyCastleSingleton;
import org.sdo.sct.KeyFinder;
import org.sdo.sct.KeyStores;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.Period;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.junit.jupiter.api.Test;

class SimpleCertPathServiceTest {

  String signatureAlg = "SHA256withECDSA";

  char[] pw() {
    return "123456".toCharArray();
  }

  X509Certificate buildCertificate(PublicKey signee, PrivateKey signer)
    throws CertificateException, CertIOException, OperatorCreationException {

    X500Name x500Name = new X500NameBuilder().build();
    JcaX509v3CertificateBuilder certificateBuilder =
      new JcaX509v3CertificateBuilder(
        x500Name,
        new BigInteger(Long.toString(System.currentTimeMillis())),
        Date.from(Instant.now()),
        Date.from(Instant.now().plusSeconds(1000)),
        x500Name,
        signee);
    certificateBuilder.addExtension(
      new ASN1ObjectIdentifier("2.5.29.19"), true, new BasicConstraints(true));

    return new JcaX509CertificateConverter()
      .setProvider(BouncyCastleSingleton.INSTANCE)
      .getCertificate(
        certificateBuilder.build(
          new JcaContentSignerBuilder(signatureAlg).build(signer)));
  }

  @Test
  void test() throws Exception {
    KeyPairGenerator keyPairGenerator =
      KeyPairGenerator.getInstance("EC", BouncyCastleSingleton.INSTANCE);
    keyPairGenerator.initialize(256, SecureRandom.getInstance("SHA1PRNG"));

    KeyPair ca = keyPairGenerator.generateKeyPair();
    KeyPair mfr = keyPairGenerator.generateKeyPair();
    KeyPair dev = keyPairGenerator.generateKeyPair();

    X509Certificate caCert = buildCertificate(ca.getPublic(), ca.getPrivate());
    X509Certificate mfrCert = buildCertificate(mfr.getPublic(), ca.getPrivate());

    PKCS10CertificationRequestBuilder csrBuilder =
      new JcaPKCS10CertificationRequestBuilder(
        new X500NameBuilder().build(),
        dev.getPublic());
    PKCS10CertificationRequest csr =
      csrBuilder.build(new JcaContentSignerBuilder(signatureAlg)
        .setProvider(BouncyCastleSingleton.INSTANCE)
        .build(dev.getPrivate()));

    KeyStore mfrKeyStore =
      KeyStore.getInstance(KeyStore.getDefaultType(), BouncyCastleSingleton.INSTANCE);
    mfrKeyStore.load(null);
    mfrKeyStore.setKeyEntry("x", mfr.getPrivate(), pw(), new Certificate[] {mfrCert});
    KeyStores mfrKeyStores = () -> List.of(mfrKeyStore).iterator();

    SimpleCertPathService cps = new SimpleCertPathService(
      this::pw,
      () -> Period.ofDays(1),
      new KeyFinder(mfrKeyStores, this::pw));
    CertPath certPath = cps.apply(csr);

    // This generated cert path should verify against the mfr key
    CertPathValidator validator =
      CertPathValidator.getInstance("PKIX", BouncyCastleSingleton.INSTANCE);
    PKIXParameters parameters = new PKIXParameters(Set.of(new TrustAnchor(caCert, null)));
    parameters.setRevocationEnabled(false);
    assertDoesNotThrow(() -> validator.validate(certPath, parameters));
  }
}
