// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.util.Iterator;
import java.util.Optional;
import javax.security.auth.DestroyFailedException;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Locates keys in a set of {@link KeyStore}s.
 */
@Service
public class KeyFinder {

  private final KeyStores keyStores;
  private final PasswordCallbackFunction pwCallback;

  /**
   * Constructor.
   *
   * @param keyStores  The {@link KeyStores} to search.
   * @param pwCallback The keystore password callback.
   */
  public KeyFinder(KeyStores keyStores,
      PasswordCallbackFunction pwCallback) {
    this.keyStores = keyStores;
    this.pwCallback = pwCallback;
  }


  /**
   * Finds a key of the given {@link KeyType}.
   *
   * @param keyType The type of key to find.
   *
   * @return A {@link KeyHandle} wrapping the located key, or NULL if not found.
   */
  public Optional<KeyHandle> find(final KeyType keyType) {

    Iterator<KeyStore> it = keyStores.iterator();
    while (it.hasNext()) {
      KeyStore keyStore = it.next();
      String alias = find(keyStore, keyType).orElse(null);
      if (null != alias) {
        return Optional.of(new KeyHandle(keyStore, alias));
      }
    }

    return Optional.empty();
  }

  private Optional<String> find(final KeyStore keyStore, final KeyType keyType) {

    String result = null;
    final Iterator<String> it;
    try {
      it = keyStore.aliases().asIterator();
      while (it.hasNext() && null == result) {
        final String alias = it.next();

        if (isKeyType(keyType, keyStore, alias) && isValidSigningKey(keyStore, alias)) {
          result = alias;
        }
      }
    } catch (KeyStoreException e) {
      LoggerFactory.getLogger(KeyFinder.class).error(e.getMessage(), e);
    }

    return Optional.ofNullable(result);
  }

  boolean isKeyType(KeyType keyType, KeyStore keyStore, String alias) {

    final Certificate cert;
    try {
      cert = keyStore.getCertificate(alias);
    } catch (KeyStoreException e) {
      return false;
    }

    return keyType == KeyUtils.toType(cert.getPublicKey());
  }

  boolean isValidSigningKey(KeyStore keyStore, String alias) {

    KeyStore.PasswordProtection protectionParameter =
        new KeyStore.PasswordProtection(pwCallback.getPassword());

    try {
      KeyStore.Entry entry = keyStore.getEntry(alias, protectionParameter);
      if (entry instanceof PrivateKeyEntry) {
        PrivateKeyEntry pkEntry = (PrivateKeyEntry) entry;
        PublicKey pubKey = pkEntry.getCertificate().getPublicKey();
        String sigAlg = CryptoLevel.of(KeyUtils.toType(pubKey)).getSignatureAlgorithm(pubKey);
        Signature s = Signature.getInstance(sigAlg, keyStore.getProvider());
        s.initSign(pkEntry.getPrivateKey());
        s.sign();
        return true;
      }

    } catch (Exception e) {
      // left empty intentionally
    } finally {
      try {
        protectionParameter.destroy();
      } catch (DestroyFailedException e) {
        // left empty intentionally
      }
    }

    return false;
  }

}
