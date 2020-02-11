// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.util.Locale;
import java.util.ResourceBundle;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class SignerNotOwnerException extends ResponseStatusException {

  public SignerNotOwnerException() {
    super(HttpStatus.INTERNAL_SERVER_ERROR, buildReasonString());
  }

  private static String buildReasonString() {

    return ResourceBundle
      .getBundle(SignerNotOwnerException.class.getName(), Locale.getDefault())
      .getString("signer.not.owner");
  }
}
