// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.sdo.sct.domain.DeviceState;
import org.sdo.sct.domain.DeviceStateRepo;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.HttpClientErrorException.BadRequest;
import org.springframework.web.client.HttpClientErrorException.Unauthorized;

@DataJpaTest(showSql = false)
@ContextConfiguration
class ErrorControllerTest {

  @Configuration
  @EnableJpaRepositories("org.sdo.sct.domain")
  @EntityScan("org.sdo.sct.domain")
  static class Config {

  }

  private final String serial = "809046fb-c326-4de8-907e-cece52774ef3";
  private final String auth = Base64.getEncoder()
    .withoutPadding()
    .encodeToString(serial.getBytes(StandardCharsets.UTF_16));

  @Autowired
  private DeviceStateRepo deviceStateRepo;

  @BeforeEach
  void beforeEach() {
    DeviceState state = new DeviceState(
      serial, Timestamp.from(Instant.now()), null, null, DeviceStatus.PENDING, null);
    deviceStateRepo.save(state);
  }

  @Test
  void post_badAuth_throwsUnauthorizedException() {
    assertThrows(Unauthorized.class, () ->
      new ErrorController(deviceStateRepo).post(null, new Error(0, 0, "hello world")).call());
    assertThrows(Unauthorized.class, () ->
      new ErrorController(deviceStateRepo).post("", new Error(0, 0, "hello world")).call());
    assertThrows(Unauthorized.class, () ->
      new ErrorController(deviceStateRepo).post(auth + "x", new Error(0, 0, "hello world")).call());
    assertThrows(Unauthorized.class, () ->
      new ErrorController(deviceStateRepo).post(auth + "\03", new Error(0, 0, "hello world"))
        .call());
  }

  @Test
  void post_badBody_throwsBadRequestException() {
    assertThrows(BadRequest.class, () ->
      new ErrorController(deviceStateRepo).post(auth, null).call());
  }

  @Test
  void post_recordsDeviceState() {
    assertDoesNotThrow(() ->
      new ErrorController(deviceStateRepo).post(auth, new Error(0, 0, "hello world")).call());

    DeviceState entity =
      deviceStateRepo.findById(serial).orElseThrow(NoSuchElementException::new);

    assertTrue(0 > entity.getStatus());
    assertNotNull(entity.getDiEnd());
    assertFalse(entity.getDiEnd().before(entity.getDiStart()));
    assertNotNull(entity.getDetails());
  }

  @Test
  void post_validInput_ok() {
    assertDoesNotThrow(() ->
      new ErrorController(deviceStateRepo).post(auth, new Error(0, 0, "hello world")).call());
  }
}
