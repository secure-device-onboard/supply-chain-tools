// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.X509CertificateHolder;
import org.sdo.sct.KeyFinder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class OnDieSignatureValidator {

  private OnDieCache onDieCache;

  /**
   * Constructor.
   */
  @Autowired
  public OnDieSignatureValidator(OnDieCache onDieCache) {
    this.onDieCache = onDieCache;
  }

  /**
   * Performs a validation of the given signature with the public key
   * extracted from the given cert chain.
   *
   * @param certChain certChain
   * @param signedData signedData
   * @param signature signature
   * @return boolean indicating if signature is valid.
   * @throws Exception when error.
   */
  public boolean validate(CertPath certChain,
                          byte[] signedData,
                          byte[] signature) throws Exception {

    List<Certificate> certificateList = (List<Certificate>) certChain.getCertificates();

    // Check revocations first.
    if (!checkRevocations(certificateList)) {
      return false;
    }

    try {
      byte[] taskInfo = Arrays.copyOfRange(signature, 0, 36);

      // adjust the signed data
      // data-to-verify format is: [ task-info | nonce (optional) | data ]
      // First 36 bytes of signature is the taskinfo. This value must be prepended
      // to the signedDat
      ByteArrayOutputStream adjSignedData = new ByteArrayOutputStream();
      adjSignedData.write(Arrays.copyOfRange(signature, 0, taskInfo.length));
      adjSignedData.write(signedData);

      byte[] adjSignature = convertSignature(signature, taskInfo);
      PublicKey publicKey = certificateList.get(0).getPublicKey();

      Signature sig = Signature.getInstance("SHA384withECDSA");
      sig.initVerify(publicKey);
      sig.update(adjSignedData.toByteArray());
      boolean result = sig.verify(adjSignature);
      return result;
    } catch (Exception ex) {
      return false;
    }
  }

  private static byte[] convertSignature(byte[] signature, byte[] taskInfo) throws Exception {
    // 0x30 b1 0x02 b2 (vr) 0x02 b3 (vs)
    byte[] rvalue = Arrays.copyOfRange(signature, taskInfo.length, taskInfo.length + 48);
    byte[] svalue = Arrays.copyOfRange(signature, taskInfo.length + 48, taskInfo.length + 96);

    boolean appendZeroToR = false;
    boolean appendZeroToS = false;
    if ((rvalue[0] & 0x80) != 0) {
      appendZeroToR = true;
    }
    if ((svalue[0] & 0x80) != 0) {
      appendZeroToS = true;
    }

    ByteArrayOutputStream adjSignature = new ByteArrayOutputStream();
    adjSignature.write(0x30);
    // total length of remaining bytes
    adjSignature.write(4 + (appendZeroToR ? 49 : 48) + (appendZeroToS ? 49 : 48));
    adjSignature.write(0x02);
    // R value
    if (appendZeroToR) {
      adjSignature.write(49);
      adjSignature.write(0x00);
      adjSignature.write(rvalue);
    } else {
      adjSignature.write(48);
      adjSignature.write(rvalue);
    }
    adjSignature.write(0x02);
    // S value
    if (appendZeroToS) {
      adjSignature.write(49);
      adjSignature.write(0x00);
      adjSignature.write(svalue);
    } else {
      adjSignature.write(48);
      adjSignature.write(svalue);
    }
    return adjSignature.toByteArray();
  }

  private boolean checkRevocations(List<Certificate> certificateList) throws Exception {
    // Check revocations first.
    CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
    for (int i = 0; i < certificateList.size(); i++) {
      X509Certificate cert = (X509Certificate) certificateList.get(i);
      X509CertificateHolder certHolder = new X509CertificateHolder(cert.getEncoded());
      CRLDistPoint cdp = CRLDistPoint.fromExtensions(certHolder.getExtensions());
      if (cdp != null) {
        DistributionPoint[] distPoints = cdp.getDistributionPoints();
        for (DistributionPoint dp : distPoints) {
          GeneralName[] generalNames =
            GeneralNames.getInstance(dp.getDistributionPoint().getName()).getNames();
          for (int j = 0; j < generalNames.length; j++) {
            byte[] crlBytes = onDieCache.getCertOrCrl(generalNames[j].getName().toString());
            if (crlBytes == null) {
              LoggerFactory.getLogger(this.getClass()).warn("CRL not found in cache for: "
                  + generalNames[j].getName().toString());
            } else {
              CRL crl = certificateFactory.generateCRL(new ByteArrayInputStream(crlBytes));
              if (crl.isRevoked(cert)) {
                return false;
              }
            }
          }
        }
      }
    }
    return true;
  }
}

