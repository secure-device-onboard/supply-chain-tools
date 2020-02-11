// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.Base64;
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
  //
  // If the getMstring string doesn't match this pattern,
  // assume a legacy EPID device and use getMstring as serial number.
  ParseResult parse(String b64m) {

    String keyTypeConstraint;
    String serialNumber;
    String deviceInfo;
    String csrPem;

    try {
      byte[] m = Base64.getDecoder().decode(b64m);

      ByteArrayInputStream instream = new ByteArrayInputStream(m);

      Scanner scanner = new Scanner(instream).useDelimiter("\00");
      keyTypeConstraint = scanner.next();
      serialNumber = scanner.next();
      deviceInfo = scanner.next();

      if (scanner.hasNext()) {
        csrPem = scanner.next();
      } else {
        csrPem = null;
      }

    } catch (IllegalArgumentException | NoSuchElementException e) {
      LoggerFactory.getLogger(getClass()).debug(e.getMessage(), e);
      return new ParseResult(b64m, b64m, KeyType.RSA2048RESTR, null);
    }

    PKCS10CertificationRequest csr = null;
    if (null != csrPem) {
      try (final PEMParser parser = new PEMParser(new StringReader(csrPem))) {
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
      }
    }

    return new ParseResult(
      serialNumber,
      deviceInfo,
      KeyTypeCodec.decode(keyTypeConstraint),
      csr);
  }

  static class ParseResult {

    private final String serialNumber;
    private final String deviceInfo;
    private final KeyType keyTypeConstraint;
    private final PKCS10CertificationRequest csr;

    private ParseResult(
        String serialNumber,
        String deviceInfo,
        KeyType keyTypeConstraint,
        PKCS10CertificationRequest csr) {

      this.serialNumber = serialNumber;
      this.deviceInfo = deviceInfo;
      this.keyTypeConstraint = keyTypeConstraint;
      this.csr = csr;
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
  }
}
