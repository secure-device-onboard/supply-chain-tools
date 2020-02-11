// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

/**
 * SDO rendezvous instruction variable: po.
 */
class RendezvousPortDevice extends RendezvousPort {

  RendezvousPortDevice(final int port) {
    super("po", port);
  }
}
