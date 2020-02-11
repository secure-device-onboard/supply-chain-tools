// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Codec for converting between JCE Key and SDO PK* types.
 */
public class KeyCodec {

  private static final String PK_NULL = "[0]";
  private static ResourceBundleHolder resourceBundleHolder =
      new ResourceBundleHolder(KeyCodec.class.getName());

  /**
   * Decode an SDO-Encoded PK* key to a {@link PublicKey}.
   *
   * @param encoded The SDO key.
   *
   * @return The JCE {@link PublicKey}.
   */
  public static PublicKey decode(String encoded) {

    if (null == encoded) {
      String format = resourceBundleHolder.get().getString("not.a.key");
      throw new IllegalArgumentException(MessageFormat.format(format, "null"));
    }

    Pattern pat = Pattern.compile("^\\[(\\d+),(\\d+),(.+?)]$");
    Matcher matcher = pat.matcher(encoded);

    if (!matcher.matches()) {
      String format = resourceBundleHolder.get().getString("not.a.key");
      throw new IllegalArgumentException(MessageFormat.format(format, encoded));
    }

    final String typeCode = matcher.group(1);
    final int encodingCode = Integer.parseUnsignedInt(matcher.group(2));
    final String encodedKey = matcher.group(3);

    final KeySpec keySpec;
    switch (KeyEncoding.valueOfInt(encodingCode)) {
      case NONE:
        pat = Pattern.compile("^\\[0]$");
        matcher = pat.matcher(encodedKey);
        if (matcher.matches()) {
          return null;
        } else {
          String format = resourceBundleHolder.get().getString("not.a.null.key");
          throw new IllegalArgumentException(MessageFormat.format(format, encodedKey));
        }

      case X509:
        pat = Pattern.compile("^\\[(\\d+),(.+?)]$");
        matcher = pat.matcher(encodedKey);
        if (!matcher.matches()) {
          String format = resourceBundleHolder.get().getString("not.an.x509.key");
          throw new IllegalArgumentException(MessageFormat.format(format, encodedKey));
        }

        int pkBytes = Integer.parseUnsignedInt(matcher.group(1));
        byte[] der = ArrayCodec.decode(matcher.group(2));
        if (pkBytes != der.length) {
          String format = resourceBundleHolder.get().getString("length.mismatch");
          throw new IllegalArgumentException(MessageFormat.format(format, pkBytes, der.length));
        }

        keySpec = new X509EncodedKeySpec(der);
        break;

      case RSAMODEXP:
        pat = Pattern.compile("^\\[(\\d+),(.+?),(\\d+),(.+?)]$");
        matcher = pat.matcher(encodedKey);
        if (!matcher.matches()) {
          String format = resourceBundleHolder.get().getString("not.an.rsa.key");
          throw new IllegalArgumentException(MessageFormat.format(format, encodedKey));
        }

        int modBytes = Integer.parseUnsignedInt(matcher.group(1));
        byte[] modulus = ArrayCodec.decode(matcher.group(2));
        if (modBytes != modulus.length) {
          String format = resourceBundleHolder.get().getString("length.mismatch");
          throw new IllegalArgumentException(
            MessageFormat.format(format, modBytes, modulus.length));
        }

        int expBytes = Integer.parseUnsignedInt(matcher.group(3));
        byte[] exponent = ArrayCodec.decode(matcher.group(4));
        if (expBytes != exponent.length) {
          String format = resourceBundleHolder.get().getString("length.mismatch");
          throw new IllegalArgumentException(
            MessageFormat.format(format, expBytes, exponent.length));
        }

        keySpec = new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(exponent));
        break;

      default:
        // bug smell, means we missed a case
        throw new RuntimeException(
          resourceBundleHolder.get().getString("unexpected.switch.default"));
    } // switch (key encoding)

    final KeyType type = KeyTypeCodec.decode(typeCode);
    try {
      final KeyFactory keyFactory =
          KeyFactory.getInstance(type.getJceAlgorithm(), BouncyCastleSingleton.INSTANCE);
      return keyFactory.generatePublic(keySpec);

    } catch (InvalidKeySpecException e) {
      throw new IllegalArgumentException(e.getMessage(), e);

    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e.getMessage(), e); // bug smell
    }
  }

  /**
   * Encode a {@link PublicKey} to an SDO PK* Key.
   *
   * @param key The JCE {@link PublicKey}.
   *
   * @return The SDO PK* Key.
   */
  public static String encode(final PublicKey key) {

    final StringWriter writer = new StringWriter();
    writer.write('[');
    final KeyType keyType = KeyUtils.toType(key);
    writer.write(KeyTypeCodec.encode(keyType));

    // Key type and key encoding have a 1:1 relationship - for each key type there is
    // only one valid encoding and vice versa.
    writer.write(',');
    final KeyEncoding keyEncoding = KeyUtils.toEncoding(keyType);
    writer.write(keyEncoding.toString());

    writer.write(',');

    switch (keyEncoding) {

      case NONE:
        writer.write(PK_NULL);
        break;

      case X509:
        final byte[] x509 = new X509EncodedKeySpec(key.getEncoded()).getEncoded();

        writer.write('[');
        writer.write(Integer.toString(x509.length));

        writer.write(',');
        writer.write(ArrayCodec.encode(x509));

        writer.write(']');
        break;

      case RSAMODEXP:
        final RSAPublicKey rsa = (RSAPublicKey) key;
        final byte[] mod = rsa.getModulus().toByteArray();
        final byte[] exp = rsa.getPublicExponent().toByteArray();

        writer.write('[');
        writer.write(Integer.toString(mod.length));

        writer.write(',');
        writer.write(ArrayCodec.encode(mod));

        writer.write(',');
        writer.write(Integer.toString(exp.length));

        writer.write(',');
        writer.write(ArrayCodec.encode(exp));

        writer.write(']');
        break;

      default:
        // bug smell, means we missed a case
        throw new RuntimeException(
          resourceBundleHolder.get().getString("unexpected.switch.default"));
    }

    writer.write(']');

    return writer.toString();
  }
}
