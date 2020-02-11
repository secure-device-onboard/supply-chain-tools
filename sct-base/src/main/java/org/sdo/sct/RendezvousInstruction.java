// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

class RendezvousInstruction {

  private final List<RendezvousVariable> variables;

  public RendezvousInstruction(List<RendezvousVariable> variables) {
    this.variables = Collections.unmodifiableList(variables);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RendezvousInstruction that = (RendezvousInstruction) o;
    return variables.equals(that.variables);
  }

  @Override
  public int hashCode() {
    return Objects.hash(variables);
  }

  @Override
  public String toString() {

    StringBuilder builder = new StringBuilder();
    builder.append('[');
    builder.append(IntegerCodec.encode(variables.size(), 8));
    builder.append(",{");
    String separator = "";
    for (RendezvousVariable variable : variables) {
      builder.append(separator);
      separator = ",";
      builder.append(variable.toString());
    }
    builder.append("}]");
    return builder.toString();
  }
}
