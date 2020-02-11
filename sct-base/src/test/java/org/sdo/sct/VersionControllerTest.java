// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0
package org.sdo.sct;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VersionControllerTest {

  @Test
  void getVersion_containsKeys() {
    final VersionController vc = new VersionController();
    final String version = vc.get();
    Assertions.assertTrue(version.contains("\"build\":"));
    Assertions.assertTrue(version.contains("\"version\":"));
    Assertions.assertTrue(version.contains("\"timestamp\":"));
  }
}
