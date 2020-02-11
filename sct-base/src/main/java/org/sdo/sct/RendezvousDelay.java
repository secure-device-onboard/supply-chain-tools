// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.time.Duration;
import java.util.Objects;

/**
 * SDO rendezvous instruction variable: delaysec.
 */
class RendezvousDelay extends RendezvousVariable {

  static final String KEY = "delaysec";

  private final Duration delay;

  /**
   * Constructor.
   *
   * @param delay the delay duration.
   */
  RendezvousDelay(final Duration delay) {
    super(KEY);

    this.delay = delay;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RendezvousDelay that = (RendezvousDelay) o;
    return super.equals(o) && Objects.equals(this.delay, that.delay);
  }

  @Override
  public int hashCode() {
    return Objects.hash(delay);
  }

  /**
   * {@inheritDoc}
   */
  protected String valueToString() {
    return IntegerCodec.encode(delay.getSeconds(), 32);
  }
}
