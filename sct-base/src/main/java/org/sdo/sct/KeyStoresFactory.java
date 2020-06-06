// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStore.CallbackHandlerProtection;
import java.security.KeyStoreException;
import java.security.Provider;
import java.security.Security;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class KeyStoresFactory extends AbstractFactoryBean<KeyStores> {

  private static ResourceBundleHolder resourceBundleHolder_ =
      new ResourceBundleHolder(KeyStoresFactory.class.getName());

  private final PasswordCallbackFunction pwCallback;
  private final Resource resource;

  /**
   * Constructor.
   *
   * <p>The supplied URI can identify:
   * <ul>
   *   <li>A PKCS12 keystore file.
   *   <li>A PKCS11 provider library (DLL/SO).
   * </ul>
   * </p>
   *
   * @param pwCallback the {@link PasswordCallbackFunction} for the KeyStore.
   * @param resource   The config {@link Resource}.
   */
  @Autowired
  public KeyStoresFactory(
      final PasswordCallbackFunction pwCallback,
      @Value("${sdo.keystore:file://C:/xSDO/sdo.p12}") final Resource resource) {

    this.pwCallback = pwCallback;
    this.resource = resource;
  }

  @Override
  public Class<?> getObjectType() {
    return KeyStores.class;
  }

  @Override
  protected KeyStores createInstance() throws IOException, KeyStoreException {

    KeyStores keyStores = createInstancePkcs11();
    if (null == keyStores) {
      keyStores = createInstancePkcs12();
    }

    return keyStores;
  }

  private KeyStore buildPkcs11KeyStore(Provider provider) throws KeyStoreException {

    CallbackHandlerProtection protectionParameter =
        new CallbackHandlerProtection(new SimpleCallbackHandlerWrapper(pwCallback));

    return KeyStore.Builder
      .newInstance("PKCS11", provider, protectionParameter)
      .getKeyStore();
  }

  private Provider buildPkcs11Provider(int slotListIndex) throws IOException {
    String config = "--"
        + "name = PKCS11-" + slotListIndex + "\n"
        + "library = \"" + resource.getFile().toString().replace("\\", "\\\\") + "\"\n"
        + "slotListIndex = " + slotListIndex + "\n";

    return Security.getProvider("SunPKCS11").configure(config);
  }

  private KeyStores createInstancePkcs11() {

    List<KeyStore> keyStores = new ArrayList<>();
    int slotListIndex = 0;

    try {
      do {
        Provider provider = buildPkcs11Provider(slotListIndex++);
        KeyStore keyStore = buildPkcs11KeyStore(provider);
        keyStores.add(keyStore);
      } while (true);
    } catch (Exception e) {
      // left empty intentially
    }

    if (0 < keyStores.size()) {
      String format = resourceBundleHolder_.get().getString("pkcs11.keystores.found");
      LoggerFactory.getLogger(getClass()).info(MessageFormat.format(format, keyStores.size()));

      return keyStores::iterator;

    } else {
      return null;
    }
  }

  private KeyStores createInstancePkcs12() throws IOException, KeyStoreException {

    // For PKCS12, the config is the URI of the keystore file.
    // KeyStore needs to load from a file: source.  If our keystore URI
    // isn't of type file, we must copy it.
    final File keyStoreFile;
    if (resource.isFile()) {
      keyStoreFile = resource.getFile();
    } else {
      try (InputStream in = resource.getInputStream()) {
        keyStoreFile = File.createTempFile("sdo", null);
        keyStoreFile.deleteOnExit();
        try (OutputStream out = new FileOutputStream(keyStoreFile)) {
          in.transferTo(out);
        }
      }
    }

    CallbackHandlerProtection protectionParameter =
        new CallbackHandlerProtection(new SimpleCallbackHandlerWrapper(pwCallback));

    KeyStore keyStore = KeyStore.Builder
        .newInstance("PKCS12",
        BouncyCastleSingleton.INSTANCE,
        keyStoreFile,
        protectionParameter)
        .getKeyStore();

    String format = resourceBundleHolder_.get().getString("pkcs12.keystore.found");
    LoggerFactory.getLogger(getClass()).info(MessageFormat.format(format, keyStore.size()));

    return List.of(keyStore)::iterator;
  }
}
