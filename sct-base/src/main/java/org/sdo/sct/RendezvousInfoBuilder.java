// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Builder for {@link RendezvousInfo} objects.
 */
public class RendezvousInfoBuilder {

  private final List<URI> uris = new ArrayList<>();

  /**
   * Build the {@link RendezvousInfo} object.
   */
  public RendezvousInfo build() {

    final List<RendezvousInstruction> instructions = new ArrayList<>();
    for (URI uri : uris) {
      instructions.add(build(uri));
    }
    return new RendezvousInfo(instructions);
  }

  private RendezvousInstruction build(URI uri) {
    final UriComponents uriComponents = UriComponentsBuilder.fromUri(uri).build();
    final List<RendezvousVariable> variables = new ArrayList<>();

    // Per protocol specification, keys must be alphabetical.  So we have:
    //
    // delaysec dn ip only po pow pr

    // delaysec...
    final MultiValueMap<String, String> uriParams = uriComponents.getQueryParams();
    String delay = uriParams.getFirst(RendezvousDelay.KEY);
    if (null != delay) {
      variables.add(new RendezvousDelay(Duration.ofSeconds(Long.parseUnsignedLong(delay))));
    }

    // dn ip...
    if (new InetAddressValidator().isValid(uri.getHost())) {
      try {
        variables.add(new RendezvousIpAddress(InetAddress.getByName(uri.getHost())));
      } catch (UnknownHostException e) {
        throw new RuntimeException(e); // this shouldn't happen, raw IPs don't need a lookup
      }
    } else {
      variables.add(new RendezvousHostname(uri.getHost()));
    }

    // only...
    RendezvousOnly.Only only = null;
    if (uriParams.containsKey(RendezvousOnly.KEY)) {
      only = RendezvousOnly.Only.valueOf(uriParams.getFirst(RendezvousOnly.KEY));
      variables.add(new RendezvousOnly(only));
    }

    // po pow...
    int port = uri.getPort();
    if (0 <= port) {
      if (RendezvousOnly.Only.owner != only) {
        variables.add(new RendezvousPortDevice(port));
      }

      if (RendezvousOnly.Only.dev != only) {
        variables.add(new RendezvousPortOwner(port));
      }
    }

    // pr...
    variables.add(new RendezvousProtocol(RendezvousProtocol.Protocol.valueOfName(uri.getScheme())));

    return new RendezvousInstruction(variables);
  }

  /**
   * Add a collection of URIs to the rendezvous instruction list.
   *
   * @param uris The URIs to add.
   *
   * @return this builder.
   */
  public RendezvousInfoBuilder with(Collection<URI> uris) {
    this.uris.addAll(uris);
    return this;
  }

  /**
   * Add a URI to the rendezvous instruction list.
   *
   * @param uri The URI to add.
   *
   * @return this builder.
   */
  public RendezvousInfoBuilder with(URI uri) {
    uris.add(uri);
    return this;
  }

}
