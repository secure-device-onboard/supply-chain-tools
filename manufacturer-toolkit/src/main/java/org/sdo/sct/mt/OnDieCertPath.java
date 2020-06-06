// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.util.encoders.Base64;
import org.sdo.sct.BouncyCastleSingleton;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class OnDieCertPath {

  private CertificateFactory certFactory;

  /**
   * Constructor.
   *
   */
  @Autowired
  public OnDieCertPath() throws Exception {
    LoggerFactory.getLogger(getClass()).debug("OnDieCertPathService");
    certFactory = CertificateFactory.getInstance("X509", BouncyCastleSingleton.INSTANCE);
  }

  /**
   * Builds a cert path for the OnDie device.
   *
   * @param b64DeviceCertChain the cert chain from ROM to leaf from the device
   * @return a complete CA to leaf cert path
   * @throws Exception if error
   */
  public CertPath buildCertPath(
      String b64DeviceCertChain, OnDieCache onDieCertCache) throws Exception {

    List<Certificate> deviceCertChain = new ArrayList<Certificate>();

    // parse the device input chain into a list of x509 certificates
    List<byte[]> certList = deserializeCertificateChain(Base64.decode(b64DeviceCertChain));
    for (byte[] array : certList) {
      InputStream is = new ByteArrayInputStream(array);
      deviceCertChain.add(certFactory.generateCertificate(is));
    }

    // Based on the ROM certificate of the device, look up the issuing certificate
    // and then each subsequent issuing certificate. Append them to the list
    // to complete an end-to-end cert chain.
    // leaf -> DAL -> Kernel -> ROM -> platform -> intermediate -> OnDie CA

    // Start with the ROM cert, loop until we get to the CA
    String certId  = getIssuingCertificate(deviceCertChain.get(0));
    while (true) {
      byte[] cert = onDieCertCache.getCertOrCrl(certId);
      if (cert == null) {
        throw new Exception(
          "Could not find cert in OnDie cache: unable to build cert chain for device: " + certId);
      }
      ByteArrayInputStream is = new ByteArrayInputStream(cert);
      X509Certificate x509Cert = (X509Certificate) certFactory.generateCertificate(is);
      deviceCertChain.add(x509Cert);
      is.close();

      // if we are at the root CA then we have the complete chain
      try {
        certId = getIssuingCertificate(x509Cert);
      } catch (Exception ex) {
        break;
      }
    }

    return certFactory.generateCertPath(deviceCertChain);
  }

  private String getIssuingCertificate(Certificate cert) throws Exception {
    X509CertificateHolder certholder = new X509CertificateHolder(cert.getEncoded());
    AuthorityInformationAccess aia =
        AuthorityInformationAccess.fromExtensions(certholder.getExtensions());
    if (aia == null) {
      throw new Exception("AuthorityInformationAccess Extension missing from device certificate.");
    }
    AccessDescription[] descs = aia.getAccessDescriptions();
    if (descs.length != 1) {
      throw new Exception("Too many descriptions in AIA certificate extension: " + descs.length);
    }
    return descs[0].getAccessLocation().getName().toString();
  }

  /// <summary>
  /// Deserializes a certificate chain byte array and returns a more flexible data-structure
  //  representing the chain.
  /// Given chain should be of the following format:
  /// [ number of certificates (2 bytes) | size of each certificate (2 bytes each) |
  //  certificate chain from root to leaf ]
  /// </summary>
  /// <param name="certChain">The certificate chain to parse.</param>
  /// <returns></returns>
  private List<byte[]> deserializeCertificateChain(byte[] certChain) throws Exception {
    List<byte[]> deserializedCertChain = new ArrayList<byte[]>();
    int offset = 0;

    // get number of certificates
    int numOfCerts;
    numOfCerts = getShortFromBytes(certChain, offset);
    offset += 2;

    // get sizes of all certificates
    int[] sizesOfCertificates = new int[numOfCerts];

    for (int i = 0; i < numOfCerts; i++) {
      sizesOfCertificates[i] = getShortFromBytes(certChain, offset);
      offset += 2;
    }

    // deserialize certificate chain
    for (int i = 0; i < numOfCerts; i++) {
      byte[] cert = Arrays.copyOfRange(certChain, offset, offset + sizesOfCertificates[i]);
      offset += cert.length;
      deserializedCertChain.add(cert);
    }

    // check that we reached the end of the chain
    if (offset < certChain.length) {
      throw new Exception("Certificate chain is larger than expected.");
    }
    return deserializedCertChain;
  }

  /// <summary>
  /// Gets a short from a byte array (via out parameter), and returns the amount of bytes
  //  read by this method.
  /// </summary>
  /// <param name="bytes">The byte array from which to parse the short.</param>
  /// <param name="startIndex">The starting index of the short.</param>
  /// <param name="result">The result short value.</param>
  /// <returns></returns>
  private short getShortFromBytes(byte[] bytes, int startIndex) throws Exception {
    if (bytes.length < startIndex + 2) {
      throw new Exception("Byte array size is too small, or startIndex is too big.");
    }

    byte[] shortArr = Arrays.copyOfRange(bytes, startIndex, startIndex + 2);

    // firmware returns the value in big-endian
    ByteBuffer wrapped = ByteBuffer.wrap(shortArr); // big-endian by default
    return wrapped.getShort();
  }

}

