// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * SDO composite type 'RendezvousInfo'.
 */
public class RendezvousInfo {

  private final List<RendezvousInstruction> instructions;

  /**
   * Constructor.
   *
   * @param instructions the RendezvousInstructions which make up this info.
   */
  public RendezvousInfo(final List<RendezvousInstruction> instructions) {
    this.instructions = Collections.unmodifiableList(instructions);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RendezvousInfo that = (RendezvousInfo) o;
    return instructions.equals(that.instructions);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return Objects.hash(instructions);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append('[');
    builder.append(IntegerCodec.encode(instructions.size(), 8));
    for (RendezvousInstruction instruction : instructions) {
      builder.append(',');
      builder.append(instruction.toString());
    }
    builder.append(']');

    return builder.toString();
  }
}
