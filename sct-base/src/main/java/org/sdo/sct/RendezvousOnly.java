// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.util.Objects;

/**
 * SDO rendezvous instruction variable: only.
 */
class RendezvousOnly extends RendezvousVariable {

  static final String KEY = "only";
  private final Only only;

  /**
   * Constructor.
   *
   * @param only The 'only' value.
   */
  RendezvousOnly(final Only only) {
    super(KEY);

    this.only = only;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RendezvousOnly that = (RendezvousOnly) o;
    return super.equals(o) && this.only == that.only;
  }

  @Override
  public int hashCode() {
    return Objects.hash(only);
  }

  /**
   * {@inheritDoc}
   */
  protected String valueToString() {
    return "\"" + StringCodec.encode(only.toString()) + "\"";
  }

  public enum Only {
    dev,
    owner
  }
}
