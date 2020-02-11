// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import org.sdo.sct.VoucherHeader;

class DiSetCredentials {

  private final VoucherHeader oh;

  DiSetCredentials(final VoucherHeader oh) {
    this.oh = oh;
  }

  @Override
  public String toString() {
    return "{\"oh\":"
      + oh.toString()
      + "}";
  }

  VoucherHeader oh() {
    return oh;
  }
}
