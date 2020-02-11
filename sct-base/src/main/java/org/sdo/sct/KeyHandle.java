// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Arrays;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

/**
 * An indirect handle to a key entry in a {@link KeyStore}.
 *
 * <p>KeyHandles reference an entry in a {@link KeyStore} without loading
 * the private key into memory until it's needed.
 * When the private key is loaded, it is wrapped in an {@link AutoCloseable}
 * to guarantee that an attempt is made to destroy the key material
 * when it's no longer in use.
 */
public class KeyHandle {

  private final KeyStore keyStore;
  private final String alias;

  /**
   * Constructor.
   *
   * @param keyStore The {@link KeyStore} containing the key entry.
   * @param alias    The alias of the key entry.
   */
  public KeyHandle(final KeyStore keyStore, final String alias) {
    this.keyStore = keyStore;
    this.alias = alias;
  }

  /**
   * The alias of the key entry.
   */
  public String getAlias() {
    return this.alias;
  }

  /**
   * Fetch the {@link Certificate} in this key entry.
   */
  public Certificate getCertificate() throws KeyStoreException {
    return keyStore.getCertificate(alias);
  }

  /**
   * Fetch an {@AutoCloseableKey} wrapper of this entry's private key.
   */
  public AutoCloseableKey getKey(PasswordCallbackFunction pwCallback)
      throws GeneralSecurityException {

    char[] pw = pwCallback.getPassword();
    try {
      return new AutoCloseableKey(keyStore.getKey(alias, pw));
    } finally {
      Arrays.fill(pw, '\0');
    }
  }

  /**
   * The {@link KeyStore} containing the key entry.
   */
  public KeyStore getKeyStore() {
    return this.keyStore;
  }

  /**
   * An {@link AutoCloseable} wrapper for a {@link PublicKey}.
   *
   * <p>The wrapper guarantees an attempt to destroy the key material
   * when the key is no longer in use.
   */
  public static class AutoCloseableKey implements AutoCloseable {

    private final Key key;

    public AutoCloseableKey(final Key key) {
      this.key = key;
    }

    @Override
    public void close() {
      if (key instanceof Destroyable) {
        Destroyable d = (Destroyable) key;
        if (!d.isDestroyed()) {
          try {
            d.destroy();
          } catch (DestroyFailedException e) {
            // This is common - most security providers don't implement this.  Ignore it.
          }
        }
      }
    }

    public Key getKey() {
      return this.key;
    }
  }
}
