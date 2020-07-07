// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
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

  private final String cacheUpdatedTouchFile = "cache_updated";

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
      // defaults: the public facing sites containing OnDie artifacts
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

        // loop through all the href entries and for each .crl and .cer
        // links, download the file and store locally
        Document doc = Jsoup.connect(url.toString()).get();
        Elements elements = doc.select("a[href]");
        for (int i = 0; i < elements.size(); i++) {
          Iterator<Attribute> iattr = elements.get(i).attributes().iterator();
          while (iattr.hasNext()) {
            Attribute attr = iattr.next();
            if (attr.getKey().equals("href")) {
              String hrefValue = attr.getValue();
              if (hrefValue.contains(".cer") || hrefValue.contains(".crl")) {
                URL fileUrl = new URL(url, hrefValue);
                byte[] fileBytes = fileUrl.openConnection().getInputStream().readAllBytes();
                Files.write(Paths.get(cacheDir, hrefValue), fileBytes);
              }
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

      // check for the "files updated" touch file
      if (Files.exists(Paths.get(cache.getAbsolutePath(), cacheUpdatedTouchFile))) {
        // rename all .new files and remove touch file
        FilenameFilter filter = (dir, name) -> name.endsWith(".new");
        File[] files = new File(cache.getAbsolutePath()).listFiles(filter);
        for (File file: files) {
          File targetFile = new File(file.getAbsolutePath().replaceAll(".new",""));
          targetFile.delete();
          file.renameTo(targetFile);
          file.delete();
        }
        Files.delete(Paths.get(cacheDir, cacheUpdatedTouchFile));
      }

      // Read each file and load into the hashmap
      File[] files = new File(cache.getAbsolutePath()).listFiles();
      if (files != null) {
        for (File file : files) {
          if (!file.isDirectory()) {
            if (file.getName().toLowerCase().endsWith(".cer")
                || file.getName().toLowerCase().endsWith(".crl")) {
              cacheMap.put(file.getName(), Files.readAllBytes(Paths.get(file.getAbsolutePath())));
            }
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

