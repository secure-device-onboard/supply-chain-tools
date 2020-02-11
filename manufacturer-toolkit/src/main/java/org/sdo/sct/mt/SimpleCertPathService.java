// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyStore.Entry;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import javax.security.auth.DestroyFailedException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.sdo.sct.BouncyCastleSingleton;
import org.sdo.sct.CryptoLevel;
import org.sdo.sct.KeyFinder;
import org.sdo.sct.KeyHandle;
import org.sdo.sct.KeyType;
import org.sdo.sct.KeyUtils;
import org.sdo.sct.PasswordCallbackFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SimpleCertPathService implements CertPathService {

  private final CertificateValidityPeriodFactory certificateValidityPeriodFactory;
  private final PasswordCallbackFunction passwordCallbackFn;
  private final KeyFinder keyFinder;

  /**
   * Constuctor.
   *
   * @param passwordCallbackFn passwordCallbackFn
   * @param certificateValidityPeriodFactory certificateValidityPeriodFactory
   * @param keyFinder keyFinder
   */
  @Autowired
  public SimpleCertPathService(
      final PasswordCallbackFunction passwordCallbackFn,
      final CertificateValidityPeriodFactory certificateValidityPeriodFactory,
      final KeyFinder keyFinder) {

    this.passwordCallbackFn = passwordCallbackFn;
    this.certificateValidityPeriodFactory = certificateValidityPeriodFactory;
    this.keyFinder = keyFinder;
  }

  @Override
  public CertPath apply(PKCS10CertificationRequest csr) throws Exception {

    // Find a signing key of the same type as the device's.
    final KeyFactory keyFactory = KeyFactory.getInstance(
        csr.getSubjectPublicKeyInfo().getAlgorithm().getAlgorithm().toString(),
        BouncyCastleSingleton.INSTANCE);
    final X509EncodedKeySpec subjectKeySpec =
        new X509EncodedKeySpec(csr.getSubjectPublicKeyInfo().getEncoded());
    final PublicKey subjectPublicKey = keyFactory.generatePublic(subjectKeySpec);
    final KeyType keyType = KeyUtils.toType(subjectPublicKey);
    final KeyHandle keyHandle = keyFinder.find(keyType).orElseThrow(
        () -> new NoSuchElementException("KeyType " + keyType + " not found in keystore"));

    // KeyStoreSpi does not support callbacks for getEntry,
    // we must fetch the password ourselves
    final Entry entry;
    final PasswordProtection pp = new PasswordProtection(
        passwordCallbackFn.getPassword());
    try {
      entry = keyHandle.getKeyStore().getEntry(keyHandle.getAlias(), pp);
    } finally {
      pp.destroy();
    }

    if (!(entry instanceof PrivateKeyEntry)) {
      throw new IllegalArgumentException("keystore entry must contain a private key");
    }

    final PrivateKeyEntry pkEntry = (PrivateKeyEntry) entry;

    final X509CertificateHolder certHolder;
    final PrivateKey issuerKey = pkEntry.getPrivateKey();
    try {
      final ContentSigner signer;
      try {
        signer =
          new JcaContentSignerBuilder(CryptoLevel.of(keyType).getSignatureAlgorithm(issuerKey))
            .setProvider(keyHandle.getKeyStore().getProvider())
            .build(issuerKey);
      } catch (OperatorCreationException e) {
        throw new RuntimeException(e);
      }

      final X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(
          new X509CertificateHolder(pkEntry.getCertificate().getEncoded()).getSubject(),
          BigInteger.valueOf(System.currentTimeMillis()),
          Date.from(Instant.now()),
          Date.from(ZonedDateTime.now().plus(certificateValidityPeriodFactory.get()).toInstant()),
          csr.getSubject(),
          csr.getSubjectPublicKeyInfo());

      certHolder = certBuilder.build(signer);

    } finally {
      try {
        issuerKey.destroy();
      } catch (DestroyFailedException e) {
        // no-op: most key implementations don't do this right, but we must try
      }
    } // try (issuer key)

    final List<Certificate> devCertChain = new ArrayList<>();
    devCertChain.add(new JcaX509CertificateConverter()
        .setProvider(BouncyCastleSingleton.INSTANCE)
        .getCertificate(certHolder));
    devCertChain.addAll(Arrays.asList(pkEntry.getCertificateChain()));

    CertificateFactory certFactory =
        CertificateFactory.getInstance("X.509", BouncyCastleSingleton.INSTANCE);
    return certFactory.generateCertPath(devCertChain);
  }
}

