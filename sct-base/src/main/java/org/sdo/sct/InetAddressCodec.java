// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.net.InetAddress;

class InetAddressCodec {

  static String encode(final InetAddress address) {
    return "["
      + IntegerCodec.encode(address.getAddress().length, 8)
      + ","
      + ArrayCodec.encode(address.getAddress())
      + "]";
  }
}
