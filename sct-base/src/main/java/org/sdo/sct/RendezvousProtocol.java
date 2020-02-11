// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * SDO rendezvous instruction variable: pr.
 */
class RendezvousProtocol extends RendezvousVariable {

  private final Protocol pr;

  /**
   * Constructor.
   *
   * @param pr The 'pr' value.
   */
  RendezvousProtocol(final Protocol pr) {
    super("pr");

    this.pr = pr;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RendezvousProtocol that = (RendezvousProtocol) o;
    return super.equals(o) && (this.pr == that.pr);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pr);
  }

  /**
   * {@inheritDoc}
   */
  protected String valueToString() {
    return "\"" + StringCodec.encode(pr.getName()) + "\"";
  }

  enum Protocol {
    REST("rest"), // unsupported
    TCP("tcp"), // unsupported
    TLS("tls"), // unsupported
    COAP("CoAP/tcp"), // unsupported
    HTTP("http"),
    HTTPS("https");

    private final String name;

    Protocol(String name) {
      this.name = name;
    }

    static Protocol valueOfName(final String name) {
      for (Protocol p : values()) {
        if (p.getName().equals(name)) {
          return p;
        }
      }

      throw new NoSuchElementException();
    }

    String getName() {
      return name;
    }
  }
}
