// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class OnDieCache {

  private final boolean autoUpdate;

  private final String cacheDir;

  private final List<URL> sourceUrl = new ArrayList<URL>();

  private HashMap<String, byte[]> cacheMap = new HashMap<String, byte[]>();

  private boolean initialized;

  /**
   * Constructor.
   *
   * @param cacheDir cacheDir
   * @param autoUpdate autoUpdate
   * @param sourceUrlList sourceUrlList
   */
  @Autowired
  public OnDieCache(@Value("${sdo.ondiecache.cachedir:null}") final String cacheDir,
                    @Value("${sdo.ondiecache.autoupdate:false}") final boolean autoUpdate,
                    @Value("${sdo.ondiecache.urlsources:}") final String sourceUrlList)
      throws Exception {

    if (sourceUrlList != null && !sourceUrlList.isEmpty()) {
      String[] urls = sourceUrlList.split(",");
      for (String url : urls) {
        this.sourceUrl.add(new URL(url));
      }
    } else {
      this.sourceUrl.add(new URL("https://tsci.intel.com/content/OnDieCA/certs/"));
      this.sourceUrl.add(new URL("https://tsci.intel.com/content/OnDieCA/crls/"));
    }

    this.cacheDir = cacheDir;
    this.autoUpdate = autoUpdate;
    if (autoUpdate) {
      // update local cache
      copyFromUrlSources();
    }
    initialized = false;
  }


  /**
   * Copy the certs and CRLs from the URL sources to the cache directory.
   *
   * @throws Exception if error
   */
  private void copyFromUrlSources() throws Exception {

    if (this.cacheDir == null) {
      throw new Exception("OnDieCache: cache directory must be specfied.");
    }
    File cache = new File(cacheDir);
    if (!cache.exists()) {
      cache.mkdir();
    }

    BufferedReader in = null;
    try {
      for (URL url : this.sourceUrl) {
        URLConnection urlConn = url.openConnection();
        in = new BufferedReader(new InputStreamReader(
          urlConn.getInputStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
          if (inputLine.contains("<a href=")) {
            String filename = "";
            if (inputLine.contains(".cer")) {
              filename = inputLine.substring(inputLine.indexOf("href=\"") + 6, inputLine.indexOf(".cer") + 4);
            } else if (inputLine.contains(".crl")) {
              filename = inputLine.substring(inputLine.indexOf("href=\"") + 6, inputLine.indexOf(".crl") + 4);
            }
            if (!filename.isEmpty()) {
              URL fileUrl = new URL(url, filename);
              byte[] fileBytes = fileUrl.openConnection().getInputStream().readAllBytes();
              Files.write(Paths.get(cacheDir, filename), fileBytes);
            }
          }
        }
        in.close();
      }
    } catch (Exception ex) {
      LoggerFactory.getLogger(getClass()).debug(ex.getMessage(), ex);
    } finally {
      in.close();
    }
  }

  /**
   * Loads the memory map of cache values from the cache directory.
   *
   * @throws Exception if error
   */
  private void loadCacheMap() throws IOException {
    if (initialized) {
      return;
    }
    if (cacheDir != null) {
      File cache = new File(cacheDir);
      if (!cache.exists()) {
        throw new IOException("OnDieCertCache: cache directory does not exist: " + cacheDir);
      }
      File[] files = new File(cache.getAbsolutePath()).listFiles();
      for (File file : files) {
        if (!file.isDirectory()) {
          if (file.getName().toLowerCase().endsWith(".cer")
              || file.getName().toLowerCase().endsWith(".crl")) {
            cacheMap.put(file.getName(), Files.readAllBytes(Paths.get(file.getAbsolutePath())));
          }
        }
      }
    }
    initialized = true;
  }

  /**
   * Returns the certificate or CRL corresponding to the specified pathname.
   * The pathname is the full original pathname to the cert file or CRL file.
   * The return value is the byte[] or the cert.
   *
   * @param pathName pathName of cache entry to retrieve
   * @return byte[] cert or crl bytes
   * @throws Exception if error
   */
  public byte[] getCertOrCrl(String pathName) throws IOException {
    if (cacheDir == null) {
      throw new IOException("OnDieCertCache: cache directory not specified.");
    }
    loadCacheMap();  // initialize cache if not yet initialized
    URL url = new URL(pathName);
    Path path = Paths.get(url.getFile());
    return cacheMap.get(path.getFileName().toString());
  }

  /**
   * Returns the number of certs in the cache.
   *
   * @return int the number of certs
   */
  public int getNumOfCerts() {
    int count = 0;
    for (String index : cacheMap.keySet()) {
      if (index.toLowerCase().endsWith(".cer")) {
        count++;
      }
    }
    return count;
  }


  /**
   * Returns the number of CRLs in the cache.
   *
   * @return int the number of CRLs
   */
  public int getNumOfCrls() {
    int count = 0;
    for (String index : cacheMap.keySet()) {
      if (index.toLowerCase().endsWith(".crl")) {
        count++;
      }
    }
    return count;
  }

  /**
   * Returns onlineUpdate setting.
   *
   * @return
   */
  public boolean getAutoUpdate() {
    return this.autoUpdate;
  }
}

