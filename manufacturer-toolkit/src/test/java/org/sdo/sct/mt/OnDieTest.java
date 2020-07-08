// Copyright 2020 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import org.junit.Before;
import org.junit.Rule;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;
import org.sdo.sct.BouncyCastleSingleton;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.cert.CertPath;
import java.security.cert.CertificateFactory;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class OnDieTest {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private String cacheDirForTesting = "cachedir";
  private OnDieCache productionOnDieCache = null;
  private CertificateFactory certFactory = null;

  @Before
  void initCertFactory() throws Exception {
    certFactory = CertificateFactory.getInstance("X509", BouncyCastleSingleton.INSTANCE);
  }

  MStringParser.ParseResult getParsedMstring() throws IOException {
    final InputStream stream =
      getClass().getClassLoader().getResourceAsStream("mstring.dat");
    byte[] mstringBytes = stream.readAllBytes();
    return new MStringParser().parse(new String(mstringBytes));
  }

  byte[] getTestArtifact(String artifactName) throws Exception {
    final InputStream stream =
      getClass().getClassLoader().getResourceAsStream(artifactName);
    return stream.readAllBytes();
  }

  OnDieCache getProductionOnDieCache() throws Exception {
    if (productionOnDieCache != null) {
      return productionOnDieCache;
    }
    tempFolder.create();

    productionOnDieCache = new OnDieCache(
      tempFolder.getRoot().getAbsolutePath(),
      true,
      "https://tsde.intel.com/content/OnDieCA/certs/,https://tsci.intel.com/content/OnDieCA/crls/");

    return productionOnDieCache;
  }


  @Test
  @DisplayName("OnDie mstring parse test")
  void testOnDieMstringParse() throws Exception {
    MStringParser.ParseResult parseResult =
      assertDoesNotThrow(() -> getParsedMstring());
  }

  @Test
  @DisplayName("OnDie cache download test")
  void testOnDieCacheDownload() throws Exception {

    tempFolder.create();
    try {
      OnDieCache onDieCache = getProductionOnDieCache();

      assertNotNull(onDieCache.getCertOrCrl(
        "https://tsde.intel.com/content/OnDieCA/=/certs/TGL_00001846_OnDie_CA.crl"));
      assertNull(onDieCache.getCertOrCrl(
        "https://tsde.intel.com/content/OnDieCA/certs/NOT_IN_THE_CACHE.crl"));
      assertThrows(MalformedURLException.class,
        () -> onDieCache.getCertOrCrl("TGL_00001846_OnDie_CA.crl"));
      assertTrue(onDieCache.getNumOfCerts() > 0);
      assertTrue(onDieCache.getNumOfCrls() > 0);
    } catch (Exception ex) {
      throw ex;
    }
  }

  @Test
  @DisplayName("OnDie cache load test")
  void testOnDieCacheLoad() throws Exception {
    OnDieCache onDieCache = new OnDieCache(
      new ClassPathResource(cacheDirForTesting).getURL().getPath(),
      false,
      null );

    assertNotNull(onDieCache.getCertOrCrl(
      "https://tsde.intel.com/content/OnDieCA/certs/TGL_00001846_OnDie_CA.crl"));
    assertThrows(MalformedURLException.class,
      () -> onDieCache.getCertOrCrl("TGL_00001846_OnDie_CA.crl"));
    assertTrue(onDieCache.getNumOfCerts() > 0);
    assertTrue(onDieCache.getNumOfCrls() > 0);
  }

  @Test
  @DisplayName("OnDie cert path test")
  void testCertPath() throws Exception {

    OnDieCache onDieCache = new OnDieCache(
      new ClassPathResource(cacheDirForTesting).getURL().getPath(),
      false,
      null );

    OnDieCertPath onDieCertPath = new OnDieCertPath();

    // invalid mstring
    String b64Mstring = "";
    assertThrows(Exception.class,
      () -> onDieCertPath.buildCertPath(b64Mstring, onDieCache));

    // valid mstring
    MStringParser.ParseResult parsedM = getParsedMstring();
    String serialNo = parsedM.getSerialNumber();
    String deviceInfo = parsedM.getDeviceInfo();
    CertPath certpath =
      assertDoesNotThrow(() -> onDieCertPath.buildCertPath(parsedM.getOnDieCertChain(), onDieCache));

  }

  @Test
  @DisplayName("OnDie signature test")
  void testOnDieSignatureInvalidSignedData() throws Exception {

    OnDieCache onDieCache = new OnDieCache(
      new ClassPathResource(cacheDirForTesting).getURL().getPath(),
      false,
      null );

    // first, get a cert path from a valid mstring
    MStringParser.ParseResult parsedM = getParsedMstring();
    OnDieCertPath onDieCertPath = new OnDieCertPath();
    CertPath certpath =
      assertDoesNotThrow(() -> onDieCertPath.buildCertPath(parsedM.getOnDieCertChain(), onDieCache));

    OnDieSignatureValidator onDieSignatureValidator = new OnDieSignatureValidator(onDieCache, false);

    // modify the signed data and verify signature fails
    assertFalse(onDieSignatureValidator.validate(
      certpath,
      (parsedM.getSerialNumber() + "extra data").getBytes(),
      Base64.getDecoder().decode(parsedM.getOnDieTestSignature())));
  }

  @Test
  @DisplayName("OnDie signature test")
  void testOnDieSignatureInvalidSignatureData() throws Exception {

    OnDieCache onDieCache = new OnDieCache(
      new ClassPathResource(cacheDirForTesting).getURL().getPath(),
      false,
      null );

    // first, get a cert path from a valid mstring
    MStringParser.ParseResult parsedM = getParsedMstring();
    OnDieCertPath onDieCertPath = new OnDieCertPath();
    CertPath certpath =
      assertDoesNotThrow(() -> onDieCertPath.buildCertPath(parsedM.getOnDieCertChain(), onDieCache));

    OnDieSignatureValidator onDieSignatureValidator = new OnDieSignatureValidator(onDieCache, false);

    // modify the signature data and verify signature fails
    assertFalse(onDieSignatureValidator.validate(
      certpath,
      parsedM.getSerialNumber().getBytes(),
      Base64.getDecoder().decode(parsedM.getOnDieTestSignature().substring(1))));
  }

  @Test
  @DisplayName("OnDie signature test")
  void testOnDieSignature() throws Exception {

    OnDieCache onDieCache = new OnDieCache(
      new ClassPathResource(cacheDirForTesting).getURL().getPath(),
      false,
      null );

    // first, get a cert path from a valid mstring
    MStringParser.ParseResult parsedM = getParsedMstring();
    OnDieCertPath onDieCertPath = new OnDieCertPath();
    CertPath certpath =
      assertDoesNotThrow(() -> onDieCertPath.buildCertPath(parsedM.getOnDieCertChain(), onDieCache));

    OnDieSignatureValidator onDieSignatureValidator = new OnDieSignatureValidator(onDieCache, false);

    assertTrue(onDieSignatureValidator.validate(
      certpath,
      parsedM.getSerialNumber().getBytes(),
      Base64.getDecoder().decode(parsedM.getOnDieTestSignature())));
  }

  @Test
  @DisplayName("OnDie signature 1 test")
  @Disabled
  void testSignature_1() throws Exception {
    OnDieCache onDieCache = getProductionOnDieCache();

    byte[] sig = getTestArtifact("test_sig_1.dat");
    byte[] b64cert = getTestArtifact("test_cert_1.dat");

    OnDieCertPath onDieCertPath = new OnDieCertPath();
    CertPath certPath = onDieCertPath.buildCertPath(new String(b64cert), onDieCache);

    OnDieSignatureValidator onDieSignatureValidator = new OnDieSignatureValidator(onDieCache, true);

    assertTrue(onDieSignatureValidator.validate(
      certPath,
      "this is a test".getBytes(),
      sig));
  }

  @Test
  @DisplayName("OnDie signature 2 test")
  @Disabled
  void testSignature_2() throws Exception {
    OnDieCache onDieCache = getProductionOnDieCache();

    byte[] sig = getTestArtifact("test_sig_2.dat");
    byte[] b64cert = getTestArtifact("test_cert_2.dat");

    OnDieCertPath onDieCertPath = new OnDieCertPath();
    CertPath certPath = onDieCertPath.buildCertPath(new String(b64cert), onDieCache);

    OnDieSignatureValidator onDieSignatureValidator = new OnDieSignatureValidator(onDieCache, true);

    assertTrue(onDieSignatureValidator.validate(
      certPath,
      "dalTest".getBytes(),
      Base64.getDecoder().decode(sig)));
  }

}
