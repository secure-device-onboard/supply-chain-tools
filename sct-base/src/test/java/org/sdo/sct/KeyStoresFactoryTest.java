// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class KeyStoresFactoryTest {

  @Test
  void test() throws Exception {
    KeyStoresFactory ksf = new KeyStoresFactory(
      "123456"::toCharArray,
      new ClassPathResource("org/sdo/sct/test.p12"));
//    new FileUrlResource("C:\\users\\gbean\\documents\\supply-chain-tools\\sdo-mfr.p12"));
//    new FileUrlResource("C:\\Program Files\\OpenSC Project\\OpenSC\\pkcs11\\opensc-pkcs11.dll"));

    ksf.setSingleton(false);
    KeyStores ks = ksf.getObject();
    assertNotNull(ks);
  }
}
