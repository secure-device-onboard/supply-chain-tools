// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.util.Arrays;

/**
 * The SDO "Hash" data type.
 */
public class Hash {

  private final int code;
  private final int size;
  private final byte[] bytes;

  protected Hash(int code, int size, byte[] bytes) {

    if (null == bytes || size != bytes.length) {
      throw new IllegalArgumentException();
    }

    this.code = code;
    this.size = size;
    this.bytes = Arrays.copyOf(bytes, bytes.length);
  }

  byte[] getBytes() {
    return Arrays.copyOf(bytes, bytes.length);
  }

  int getCode() {
    return code;
  }

  int getSize() {
    return size;
  }

  static class Hmac256 extends Hash {

    Hmac256(byte[] bytes) {
      super(108, 256 / 8, bytes);
    }
  }

  static class Hmac384 extends Hash {

    Hmac384(byte[] bytes) {
      super(114, 384 / 8, bytes);
    }
  }

  public static class Null extends Hash {

    public Null() {
      super(0, 0, new byte[0]);
    }
  }

  static class Sha256 extends Hash {

    Sha256(byte[] bytes) {
      super(8, 256 / 8, bytes);
    }
  }

  static class Sha384 extends Hash {

    Sha384(byte[] bytes) {
      super(14, 384 / 8, bytes);
    }
  }
}
