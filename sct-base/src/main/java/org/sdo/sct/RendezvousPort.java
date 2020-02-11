// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.util.Objects;

/**
 * SDO rendezvous instruction variable: po, pow.
 */
abstract class RendezvousPort extends RendezvousVariable {

  private final int port;

  /**
   * Constructor.
   *
   * @param key  The variable name.
   * @param port The port number.
   */
  RendezvousPort(final String key, final int port) {
    super(key);

    this.port = port;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RendezvousPort that = (RendezvousPort) o;
    return super.equals(that) && (this.port == that.port);
  }

  @Override
  public int hashCode() {
    return Objects.hash(port);
  }

  /**
   * {@inheritDoc}
   */
  protected String valueToString() {
    return IntegerCodec.encode(port, 16);
  }
}
