// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Scanner;

import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.sdo.sct.KeyType;
import org.sdo.sct.KeyTypeCodec;
import org.slf4j.LoggerFactory;

class MStringParser {

  // DI.AppStart.getMstring is the base64-coded null-separated concatenation of multiple strings:
  // - the key type requirement of the device, as an integer-coded KeyType
  //   This is the type of owner key the device is prepared to parse, not the type
  //   of the device's key.
  // - the device serial number
  // - a device info hint
  // - if the device is using an EC keypair, a CSR.
  // - if the device is OnDie ECDSA then the device cert chain and a
  //   test signature is also present
  //
  // If the getMstring string doesn't match a valid pattern,
  // assume a legacy EPID device and use getMstring as serial number.
  ParseResult parse(String b64m) {

    String keyTypeConstraint;
    String serialNumber;
    String deviceInfo;
    String token4 = null;
    String token5 = null;

    // set defaults for optional values derived from mstring
    PKCS10CertificationRequest csr = null; // used by ECC devices

    //The next three values are used by OnDie ECDSA devices
    String onDieCertChain = null;
    String testSignature = null;

    KeyType keyType = null;

    // do some basic verification of the different options
    Scanner scanner = null;
    try {
      byte[] m = Base64.getDecoder().decode(b64m);

      ByteArrayInputStream instream = new ByteArrayInputStream(m);

      scanner = new Scanner(instream).useDelimiter("\00");

      // the first three tokens are common for all devices
      keyTypeConstraint = scanner.next();
      serialNumber = scanner.next();
      deviceInfo = scanner.next();

      if (scanner.hasNext()) {
        token4 = scanner.next();
      }
      if (scanner.hasNext()) {
        token5 = scanner.next();
      }
    } catch (IllegalArgumentException | NoSuchElementException e) {
      // Error encountered so default to EPID device and mstring = device serial no.
      // Most likely the user specified some unencoded mstring such as "test", "device 1", etc.
      LoggerFactory.getLogger(getClass()).debug(e.getMessage(), e);
      return new ParseResult(
          b64m, b64m, KeyType.RSA2048RESTR, null, null, null);
    }

    // An encoded mstring with the minimum number of tokens was specified so
    // from this point on any error will be treated as such and not a fallback
    // to default behavior.

    // Validate the key type next.
    keyType = KeyTypeCodec.decode(keyTypeConstraint);

    if ((keyType.getId() == KeyType.ECDSA_P_256.getId())
        || (keyType.getId() == KeyType.ECDSA_P_384.getId())) {
      // this is an ECDSA device
      // Note: token4 is the csr, it is not mandatory and will be allowed
      if (token4 != null) {
        try (final PEMParser parser = new PEMParser(new StringReader(token4))) {
          Object o = parser.readObject();
          if (o instanceof PKCS10CertificationRequest) {
            csr = (PKCS10CertificationRequest) o;
          } else {
            // There is no healthy case in which we might not find a CSR,
            // so if we find a not-a-CSR, fail hard.
            String format =
                ResourceBundle.getBundle(getClass().getName(), Locale.getDefault())
                .getString("not.a.csr");
            throw new IllegalArgumentException(
              MessageFormat.format(format, o.getClass().getSimpleName()));
          }
        } catch (IOException e) {
          LoggerFactory.getLogger(getClass()).debug(e.getMessage(), e);
          throw new IllegalArgumentException("csr parsing error: " + e.getMessage());
        }
      }
    } else if (token4 != null && token5 != null) {
      // this is an OnDie ECDSA device
      try {
        // processing of cert chain and usage of test signature comes later so
        // just pass these along
        onDieCertChain = token4;
        testSignature = token5;
      } catch (Exception e) {
        LoggerFactory.getLogger(getClass()).debug(e.getMessage(), e);
        throw new IllegalArgumentException("onDie error: " + e.getMessage());
      }
    } else if (keyType.getId() == KeyType.RSA2048RESTR.getId()) {
      // EPID device - there is nothing more to do
    } else {
      throw new IllegalArgumentException("unrecognized device type: " + keyType.getId());
    }

    return new ParseResult(
      serialNumber,
      deviceInfo,
      keyType,
      csr,
      onDieCertChain,
      testSignature);
  }

  static class ParseResult {

    private final String serialNumber;
    private final String deviceInfo;
    private final KeyType keyTypeConstraint;
    private final PKCS10CertificationRequest csr;
    private final String onDieCertChain;
    private final String onDieTestSignature;

    private ParseResult(
        String serialNumber,
        String deviceInfo,
        KeyType keyTypeConstraint,
        PKCS10CertificationRequest csr,
        String onDieCertChain,
        String onDieTestSignature) {

      this.serialNumber = serialNumber;
      this.deviceInfo = deviceInfo;
      this.keyTypeConstraint = keyTypeConstraint;
      this.csr = csr;
      this.onDieCertChain = onDieCertChain;
      this.onDieTestSignature = onDieTestSignature;
    }

    PKCS10CertificationRequest getCsr() {
      return csr;
    }

    String getDeviceInfo() {
      return deviceInfo;
    }

    KeyType getKeyTypeConstraint() {
      return keyTypeConstraint;
    }

    String getSerialNumber() {
      return serialNumber;
    }

    String getOnDieCertChain() {
      return onDieCertChain;
    }

    String getOnDieTestSignature() {
      return onDieTestSignature;
    }

  }
}
