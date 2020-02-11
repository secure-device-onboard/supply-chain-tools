// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.sec.SECObjectIdentifiers;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;

/**
 * Key-related utilities.
 */
public class KeyUtils {

  private static ResourceBundleHolder resourceBundleHolder_ =
      new ResourceBundleHolder(KeyUtils.class.getName());

  /**
   * Identify the {@link KeyType} of the given {@link PublicKey}.
   */
  public static KeyType toType(final PublicKey key) {

    if (null == key) {
      return KeyType.NONE;

    } else if (key instanceof ECPublicKey) {
      final ECPublicKey ecKey = (ECPublicKey) key;
      final ASN1Sequence asn1Sequence = ASN1Sequence.getInstance(ecKey.getEncoded());

      // The curve parameters can be direct or indirect.
      //
      // If it's indirect,
      // the key will contain an OID identifying the curve parameters.
      //
      // If it's direct,
      // the key will contain the curve parameters, which we must match
      // against a known curve.
      //
      for (final ASN1ObjectIdentifier oid : findObjectIdentifiers(asn1Sequence)) {
        if (SECObjectIdentifiers.secp256r1.equals(oid)) {
          return KeyType.ECDSA_P_256;

        } else if (SECObjectIdentifiers.secp384r1.equals(oid)) {
          return KeyType.ECDSA_P_384;

        } else if (X9ObjectIdentifiers.prime_field.equals(oid)) {

          final ECParameterSpec secp256r1 =
              EC5Util.convertToSpec(SECNamedCurves.getByOID(SECObjectIdentifiers.secp256r1));
          final ECParameterSpec secp384r1 =
              EC5Util.convertToSpec(SECNamedCurves.getByOID(SECObjectIdentifiers.secp384r1));

          if (areEqualByValue(secp256r1, ecKey.getParams())) {
            return KeyType.ECDSA_P_256;

          } else if (areEqualByValue(secp384r1, ecKey.getParams())) {
            return KeyType.ECDSA_P_384;

          }
        }
      }

      throw new UnsupportedEcCurveException(
        resourceBundleHolder_.get().getString("unsupported.ec.curve"));

    } else if (key instanceof RSAPublicKey) {
      final RSAPublicKey rsaKey = (RSAPublicKey) key;

      if (!RSAKeyGenParameterSpec.F4.equals(rsaKey.getPublicExponent())) {
        String format = resourceBundleHolder_.get().getString("unsupported.rsa.exponent");
        throw new UnsupportedExponentException(
          MessageFormat.format(format, rsaKey.getPublicExponent()));
      }

      if (2048 == rsaKey.getModulus().bitLength()) {
        return KeyType.RSA2048RESTR;

      } else {
        String format = resourceBundleHolder_.get().getString("unsupported.rsa.size");
        throw new UnsupportedKeySizeException(
          MessageFormat.format(format, rsaKey.getModulus().bitLength()));
      }
    }

    String format = resourceBundleHolder_.get().getString("unexpected.key");
    throw new IllegalArgumentException(MessageFormat.format(format, key.getClass().getName()));
  }

  public static class UnsupportedEcCurveException extends IllegalArgumentException {
    public UnsupportedEcCurveException(String s) {
      super(s);
    }
  }

  public static class UnsupportedExponentException extends IllegalArgumentException {
    public UnsupportedExponentException(String s) {
      super(s);
    }
  }

  public static class UnsupportedKeySizeException extends IllegalArgumentException {
    public UnsupportedKeySizeException(String s) {
      super(s);
    }
  }

  /**
   * Converts a {@link KeyType} to its corresponding {@link KeyEncoding}.
   */
  static KeyEncoding toEncoding(final KeyType type) {

    switch (type) {

      case NONE:
        return KeyEncoding.NONE;

      case RSA2048RESTR:
        return KeyEncoding.RSAMODEXP;

      case ECDSA_P_256:
      case ECDSA_P_384:
        return KeyEncoding.X509;

      default:
        String format = resourceBundleHolder_.get().getString("unexpected.default");
        throw new RuntimeException(MessageFormat.format(format, type));
    }
  }

  // ECParameterSpec types only support equality by identity, but we need
  // to be able to check for value-equality.
  private static boolean areEqualByValue(final ECParameterSpec left, final ECParameterSpec right) {

    return !(null == left || null == right)
      && left.getCurve().equals(right.getCurve())
      && left.getCofactor() == right.getCofactor()
      && left.getGenerator().equals(right.getGenerator())
      && left.getOrder().equals(right.getOrder());
  }

  private static Set<ASN1ObjectIdentifier> findObjectIdentifiers(
      final ASN1Encodable asn1Encodable) {

    if (asn1Encodable instanceof ASN1ObjectIdentifier) {
      final Set<ASN1ObjectIdentifier> s = new HashSet<>();
      s.add((ASN1ObjectIdentifier) asn1Encodable);
      return s;

    } else if (asn1Encodable instanceof DLSequence) {
      final DLSequence dlSequence = (DLSequence) asn1Encodable;
      final Set<ASN1ObjectIdentifier> s = new HashSet<>();

      dlSequence.forEach(element -> s.addAll(findObjectIdentifiers(element)));
      return s;

    } else {
      return Collections.emptySet();
    }
  }
}
