// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.net.InetAddress;
import java.util.Objects;

/**
 * SDO rendezvous instruction variable: ip.
 */
class RendezvousIpAddress extends RendezvousVariable {

  private final InetAddress address;

  /**
   * Constructor.
   *
   * @param address The network address.
   */
  RendezvousIpAddress(final InetAddress address) {
    super("ip");

    this.address = address;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RendezvousIpAddress ipAddress = (RendezvousIpAddress) o;
    return super.equals(o) && Objects.equals(address, ipAddress.address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(address);
  }

  /**
   * {@inheritDoc}
   */
  protected String valueToString() {
    return InetAddressCodec.encode(address);
  }
}
