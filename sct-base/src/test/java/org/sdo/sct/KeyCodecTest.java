// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.KeyStoreException;
import java.security.PublicKey;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;

class KeyCodecTest {

  final static String EC256_KEY = "[91,\"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEJLQhIi2DrZoPLg/KTq/0qoCMllQ2BzETg6YFoGwRid6kPQzKT7W91BYjznE7ATpP+Fz4Fjt2Apw9inlrxE2jxg==\"]";
  final static String EC384_KEY = "[120,\"MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAEtfejBdXmhQsDJtpkFOreMzkzn1lfldlHuxXeGirDRdeVhI8DmiaE0e6F4HhUXtDJv/YDgrKxs/ihhBmMdU8XnXvseSBgisac+3j37lp5fB+oyun4r6Cdh+0BZcO9W0sk\"]";
  final static String RSA2048_KEY = "[257,\"AO4N1WyNHIVvQE9S7chM5WtqJeZ31PI5/G6QOqW8iIVtV+FLXPqmDtTximusss1Zg1XFM9RpDtCO8bIknhC7n6XSItFFXaDLPG8HIH+GYtzWEjMt0Kzo4qpv/8CUgNPTMrh/cV7b8WjxddocI5tt9rScqGr93OxGXPHf5ft2s5F/QyBEZFpe0kHESIBaoBU+eSIRMUY79YGbJ2Ffk4zBUkmyeOySTr5ShiwD9X+3FeBfkaLNBQpRcm5LgUzt+jmbmC3IA+0evscOWW2MApL90FAPa4TNgyaJ2O3TEsDOfe+7mXiS/sSiS6TfG0qE0G87G6tyOz5YkJfUypz5V1wF7tc=\",3,\"AQAB\"]";

  final KeyStores keyStores = loadKeyStores();

  PasswordCallbackFunction keyStorePw() {
    return () -> "123456".toCharArray();
  }

  KeyStores loadKeyStores() {
    KeyStoresFactory keyStoresFactory = new KeyStoresFactory(
      "123456"::toCharArray, new ClassPathResource("org/sdo/sct/test.p12"));
    keyStoresFactory.setSingleton(false);
    try {
      return keyStoresFactory.getObject();
    } catch (Exception e) {
      return null;
    }
  }

  @ParameterizedTest
  @DisplayName("keys are reflexive")
  @EnumSource(value = KeyType.class, names = {"ECDSA_P_256", "ECDSA_P_384", "RSA2048RESTR"})
  void test(KeyType keyType) throws KeyStoreException {

    KeyFinder keyFinder = new KeyFinder(keyStores, keyStorePw());
    KeyHandle keyh = keyFinder.find(keyType).orElseThrow(NoSuchElementException::new);

    PublicKey expected = keyh.getCertificate().getPublicKey();
    PublicKey actual = KeyCodec.decode(KeyCodec.encode(expected));
    assertEquals(expected, actual);
  }

  @ParameterizedTest
  @DisplayName("bad encodings")
  @NullSource
  @ValueSource(strings = {
    "",
    "the quick brown fox jumped over the lazy dogs",
    "[0,0,[0]]]",
    "[0,0," + EC256_KEY + "]",
    "[13,1," + RSA2048_KEY + "]",
    "[1,3," + EC256_KEY + "]",
    "[14,1," + EC384_KEY,

  })
  void testBadEncoding(String encoded) {
    assertThrows(IllegalArgumentException.class, () -> KeyCodec.decode(encoded));
  }

  @ParameterizedTest
  @DisplayName("good non-null encodings")
  @ValueSource(strings = {
    "[13,1," + EC256_KEY + "]",
    "[14,1," + EC384_KEY + "]",
    "[1,3," + RSA2048_KEY + "]"
  })
  void testGoodNotNullEncoding(String encoded) {
    assertNotNull(KeyCodec.decode(encoded));
  }

  @ParameterizedTest
  @DisplayName("good null encodings")
  @ValueSource(strings = {
    "[0,0,[0]]"})
  void testGoodNullEncoding(String encoded) {
    assertNull(KeyCodec.decode(encoded));
  }

  @Test
  @DisplayName("length mismatch")
  void testInvalidKeySpec() {
    assertThrows(IllegalArgumentException.class,
      () -> KeyCodec.decode("[13,1," + EC256_KEY.replace("MFkwEwYH", "AAAAAAAA") + "]"));
  }

  @Test
  @DisplayName("invalid key")
  void testLengthMismatch() {
    assertThrows(IllegalArgumentException.class,
      () -> KeyCodec.decode("[13,1," + EC256_KEY.replace("[91,", "[90,") + "]"));
  }
}
