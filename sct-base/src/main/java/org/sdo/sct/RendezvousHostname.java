// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.util.Objects;

/**
 * SDO rendezvous instruction variable: dn.
 */
class RendezvousHostname extends RendezvousVariable {

  private final String hostname;

  /**
   * Constructor.
   *
   * @param hostname The hostname.
   */
  RendezvousHostname(String hostname) {
    super("dn");

    this.hostname = hostname;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    RendezvousHostname hostname = (RendezvousHostname) o;
    return Objects.equals(this.hostname, hostname.hostname);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), hostname);
  }

  /**
   * {@inheritDoc}
   */
  protected String valueToString() {
    return "\"" + StringCodec.encode(hostname) + "\"";
  }
}
