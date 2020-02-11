// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.util.Objects;

abstract class RendezvousVariable implements Comparable<RendezvousVariable> {

  private final String key;

  RendezvousVariable(final String key) {
    this.key = key;
  }

  @Override
  public int compareTo(RendezvousVariable that) {
    return key.compareTo(that.key);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RendezvousVariable variable = (RendezvousVariable) o;
    return Objects.equals(key, variable.key);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key);
  }

  @Override
  public String toString() {
    return "\""
      + key
      + "\":"
      + valueToString();
  }

  protected abstract String valueToString();
}
