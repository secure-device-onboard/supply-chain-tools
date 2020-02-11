// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.util.NoSuchElementException;

enum KeyEncoding {
  NONE(0),
  X509(1),
  RSAMODEXP(3);

  private final int code;

  KeyEncoding(int code) {
    this.code = code;
  }

  @Override
  public final String toString() {
    return Integer.valueOf(code).toString();
  }

  static KeyEncoding valueOfInt(int code) {
    for (KeyEncoding e : values()) {
      if (e.code == code) {
        return e;
      }
    }

    throw new NoSuchElementException();
  }
}
