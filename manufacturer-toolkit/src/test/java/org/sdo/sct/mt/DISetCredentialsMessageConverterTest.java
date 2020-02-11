// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.sdo.sct.RendezvousInfo;
import org.sdo.sct.RendezvousInfoBuilder;
import org.sdo.sct.VoucherHeader;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.http.MockHttpOutputMessage;

class DiSetCredentialsMessageConverterTest {

  @Test
  void readInternal() {
    DiSetCredentialsMessageConverter c = new DiSetCredentialsMessageConverter();
    assertThrows(UnsupportedOperationException.class, () ->
      c.readInternal(DiSetCredentials.class, new MockHttpInputMessage(new byte[0])));
  }

  @Test
  void writeInternal() {
    DiSetCredentialsMessageConverter c = new DiSetCredentialsMessageConverter();

    final UUID uuid = UUID.fromString("691355c8-3172-412a-95d4-4560d00fd406");

    final RendezvousInfo rendezvousInfo =
      new RendezvousInfoBuilder().with(URI.create("http://127.0.0.1")).build();
    DiSetCredentials diSetCredentials = new DiSetCredentials(
      new VoucherHeader(
        rendezvousInfo,
        uuid,
        "[hello world!]",
        null,
        null));

    final String expected = "{\"oh\":{\"pv\":113,"
      + "\"pe\":0,"
      + "\"r\":[1,[2,{\"ip\":[4,\"fwAAAQ==\"],\"pr\":\"http\"}]],"
      + "\"g\":\"aRNVyDFyQSqV1EVg0A/UBg==\","
      + "\"d\":\"\\u005bhello world!\\u005d\","
      + "\"pk\":[0,0,[0]]}}";

    MockHttpOutputMessage m = new MockHttpOutputMessage();
    assertDoesNotThrow(() -> c.writeInternal(diSetCredentials, m));
    assertEquals(expected, m.getBodyAsString());
  }
}
