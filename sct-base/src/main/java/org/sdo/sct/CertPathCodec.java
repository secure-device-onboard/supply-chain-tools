// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.List;

public class CertPathCodec {

  /**
   * Encodes the certPath.
   *
   * @param certPath certificate path
   * @return encoded certificate path
   * @throws CertificateEncodingException if error
   */
  public static String encode(final CertPath certPath) throws CertificateEncodingException {
    final List<? extends Certificate> certificates = certPath.getCertificates();
    StringBuilder builder = new StringBuilder();

    builder.append("[1,");
    builder.append(IntegerCodec.encode(certificates.size(), 8));
    builder.append(",[");
    String separator = "";
    for (Certificate certificate : certificates) {
      builder.append(separator);
      separator = ",";

      final byte[] der = certificate.getEncoded();
      builder.append("[");
      builder.append(IntegerCodec.encode(der.length, 16));
      builder.append(",");
      builder.append(ArrayCodec.encode(der));
      builder.append("]");
    }
    builder.append("]]");

    return builder.toString();
  }
}
