// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SuppressWarnings("unused")
class VersionController {

  @GetMapping(path = "/api/version", produces = MediaType.APPLICATION_JSON_VALUE)
  String get() {

    final String unknown = "unknown";

    final String build;
    final String version;
    final String timestamp;

    final Properties properties = new Properties();
    try (final InputStream stream =
        getClass().getClassLoader().getResourceAsStream("version.properties")) {

      properties.load(stream);

    } catch (IOException ignored) {
      // ignored
    }

    build = properties.getProperty("app.build", unknown);
    version = properties.getProperty("app.version", unknown);
    timestamp = properties.getProperty("app.timestamp", unknown);

    return "{\"build\":\"" + build + "\","
      + "\"version\":\"" + version + "\","
      + "\"timestamp\":\"" + timestamp + "\"}";
  }
}
