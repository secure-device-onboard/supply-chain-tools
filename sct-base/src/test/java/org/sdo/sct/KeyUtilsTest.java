// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.sdo.sct.KeyUtils.UnsupportedEcCurveException;
import org.sdo.sct.KeyUtils.UnsupportedExponentException;
import org.sdo.sct.KeyUtils.UnsupportedKeySizeException;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.stream.Stream;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KeyUtilsTest {

  static Stream<Arguments> testToTypeParamProvider() {
    return Stream.of(
      Arguments.arguments(
        KeyType.ECDSA_P_256,
        "-----BEGIN PUBLIC KEY-----\n"
          + "MIIBSzCCAQMGByqGSM49AgEwgfcCAQEwLAYHKoZIzj0BAQIhAP////8AAAABAAAA\n"
          + "AAAAAAAAAAAA////////////////MFsEIP////8AAAABAAAAAAAAAAAAAAAA////\n"
          + "///////////8BCBaxjXYqjqT57PrvVV2mIa8ZR0GsMxTsPY7zjw+J9JgSwMVAMSd\n"
          + "NgiG5wSTamZ44ROdJreBn36QBEEEaxfR8uEsQkf4vOblY6RA8ncDfYEt6zOg9KE5\n"
          + "RdiYwpZP40Li/hp/m47n60p8D54WK84zV2sxXs7LtkBoN79R9QIhAP////8AAAAA\n"
          + "//////////+85vqtpxeehPO5ysL8YyVRAgEBA0IABG8hE0kEnjHYi/q+otyI6NMy\n"
          + "pnq48K9F8kX1pOoGu5BtlgmU4qWKmBceqUrqjWAtldTC3jqiADvOLrKRagJM3wo=\n"
          + "-----END PUBLIC KEY-----"),
      Arguments.arguments(
        KeyType.ECDSA_P_256,
        "-----BEGIN PUBLIC KEY-----\n"
          + "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEhyYCEt6lpputkOovC6LtMBSHKY3/\n"
          + "hzrKrb/DYeG/JjZT3+y91B6PoBryxQWguT8yZf0e2WJKsDpUahUnlmERuw==\n"
          + "-----END PUBLIC KEY-----"
      ),
      Arguments.arguments(
        KeyType.ECDSA_P_384,
        "-----BEGIN PUBLIC KEY-----\n"
          + "MIIBzDCCAWQGByqGSM49AgEwggFXAgEBMDwGByqGSM49AQECMQD/////////////\n"
          + "/////////////////////////////v////8AAAAAAAAAAP////8wewQw////////\n"
          + "//////////////////////////////////7/////AAAAAAAAAAD////8BDCzMS+n\n"
          + "4j7n5JiOBWvj+C0ZGB2cbv6BQRIDFAiPUBOHWsZWOY2KLtGdKoXI7dPsKu8DFQCj\n"
          + "NZJqoxmieh0AiWpnc6SCes2scwRhBKqHyiK+iwU3jrHHHvMgrXRuHTtii6ebmFn3\n"
          + "QeCCVCo4VQLyXb9VKWw6VF44cnYKtzYX3kqWJixvXZ6Yv5KS3Cn49B29KJoUfOna\n"
          + "MRO18LjACmCxzh1+gZ16Qx18kOoOXwIxAP//////////////////////////////\n"
          + "/8djTYH0Ny3fWBoNskiwp3rs7BlqzMUpcwIBAQNiAATZ6YA0eKa1SFt8XRwYG5h6\n"
          + "1q8LWyGZXt/upPai1VhsqyxMysK/7DAnwjsWm8AZmLs6osjvp3xC12UN5vx9rAYt\n"
          + "YyrQkbDnxGEiCGkh4xx8gjWfvJZOdspNMqu6toPoeVs=\n"
          + "-----END PUBLIC KEY-----"
      ),
      Arguments.arguments(
        KeyType.ECDSA_P_384,
        "-----BEGIN PUBLIC KEY-----\n"
          + "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAEEqBSHAMgNJKFluYS8nvGqtQgJz5iRaj7\n"
          + "+frdWCR3N1TcwHFCa3yCaY/bbnrDqw2onmx9//Wjg7NcQxKreY6tOk6khCsjLTvu\n"
          + "BScCZiP8rB7DDu+QEl73N/hXka0iTkCU\n"
          + "-----END PUBLIC KEY-----"
      ),
      Arguments.arguments(
        KeyType.RSA2048RESTR,
        "-----BEGIN PUBLIC KEY-----\n"
          + "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwPkZzn1/2mD2dL25hs1i\n"
          + "SZtz1j2l31zeOStSZdHg5aSot0ah6f3AArjzM++2POK5pupzOwNF8ZSWG3mkrrzp\n"
          + "11m5uqmhdyCQGDSnCUz6hRBVbGu6B9cdo5MRSGgHNUWljLpvEgF64yx+4rb06w2o\n"
          + "b5Yr2Ts5KM+RIvCdyNA7/f7myYnhXtB6+u2XNvPCaS0DAAcnvMvfgj5GtLswNEJN\n"
          + "7ikFH1VF2gcK5/Z03jrM0mMfIXNf1Ha6KyFuyO3IYPXPAAt9Og/AG/5ByNwnE3xe\n"
          + "jgvWV+QP5WYx3ojlD/cq7j6Kby+n7RnMSi/qRKaf9t5yYCKdXDG++bj+K4G26tCO\n"
          + "OwIDAQAB\n"
          + "-----END PUBLIC KEY-----"
      )
    );
  }

  @ParameterizedTest
  @MethodSource("testToTypeParamProvider")
  void testToType(KeyType type, String pem) throws Exception {
    assertEquals(type, KeyUtils.toType(pemToKey(pem)));
  }

  @Test
  void testToTypeBadRsaExponent() throws Exception {

    PublicKey key = pemToKey("-----BEGIN PUBLIC KEY-----\n"
      + "MIIBIDANBgkqhkiG9w0BAQEFAAOCAQ0AMIIBCAKCAQEAw1maiBOkUKWGTWZl3VBh\n"
      + "vcbjim8JN9KNITl6Q4oiCN9A5SRTVNpaP5hUtKW9pSBXfGYDfk89iO0cseeELC3Y\n"
      + "jPSwx0r+vDsrUe3DClI8KsdXeDXEBW0J8cMhX4CKj07PzLP7LkThNJqUK5s1uK5O\n"
      + "KIJzhuoMbqBM4JoImRDQd/Xyu+4FEV0ejSZCFioT9TGtnueRyu7arwFOY3auXwIw\n"
      + "I1/kdIKN0BRTXZkNiA/NrxePxR7IXFDW07AWo3KJrxVDaCOgSBoO1H/XsDrgIdX9\n"
      + "LF5OCvpmgWw+jlkdlXbhOFFr1bnhKHFwyOAdwR8xFNYkaAw/Y9iJm5IYaUS+3S5R\n"
      + "XwIBAw==\n"
      + "-----END PUBLIC KEY-----");

    assertThrows(
      UnsupportedExponentException.class,
      () -> KeyUtils.toType(key));
  }

  @Test
  void testToTypeBadRsaModulus() throws Exception {

    PublicKey key = pemToKey("-----BEGIN PUBLIC KEY-----\n"
      + "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDfIT4x9pUqjetrV1tCX0r1JFWf\n"
      + "mw96dvvMdpJGiOCtR5NphVgGsfex8STlloRQQ0Lg+g4hZ8Xw2LxKNAI/3t6yEyME\n"
      + "AaKR/7d0CkYmV6PnO+zZZJz2jFxJLf9Y7D/Qb2ifAEG1FR1H/r3zVQns7Tchahy/\n"
      + "8ccwRkdjEPglZwIqpQIDAQAB\n"
      + "-----END PUBLIC KEY-----");

    assertThrows(
      UnsupportedKeySizeException.class,
      () -> KeyUtils.toType(key));
  }

  @Test
  void testToTypeBadEcCurve() throws Exception {

    PublicKey key = pemToKey("-----BEGIN PUBLIC KEY-----\n"
      +"ME4wEAYHKoZIzj0CAQYFK4EEACEDOgAEBqJH/C0ZwDMbHQ4EVw4CEF9bli9lHIL3\n"
      +"0GpIhbE6nUjJlNiIIjL5XEixwiSp1vNVDNKtQWAciQk=\n"
      +"-----END PUBLIC KEY-----");

    assertThrows(
      UnsupportedEcCurveException.class,
      () -> KeyUtils.toType(key));
  }

  private PublicKey pemToKey(String pem)
    throws InvalidKeySpecException, IOException, NoSuchAlgorithmException {

    PEMParser pemParser = new PEMParser(new StringReader(pem));
    SubjectPublicKeyInfo spki = (SubjectPublicKeyInfo) pemParser.readObject();
    final KeyFactory keyFactory = KeyFactory.getInstance(
      spki.getAlgorithm().getAlgorithm().toString(),
      BouncyCastleSingleton.INSTANCE);
    final X509EncodedKeySpec subjectKeySpec =
      new X509EncodedKeySpec(spki.getEncoded());
    return keyFactory.generatePublic(subjectKeySpec);
  }
}

