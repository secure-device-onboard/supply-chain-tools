// Copyright 2020 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class OnDieTest {

  String TEST_PUBKEY = "-----BEGIN PUBLIC KEY-----\n"
    + "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAEICpmzISbMOCBxKcvkQLVAZUE6fbpnEQa\n"
    + "HoO/x1rdMuRCn9pBqfDeqoP8mpR2SspMDTt0pXxvAb+KSZkFbU89cEnqx7zSH64K\n"
    + "dRLe5RdJUy1opGNFtM8CZgsG1wExODBC\n"
    + "-----END PUBLIC KEY-----";


  @Test
  @DisplayName("OnDie cache download test")
  void testOnDieCacheDownload() throws Exception {

    Path tempDir = Paths.get("cachedir-temp");
    try {
      OnDieCache onDieCache = new OnDieCache(
        tempDir.toString(),
        true,
        "https://pre1-tsci.intel.com/content/OD/certs/,https://tsci.intel.com/content/OnDieCA/crls/");

      assertNotNull(onDieCache.getCertOrCrl(
        "https://pre1-tsci.intel.com/content/OD/certs/TGL_00001846_OnDie_CA.crl"));
      assertNull(onDieCache.getCertOrCrl(
        "https://pre1-tsci.intel.com/content/OD/certs/NOT_IN_THE_CACHE.crl"));
      assertThrows(MalformedURLException.class,
        () -> onDieCache.getCertOrCrl("TGL_00001846_OnDie_CA.crl"));
      assertTrue(onDieCache.getNumOfCerts() > 0);
      assertTrue(onDieCache.getNumOfCrls() > 0);
    } catch (Exception ex) {
      throw ex;
    } finally {
      //FileSystemUtils.deleteRecursively(tempDir);
    }
  }

  @Test
  @DisplayName("OnDie cache load test")
  void testOnDieCacheLoad() throws Exception {
    OnDieCache onDieCache = new OnDieCache(
      new ClassPathResource("cachedir").getURL().getPath(),
      false,
      null );

    assertNotNull(onDieCache.getCertOrCrl(
      "https://pre1-tsci.intel.com/content/OD/certs/TGL_00001846_OnDie_CA.crl"));
    assertThrows(MalformedURLException.class,
      () -> onDieCache.getCertOrCrl("TGL_00001846_OnDie_CA.crl"));
    assertTrue(onDieCache.getNumOfCerts() == 0);
    assertTrue(onDieCache.getNumOfCrls() > 0);
  }

}
