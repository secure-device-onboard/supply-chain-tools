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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@SuppressWarnings("unused")
class ErrorController {

  private final DeviceStateRepo deviceStateRepo;

  @Autowired
  ErrorController(DeviceStateRepo deviceStateRepo) {
    this.deviceStateRepo = deviceStateRepo;
  }

  @PostMapping(path = "/mp/113/msg/255",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @SuppressWarnings("unused")
  Callable<Void> post(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
      @RequestBody final Error error) {

    if (null == authToken || authToken.isBlank()) {
      throw HttpClientErrorException.create(
        HttpStatus.UNAUTHORIZED, null, null, null, StandardCharsets.US_ASCII);
    }

    final String deviceSerial;
    try {
      deviceSerial = new String(Base64.getDecoder().decode(authToken), StandardCharsets.UTF_16);
    } catch (IllegalArgumentException e) {
      throw HttpClientErrorException.create(
        HttpStatus.UNAUTHORIZED, null, null, null, StandardCharsets.US_ASCII);
    }

    if (null == error) {
      throw HttpClientErrorException.create(
        HttpStatus.BAD_REQUEST, null, null, null, StandardCharsets.US_ASCII);
    }

    return () -> {
      post_(deviceSerial, error);
      return null;
    };
  }

  void post_(final String deviceSerial, final Error error) {

    final DeviceState session = deviceStateRepo.findById(deviceSerial)
        .orElseThrow(() -> HttpClientErrorException.create(
        HttpStatus.UNAUTHORIZED, null, null, null, StandardCharsets.US_ASCII));

    session.setStatus(DeviceStatus.FAILED);
    session.setDiEnd(Timestamp.from(Instant.now()));
    session.setDetails(error.getEm());
    deviceStateRepo.save(session);
  }
}
