// Copyright 2020 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.domain;

/**
 * Provides shared domain limits to JPA entities.
 */
public abstract class Limits {

  static final int SERIAL_NUMBER_MAXLEN = 128;
  static final int DETAILS_MAXLEN = 2048;
  static final int STATION_NAME_MAXLEN = 64;
  static final int DEVICE_UUID_MAXLEN = 64;
}
