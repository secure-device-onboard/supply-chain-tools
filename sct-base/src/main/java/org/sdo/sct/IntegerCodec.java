// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

/**
 * Codec for SDO UInt* integer types.
 */
public class IntegerCodec {

  private static ResourceBundleHolder resourceBundleHolder =
      new ResourceBundleHolder(IntegerCodec.class.getName());

  /**
   * Decodes an SDO Uint* to a Java integer.
   *
   * @param text  The SDO UInt text.
   * @param width The width, in bits, of the UInt field.
   *
   * @return The decoded int.
   */
  public static int decode(final CharSequence text, final int width) {

    final int min = 0;
    final int max = (1 << width) - 1;
    final int value = Integer.decode(text.toString());
    if (min <= value && value <= max) {
      return value;
    } else {
      throw new IllegalArgumentException(resourceBundleHolder.get().getString("illegal.value"));
    }
  }

  /**
   * Encodes a Java integer to an SDO UInt.
   *
   * @param value The int to encode.
   * @param width The width, in bits, of the SDO UInt.
   *
   * @return The encoded UInt.
   */
  public static String encode(final long value, final int width) {

    final long min = 0;
    final long max = (1L << width) - 1;
    if (min <= value && value <= max) {
      return Long.toString(value);
    } else {
      throw new IllegalArgumentException(resourceBundleHolder.get().getString("illegal.value"));
    }
  }
}
