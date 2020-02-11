// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class NoSuchKeyException extends ResponseStatusException {

  public NoSuchKeyException(KeyType type) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, buildReasonString(type));
  }

  private static String buildReasonString(KeyType type) {

    String format = ResourceBundle
        .getBundle(NoSuchKeyException.class.getName(), Locale.getDefault())
        .getString("no.such.key");
    return MessageFormat.format(format, type);
  }
}
