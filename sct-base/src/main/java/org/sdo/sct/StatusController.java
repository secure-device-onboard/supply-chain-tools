// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SuppressWarnings("unused")
class StatusController {

  @Autowired
  DataSource dataSource;

  @GetMapping(path = "/api/v1/status", produces = MediaType.APPLICATION_JSON_VALUE)
  Callable<String> get() {

    return this::get_;
  }

  private String get_() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("{");

    try (Connection connection = dataSource.getConnection()) {
      DatabaseMetaData metaData = connection.getMetaData();

      stringBuilder
        .append("\"connection.databaseProductName\":\"")
        .append(metaData.getDatabaseProductName())
        .append("\"");

      stringBuilder
        .append(",\"connection.databaseProductVersion\":\"")
        .append(metaData.getDatabaseProductVersion())
        .append("\"");

      stringBuilder
        .append(",\"connection.url\":\"")
        .append(metaData.getURL())
        .append("\"");

      stringBuilder
        .append(",\"connection.user\":\"")
        .append(metaData.getUserName())
        .append("\"");

    } catch (SQLException e) {
      stringBuilder
        .append("\"connection.error\":\"")
        .append(e.getMessage())
        .append("\"");
    }

    stringBuilder.append("}");
    return stringBuilder.toString();
  }
}
