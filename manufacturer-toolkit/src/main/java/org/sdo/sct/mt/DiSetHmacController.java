// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.Callable;
import org.sdo.sct.domain.DeviceState;
import org.sdo.sct.domain.DeviceStateRepo;
import org.sdo.sct.domain.OwnershipVoucherEntry;
import org.sdo.sct.domain.OwnershipVoucherRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@RestController
@SuppressWarnings("unused")
class DiSetHmacController {

  private static final String BREAK = "\03";
  private final DeviceStateRepo deviceStateRepo;
  private final OwnershipVoucherRepo ownerVoucherRepo;

  @Autowired
  DiSetHmacController(
      DeviceStateRepo deviceStateRepo, OwnershipVoucherRepo ownerVoucherRepo) {
    this.deviceStateRepo = deviceStateRepo;
    this.ownerVoucherRepo = ownerVoucherRepo;
  }

  @PostMapping(path = "/mp/113/msg/12",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @SuppressWarnings("unused")
  Callable<DiDone> post(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
      @RequestBody final DiSetHmac diSetHmac) {

    if (null == authToken || authToken.isBlank()) {
      throw HttpClientErrorException.create(
        HttpStatus.UNAUTHORIZED, null, null, null, StandardCharsets.US_ASCII);
    }

    if (null == diSetHmac) {
      throw HttpClientErrorException.create(
        HttpStatus.BAD_REQUEST, null, null, null, StandardCharsets.US_ASCII);
    }

    return () -> post_(authToken, diSetHmac);
  }

  private DiDone post_(final String authToken, final DiSetHmac diSetHmac) {
    final String deviceSerial =
        new String(Base64.getDecoder().decode(authToken), StandardCharsets.UTF_16);

    final DeviceState session = deviceStateRepo.findById(deviceSerial)
        .orElseThrow(() -> HttpClientErrorException.create(
        HttpStatus.UNAUTHORIZED, null, null, null, StandardCharsets.US_ASCII));

    // The voucher has already been generated with a null where hmac should be.
    // Complete the voucher by replacing the null.
    if (null == session.getSessionData()
        || session.getSessionData().isBlank()
        || !session.getSessionData().contains(BREAK)) {

      throw HttpServerErrorException.create(
        HttpStatus.INTERNAL_SERVER_ERROR, null, null, null, StandardCharsets.US_ASCII); // bug
    }

    final String ov = session.getSessionData().replace(BREAK, diSetHmac.getHmac());
    final Timestamp now = Timestamp.from(Instant.now());
    ownerVoucherRepo.save(new OwnershipVoucherEntry(deviceSerial, ov, null));

    session.setStatus(DeviceStatus.OK);
    session.setDiEnd(now);
    deviceStateRepo.save(session);

    return new DiDone();
  }
}
