// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.text.MessageFormat;

/**
 * Codec for {@link KeyType}.
 */
public class KeyTypeCodec {

  private static final int WIDTH = 8;
  private static ResourceBundleHolder resourceBundleHolder_ =
      new ResourceBundleHolder(KeyTypeCodec.class.getName());

  /**
   * Decodes the given string as a {@link KeyType}.
   */
  public static KeyType decode(CharSequence encoded) {

    if (null == encoded) {
      throw new IllegalArgumentException(resourceBundleHolder_.get().getString("unexpected.null"));
    }

    int keyCode = IntegerCodec.decode(encoded, WIDTH);
    for (KeyType keyType : KeyType.values()) {
      if (keyCode == keyType.getId()) {
        return keyType;
      }
    }

    String format = resourceBundleHolder_.get().getString("illegal.value");
    throw new IllegalArgumentException(MessageFormat.format(format, encoded));
  }

  /**
   * Encodes the given {@link KeyType}.
   */
  public static String encode(KeyType keyType) {
    return IntegerCodec.encode(keyType.getId(), WIDTH);
  }
}
