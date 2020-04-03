// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

/**
 * Values used in sdo.mt_device_state.status database column.
 */
class DeviceStatus {
  public static final int OK = 1;
  public static final int PENDING = 0;
  public static final int FAILED = -1;
  public static final int TIMEOUT = -2;
}
