// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.security.Provider;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * A singleton wrapper for {@link BouncyCastleProvider}.
 */
public class BouncyCastleSingleton {

  public static final Provider INSTANCE = loadInstance();

  private static Provider loadInstance() {
    return new BouncyCastleProvider();
  }
}
