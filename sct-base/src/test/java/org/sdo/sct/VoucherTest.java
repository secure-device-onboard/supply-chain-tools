// Copyright 2020 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class VoucherTest {

  @Test
  void testAssign() throws Exception {

    final String sigAlg = "SHA384withECDSA";

    KeyPairGenerator keyPairGenerator =
      KeyPairGenerator.getInstance("EC", BouncyCastleSingleton.INSTANCE);
    keyPairGenerator.initialize(384, SecureRandom.getInstance("SHA1PRNG"));

    KeyPair mfrKeys = keyPairGenerator.generateKeyPair();
    KeyPair own1Keys = keyPairGenerator.generateKeyPair();
    KeyPair own2Keys = keyPairGenerator.generateKeyPair();

    VoucherHeader vh = new VoucherHeader(
      new RendezvousInfoBuilder().with(URI.create("http://localhost")).build(),
      UUID.randomUUID(),
      "test",
      mfrKeys.getPublic(),
      null);

    Voucher v1 = new Voucher(
      vh.toString(),
      new Hash.Null().toString(),
      null,
      Collections.emptyList());

    Signature mfrSigner = Signature.getInstance(sigAlg, BouncyCastleSingleton.INSTANCE);
    mfrSigner.initSign(mfrKeys.getPrivate());

    Signature own1Signer = Signature.getInstance(sigAlg, BouncyCastleSingleton.INSTANCE);
    own1Signer.initSign(own1Keys.getPrivate());

    Signature own2Signer = Signature.getInstance(sigAlg, BouncyCastleSingleton.INSTANCE);
    own2Signer.initSign(own2Keys.getPrivate());

    Voucher v2 = v1.assign(own1Keys.getPublic(), mfrSigner);
    Voucher v3 = v2.assign(own2Keys.getPublic(), own1Signer);

    assertTrue(v1.getEn().isEmpty());
    assertTrue(Voucher.decodeEntryList(0, v1.encodeEntryList()).isEmpty());
    assertThrows(
      IllegalArgumentException.class,
      () -> Voucher.decodeEntryList(1, v1.encodeEntryList()));

    assertFalse(v2.getEn().isEmpty());
    assertFalse(Voucher.decodeEntryList(1, v2.encodeEntryList()).isEmpty());
    assertThrows(
      IllegalArgumentException.class,
      () -> Voucher.decodeEntryList(0, v2.encodeEntryList()));

    assertFalse(v3.getEn().isEmpty());
    assertFalse(Voucher.decodeEntryList(2, v3.encodeEntryList()).isEmpty());

    assertTrue(v1.isOwner(mfrSigner));
    assertFalse(v2.isOwner(mfrSigner));
    assertFalse(v3.isOwner(mfrSigner));

    assertFalse(v1.isOwner(own1Signer));
    assertTrue(v2.isOwner(own1Signer));
    assertFalse(v3.isOwner(own1Signer));

    assertFalse(v1.isOwner(own2Signer));
    assertFalse(v2.isOwner(own2Signer));
    assertTrue(v3.isOwner(own2Signer));
  }
}
