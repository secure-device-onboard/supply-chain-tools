// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0
package org.sdo.sct.mt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.sdo.sct.BouncyCastleSingleton;
import org.sdo.sct.CryptoLevel;
import org.sdo.sct.KeyFinder;
import org.sdo.sct.KeyStores;
import org.sdo.sct.KeyStoresFactory;
import org.sdo.sct.KeyType;
import org.sdo.sct.KeyTypeCodec;
import org.sdo.sct.PasswordCallbackFunction;
import org.sdo.sct.domain.DeviceState;
import org.sdo.sct.domain.DeviceStateRepo;
import org.sdo.sct.domain.OwnershipVoucherEntry;
import org.sdo.sct.domain.OwnershipVoucherRepo;
import org.sdo.sct.domain.ServerSettings;
import org.sdo.sct.domain.ServerSettingsRepo;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.Period;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest(showSql = false)
@Transactional
class DITest {

  @Autowired
  private
  DeviceStateRepo deviceStateRepo;
  @Autowired
  private
  OwnershipVoucherRepo ownerVoucherRepo;
  @Autowired
  private
  ServerSettingsRepo serverSettingsRepo;
  @Autowired
  private
  DiAppStartController diAppStartController;

  static Stream<Arguments> paramProvider() {
    return Stream.of(
      Arguments.arguments(KeyType.RSA2048RESTR, false),
      Arguments.arguments(KeyType.RSA2048RESTR, true),
      Arguments.arguments(KeyType.ECDSA_P_256, false),
      Arguments.arguments(KeyType.ECDSA_P_256, true),
      Arguments.arguments(KeyType.ECDSA_P_384, false),
      Arguments.arguments(KeyType.ECDSA_P_384, true));
  }

  @BeforeEach
  void beforeEach() {

    serverSettingsRepo.save(
      new ServerSettings(
        null,
        "http://localhost:8040 https://127.0.0.1:8040",
        Period.ofDays(1).toString()));
  }

  @ParameterizedTest
  @MethodSource("paramProvider")
  void test(KeyType keyType, boolean useCsr) throws Exception {

    PKCS10CertificationRequest csr = null;
    if (useCsr) {
      KeyPairGenerator kpg = KeyPairGenerator.getInstance(
        keyType.getJceAlgorithm(), BouncyCastleSingleton.INSTANCE);
      kpg.initialize(toSize(keyType), SecureRandom.getInstance("SHA1PRNG"));
      KeyPair keyPair = kpg.generateKeyPair();
      PKCS10CertificationRequestBuilder csrBuilder = new JcaPKCS10CertificationRequestBuilder(
        new X500NameBuilder().build(), keyPair.getPublic());
      final String signingAlg = CryptoLevel.of(keyType).getSignatureAlgorithm(keyPair.getPublic());
      csr = csrBuilder.build(new JcaContentSignerBuilder(signingAlg).build(keyPair.getPrivate()));
    }

    final String serial = UUID.randomUUID().toString();
    final String d = UUID.randomUUID().toString();
    String m = KeyTypeCodec.encode(keyType)
      + "\00" + serial
      + "\00" + d;
    if (null != csr) {
      m += "\00" + toPEM(csr);
    }

    ResponseEntity<DiSetCredentials> response = diAppStartController
      .post(new DiAppStart(Base64.getEncoder().encodeToString(m.getBytes())))
      .call();

    // Did the call succeed?
    assertNotNull(response);
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());

    // Does the HTTP response have an authorization: header?
    assertNotNull(response.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));

    // Is pe/pk consistent?
    assertNotNull(response.getBody().oh().getPublicKey());

    // Is the device state table properly updated?
    Optional<DeviceState> opt = deviceStateRepo.findById(serial);
    assertTrue(opt.isPresent());
    DeviceState deviceState = opt.get();

    assertEquals(serial, deviceState.getDeviceSerialNo());

    assertNotNull(deviceState.getDiStart());
    assertTrue(Instant.now().isAfter(deviceState.getDiStart().toInstant()));

    assertNull(deviceState.getDiEnd());

    assertEquals(DeviceStatus.PENDING, deviceState.getStatus().intValue());

    final String auth = Base64.getEncoder()
      .withoutPadding()
      .encodeToString(serial.getBytes(StandardCharsets.UTF_16));
    Callable<DiDone> callable =
      new DiSetHmacController(deviceStateRepo, ownerVoucherRepo)
        .post(auth, new DiSetHmac("[32,108,\"1IOGFrfUn1dIRRSxuMbXgCfpdagOvB90dIla9Ki+aN8=\"]"));

    assertNotNull(callable);
    assertNotNull(callable.call());

    OwnershipVoucherEntry ove = ownerVoucherRepo.findById(serial).orElseThrow();
    assertNotNull(ove.getVoucher());
    assertFalse(ove.getVoucher().isBlank());
    assertTrue(ove.getVoucher().startsWith("{\"sz\":0,\"oh\":{\"pv\":113,\"pe\":"));

    // Is the device state table properly updated?
    opt = deviceStateRepo.findById(serial);
    assertTrue(opt.isPresent());
    deviceState = opt.get();

    assertEquals(serial, deviceState.getDeviceSerialNo());

    assertNotNull(deviceState.getDiStart());
    assertFalse(Instant.now().isBefore(deviceState.getDiStart().toInstant()));

    assertNotNull(deviceState.getDiEnd());
    assertFalse(Instant.now().isBefore(deviceState.getDiEnd().toInstant()));
    assertFalse(deviceState.getDiEnd().toInstant().isBefore(deviceState.getDiStart().toInstant()));

    assertEquals(DeviceStatus.OK, deviceState.getStatus().intValue());
  }

  private String toPEM(PKCS10CertificationRequest csr) throws IOException {
    StringWriter sw = new StringWriter();
    try (JcaPEMWriter pw = new JcaPEMWriter(sw)) {
      pw.writeObject(csr);
    }
    return sw.toString();
  }

  private int toSize(final KeyType keyType) {
    switch (keyType) {
      case RSA2048RESTR:
        return 2048;
      case ECDSA_P_256:
        return 256;
      case ECDSA_P_384:
        return 384;
      default:
        throw new RuntimeException();
    }
  }

  @Configuration
  @EnableJpaRepositories("org.sdo.sct.domain")
  @EntityScan("org.sdo.sct.domain")
  static class Config {

    @Autowired
    DeviceStateRepo deviceStateRepo;

    @Autowired
    ServerSettingsRepo serverSettingsRepo;

    @Bean
    CertPathService certPathService() throws Exception {
      return new SimpleCertPathService(
        passwordCallbackFunction(),
        certificateValidityPeriodFactory(),
        new KeyFinder(keyStores(), passwordCallbackFunction()));
    }

    @Bean
    OnDieCache onDieCertCache() throws Exception {
      return new OnDieCache(null, false, null);
    }

    @Bean
    OnDieCertPath onDieCertPathService() throws Exception {
      return new OnDieCertPath();
    }

    @Bean
    OnDieSignatureValidator onDieSignatureValidator() throws Exception {
      return new OnDieSignatureValidator(onDieCertCache());
    }

    @Bean
    CertificateValidityPeriodFactory certificateValidityPeriodFactory() {
      return () -> Period.ofDays(1);
    }

    @Bean
    DiAppStartController diAppStartController() throws Exception {
      return new DiAppStartController(
        certPathService(),
        onDieCertPathService(),
        onDieCertCache(),
        onDieSignatureValidator(),
        deviceStateRepo,
        serverSettingsRepo,
        new KeyFinder(keyStores(), passwordCallbackFunction()));
    }

    @Bean
    KeyStores keyStores() throws Exception {
      KeyStoresFactory ksf = new KeyStoresFactory(
        passwordCallbackFunction(), new ClassPathResource("org/sdo/sct/test.p12"));
      ksf.setSingleton(false);
      return ksf.getObject();
    }

    @Bean
    PasswordCallbackFunction passwordCallbackFunction() {
      return "123456"::toCharArray;
    }
  }
}
