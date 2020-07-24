// Copyright 2020 Intel Corporation
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
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;



@Service
public class OnDieCache {

  private boolean autoUpdate = false;

  private String cacheDir = "";

  private final List<URL> sourceUrl = new ArrayList<URL>();

  private HashMap<String, byte[]> cacheMap = new HashMap<String, byte[]>();

  private final String cacheUpdatedTouchFile = "cache_updated";

  /**
   * Constructor.
   *
   * @param cacheDir cacheDir
   * @param autoUpdate autoUpdate
   * @param sourceUrlList sourceUrlList
   */
  @Autowired
  public OnDieCache(@Value("${sdo.ondiecache.cachedir:#{null}}") final String cacheDir,
                    @Value("${sdo.ondiecache.autoupdate:false}") final boolean autoUpdate,
                    @Value("${sdo.ondiecache.urlsources:}") final String sourceUrlList)
      throws Exception {

    if (sourceUrlList != null && !sourceUrlList.isEmpty()) {
      String[] urls = sourceUrlList.split(",");
      for (String url : urls) {
        this.sourceUrl.add(new URL(url.trim()));
      }
    } else {
      // defaults: the public facing sites containing OnDie artifacts
      this.sourceUrl.add(new URL("https://tsci.intel.com/content/OnDieCA/certs/"));
      this.sourceUrl.add(new URL("https://tsci.intel.com/content/OnDieCA/crls/"));
    }

    if (cacheDir != null) {
      File cache = new File(cacheDir);
      if (!cache.exists()) {
        throw new IOException("OnDieCertCache: cache directory does not exist: " + cacheDir);
      }
      if (!cache.isDirectory()) {
        throw new IOException("OnDieCertCache: cache directory must be a directory: " + cacheDir);
      }

      this.cacheDir = cacheDir;
      this.autoUpdate = autoUpdate;
      if (autoUpdate) {
        // update local cache
        copyFromUrlSources();
      }
      loadCacheMap();
    }
  }


  /**
   * Copy the certs and CRLs from the URL sources to the cache directory.
   *
   */
  private void copyFromUrlSources() {

    File cache = new File(cacheDir);
    if (!cache.exists()) {
      cache.mkdir();
    }

    try {
      for (URL url : this.sourceUrl) {
        URLConnection urlConn = url.openConnection();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
            urlConn.getInputStream()))) {

          // loop through all the href entries and for each .crl and .cer
          // links, download the file and store locally
          Document doc = Jsoup.connect(url.toString()).get();
          Elements elements = doc.select("a[href]");
          if (elements != null) {
            for (Element e : elements) {
              if (e != null) {
                for (Attribute attr : e.attributes()) {
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
            }
          }
        }
      }
    } catch (IOException ex) {
      LoggerFactory.getLogger(getClass()).debug(ex.getMessage(), ex);
    }
  }

  /**
   * Loads the memory map of cache values from the cache directory.
   *
   * @throws Exception if error
   */
  private void loadCacheMap() throws IOException {
    if (cacheDir != null) {
      File cache = new File(cacheDir);

      // Read each file and load into the hashmap
      File[] files = new File(cache.getAbsolutePath()).listFiles();
      if (files != null) {
        for (File file : files) {
          if (!file.isDirectory()) {
            if (file.getName().toLowerCase().endsWith(".crl")
                || file.getName().toLowerCase().endsWith(".cer")) {
              cacheMap.put(file.getName(), Files.readAllBytes(Paths.get(file.getAbsolutePath())));
            }
          }
        }
      }
    }
  }


  private boolean isCacheUpdateNeeded() throws IOException {
    if (cacheDir != null) {
      File cache = new File(cacheDir);

      // check for the "files updated" touch file
      if (Files.exists(Paths.get(cache.getAbsolutePath(), cacheUpdatedTouchFile))) {
        // rename all .new files and remove touch file
        FilenameFilter filter = (dir, name) -> name.endsWith(".new");
        File[] files = new File(cache.getAbsolutePath()).listFiles(filter);
        for (File file : files) {
          File targetFile = new File(file.getAbsolutePath().replaceAll(".new", ""));
          targetFile.delete();
          file.renameTo(targetFile);
          file.delete();
        }
        Files.delete(Paths.get(cacheDir, cacheUpdatedTouchFile));
        return true;
      }
    }
    return false;
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
  public byte[] getCertOrCrl(String pathName) throws IOException, IllegalArgumentException {
    if (isCacheUpdateNeeded()) {
      loadCacheMap();  // initialize cache if not yet initialized
    }

    URL url = new URL(pathName);
    if (url == null) {
      throw new IllegalArgumentException("OnDieCache: illegal crl reference: " + pathName);
    }
    Path path = Paths.get(url.getFile());
    Path fileName = path.getFileName();
    if (fileName == null) {
      throw new IllegalArgumentException("OnDieCache: illegal crl reference: " + pathName);
    }
    return cacheMap.get(fileName.toString());
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

}

