// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.sdo.sct.CertPathCodec;
import org.sdo.sct.CryptoLevel;
import org.sdo.sct.DigestWrapper;
import org.sdo.sct.Hash;
import org.sdo.sct.KeyFinder;
import org.sdo.sct.KeyHandle;
import org.sdo.sct.RendezvousInfo;
import org.sdo.sct.RendezvousInfoBuilder;
import org.sdo.sct.ResourceBundleHolder;
import org.sdo.sct.VoucherHeader;
import org.sdo.sct.domain.DeviceState;
import org.sdo.sct.domain.DeviceStateRepo;
import org.sdo.sct.domain.ServerSettingsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@SuppressWarnings("unused")
class DiAppStartController {

  private static final String BREAK = "\03";
  private static final String X509 = "X.509";
  private static ResourceBundleHolder resourceBundleHolder_ =
      new ResourceBundleHolder(DiAppStartController.class.getName());
  private final CertPathService certPathService;
  private final OnDieCertPath onDieCertPathService;
  private final OnDieCache onDieCertCache;
  private final OnDieSignatureValidator onDieSignatureValidator;
  private final DeviceStateRepo deviceStateRepo;
  private final ServerSettingsRepo serverSettingsRepo;
  private final KeyFinder keyFinder;

  @Autowired
  DiAppStartController(
      final CertPathService certPathService,
      final OnDieCertPath onDieCertPathService,
      final OnDieCache onDieCertCache,
      final OnDieSignatureValidator onDieSignatureValidator,
      final DeviceStateRepo deviceStateRepo,
      final ServerSettingsRepo serverSettingsRepo,
      final KeyFinder keyFinder) {

    this.certPathService = certPathService;
    this.onDieCertPathService = onDieCertPathService;
    this.onDieCertCache = onDieCertCache;
    this.onDieSignatureValidator = onDieSignatureValidator;
    this.deviceStateRepo = deviceStateRepo;
    this.serverSettingsRepo = serverSettingsRepo;
    this.keyFinder = keyFinder;
  }

  private static String decodeBase64ToAsciiString(final String b64) {
    final Charset ascii = StandardCharsets.US_ASCII;
    return ascii.decode(ByteBuffer.wrap(Base64.getDecoder().decode(b64))).toString();
  }

  @PostMapping(path = "/mp/113/msg/10",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @SuppressWarnings("unused")
  Callable<ResponseEntity<DiSetCredentials>> post(@RequestBody final DiAppStart diAppStart) {

    if (null == diAppStart) {
      throw new IllegalArgumentException();
    }

    return () -> post_(diAppStart);
  }

  private RendezvousInfo parseRendezvousInfo(final String config) {

    final List<URI> uris = Arrays.stream(config.split("\\s+"))
        .map(URI::create)
        .collect(Collectors.toUnmodifiableList());

    return new RendezvousInfoBuilder().with(uris).build();
  }

  private ResponseEntity<DiSetCredentials> post_(final DiAppStart diAppStart) throws Exception {

    final MStringParser.ParseResult parsedM;
    final DeviceState deviceState;
    try {
      parsedM = new MStringParser().parse(diAppStart.getMstring());

      deviceState = new DeviceState(
        parsedM.getSerialNumber(),
        Timestamp.from(Instant.now()),
        null,
        null,
        DeviceStatus.PENDING,
        diAppStart.getMstring());

    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    try {
      return post_(parsedM, deviceState);

    } catch (Exception e) {
      deviceState.setDiEnd(Timestamp.from(Instant.now()));
      deviceState.setDetails(e.getMessage());
      deviceState.setStatus(DeviceStatus.FAILED);
      throw e;

    } finally {
      deviceStateRepo.save(deviceState);
    }
  }

  private ResponseEntity<DiSetCredentials>
      post_(final MStringParser.ParseResult m, final DeviceState deviceState) throws Exception {

    // Find the manufacturer certificate matching the device's requirements
    KeyHandle myKey = keyFinder.find(m.getKeyTypeConstraint())
        .orElseThrow(() -> {
          String format = resourceBundleHolder_.get().getString("key.not.found");
          return new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, MessageFormat.format(format, m.getKeyTypeConstraint()));
        });
    final Certificate myCert = myKey.getCertificate();

    final CryptoLevel cryptoLevel = CryptoLevel.of(m.getKeyTypeConstraint());
    final DigestWrapper digest = cryptoLevel.buildDigestWrapper();

    final String dc;
    final Hash hdc;
    if (null != m.getCsr()) {
      final CertPath certPath = certPathService.apply(m.getCsr());
      dc = CertPathCodec.encode(certPath);

      digest.reset();
      digest.update(dc.getBytes(StandardCharsets.US_ASCII));
      hdc = digest.doFinal();

    } else if (null != m.getOnDieCertChain()) {
      // build dc for DAL ECDSA device
      final CertPath certPath =
          onDieCertPathService.buildCertPath(m.getOnDieCertChain(), this.onDieCertCache);

      // validate test signature against certpath
      if (!onDieSignatureValidator.validate(
          certPath,
          m.getSerialNumber().getBytes(),
          Base64.getDecoder().decode(m.getOnDieTestSignature()))) {
        throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "OnDie test signature failure.");
      }
      dc = CertPathCodec.encode(certPath);

      digest.reset();
      digest.update(dc.getBytes(StandardCharsets.US_ASCII));
      hdc = digest.doFinal();
    } else {
      dc = null;
      hdc = null;
    }

    final PublicKey pk = myCert.getPublicKey();
    final UUID g = UUID.randomUUID();
    final VoucherHeader oh = new VoucherHeader(
        parseRendezvousInfo(serverSettingsRepo.get().getRendezvousInfo()),
        g,
        m.getDeviceInfo(),
        pk,
        hdc);

    // To complete the ownership voucher, hmac is needed from DI.SetHMAC.
    // However, no more work is needed on anything else.  Avoid re-encoding
    // the parts we have by assembling everything now and saving it for later.
    final StringBuilder ovBuilder = new StringBuilder();
    ovBuilder
      .append("{\"sz\":0,\"oh\":")
      .append(oh.toString())
      .append(",\"hmac\":")
      .append(BREAK); // we'll replace the control character when we get HMAC
    if (!(null == dc || dc.isBlank())) {
      ovBuilder.append(",\"dc\":").append(dc);
    }
    ovBuilder.append(",\"en\":[]}");
    deviceState.setSessionData(ovBuilder.toString());

    DiSetCredentials diSetCredentials = new DiSetCredentials(oh);
    final String auth = Base64.getEncoder()
        .withoutPadding()
        .encodeToString(m.getSerialNumber().getBytes(StandardCharsets.UTF_16));
    return ResponseEntity.ok()
      .header(HttpHeaders.AUTHORIZATION, auth)
      .body(diSetCredentials);
  }
}
