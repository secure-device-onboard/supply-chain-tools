// Copyright 2020 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.sdo.sct.KeyType;

import java.util.Base64;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MStringParserTest {

  String TEST_PUBKEY = "-----BEGIN PUBLIC KEY-----\n"
    + "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAEICpmzISbMOCBxKcvkQLVAZUE6fbpnEQa\n"
    + "HoO/x1rdMuRCn9pBqfDeqoP8mpR2SspMDTt0pXxvAb+KSZkFbU89cEnqx7zSH64K\n"
    + "dRLe5RdJUy1opGNFtM8CZgsG1wExODBC\n"
    + "-----END PUBLIC KEY-----";

  String TEST_CSR = "-----BEGIN CERTIFICATE REQUEST-----\n"
    + "MIIBSzCB0wIBADBUMQswCQYDVQQGEwJVUzENMAsGA1UECAwEVGVzdDENMAsGA1UE\n"
    + "BwwEVGVzdDENMAsGA1UECgwEVGVzdDEYMBYGA1UEAwwPd3d3LmV4YW1wbGUuY29t\n"
    + "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAEICpmzISbMOCBxKcvkQLVAZUE6fbpnEQa\n"
    + "HoO/x1rdMuRCn9pBqfDeqoP8mpR2SspMDTt0pXxvAb+KSZkFbU89cEnqx7zSH64K\n"
    + "dRLe5RdJUy1opGNFtM8CZgsG1wExODBCoAAwCgYIKoZIzj0EAwIDZwAwZAIwfLKw\n"
    + "3zEY5XJDXzNgCGeRL8VafFaadlUcr98hG79sAnRSXQjmn50/DMFVKkyD0HCBAjB4\n"
    + "VKKiOUclzKe8eDi3ttPPFaut7drygQbcl3TmMacuAYgAcha+Yr9omsRvseUjbe4=\n"
    + "-----END CERTIFICATE REQUEST-----";

  @Test
  @DisplayName("getMstring contains illegal keytype constraint")
  void testIllegalKeyTypeConstraint() throws Exception {
    MStringParser msp = new MStringParser();

    assertThrows(NumberFormatException.class, () ->
      msp.parse(Base64.getEncoder().encodeToString("test\00test\00test".getBytes()))
    );

    assertThrows(IllegalArgumentException.class, () ->
      msp.parse(Base64.getEncoder().encodeToString("99\00test\00test".getBytes()))
    );
  }

  @Test
  @DisplayName("getMstring contains no field separators")
  void testNoFieldSeparators() throws Exception {
    MStringParser msp = new MStringParser();
    String m = "test";
    MStringParser.ParseResult pr = msp.parse(m);

    Assertions.assertEquals(KeyType.RSA2048RESTR, pr.getKeyTypeConstraint());
    assertEquals(m, pr.getSerialNumber());
    assertEquals(m, pr.getDeviceInfo());
    assertNull(pr.getCsr());
  }

  @Test
  @DisplayName("getMstring not base64 encoded")
  void testPlainString() throws Exception {
    MStringParser msp = new MStringParser();
    String m = "the quick brown fox jumped over the lazy dogs";
    MStringParser.ParseResult pr = msp.parse(m);

    Assertions.assertEquals(KeyType.RSA2048RESTR, pr.getKeyTypeConstraint());
    assertEquals(m, pr.getSerialNumber());
    assertEquals(m, pr.getDeviceInfo());
    assertNull(pr.getCsr());
  }

  @Test
  @DisplayName("getMstring contains too few field separators")
  void testTooFewFieldSeparators() throws Exception {
    MStringParser msp = new MStringParser();
    String m = Base64.getEncoder().encodeToString("test\00".getBytes());
    MStringParser.ParseResult pr = msp.parse(m);

    Assertions.assertEquals(KeyType.RSA2048RESTR, pr.getKeyTypeConstraint());
    assertEquals(m, pr.getSerialNumber());
    assertEquals(m, pr.getDeviceInfo());
    assertNull(pr.getCsr());
  }

  @Test
  @DisplayName("getMstring contains no CSR")
  void testNoCSR() throws Exception {
    MStringParser msp = new MStringParser();
    String m = Base64.getEncoder().encodeToString("14\00test1\00test2".getBytes());
    MStringParser.ParseResult pr = msp.parse(m);

    Assertions.assertEquals(KeyType.ECDSA_P_384, pr.getKeyTypeConstraint());
    assertEquals("test1", pr.getSerialNumber());
    assertEquals("test2", pr.getDeviceInfo());
    assertNull(pr.getCsr());
  }

  @Test
  @DisplayName("getMstring contains PEM CSR")
  void testPEM_CSR() throws Exception {

    MStringParser msp = new MStringParser();

    String m = "14\00test1\00test2\00" + TEST_CSR;
    m = Base64.getEncoder().encodeToString(m.getBytes());
    MStringParser.ParseResult pr = msp.parse(m);

    Assertions.assertEquals(KeyType.ECDSA_P_384, pr.getKeyTypeConstraint());
    assertEquals("test1", pr.getSerialNumber());
    assertEquals("test2", pr.getDeviceInfo());
    assertNotNull(pr.getCsr());
  }

  @Test
  @DisplayName("getMstring doesn't contain PEM CSR")
  void testPEM_nonCSR() throws Exception {

    MStringParser msp = new MStringParser();

    String m = "14\00test1\00test2\00" + TEST_PUBKEY;

    assertThrows(IllegalArgumentException.class,
      () -> msp.parse(Base64.getEncoder().encodeToString(m.getBytes())));
  }
}
