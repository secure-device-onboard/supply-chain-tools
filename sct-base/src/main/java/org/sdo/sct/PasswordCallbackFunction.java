// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

/**
 * A callback for obtaining passwords at runtime.
 */
@FunctionalInterface
public interface PasswordCallbackFunction {
  char[] getPassword();
}
