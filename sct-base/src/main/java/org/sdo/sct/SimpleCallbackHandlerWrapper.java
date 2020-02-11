// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Wraps a simple password callback as a JCE CallbackHandler.
 */
public class SimpleCallbackHandlerWrapper implements CallbackHandler {

  private final PasswordCallbackFunction pwCallback;

  /**
   * Constructor.
   *
   * @param pwCallback the wrapped {@link PasswordCallbackFunction}.
   */
  public SimpleCallbackHandlerWrapper(PasswordCallbackFunction pwCallback) {
    this.pwCallback = pwCallback;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
    for (Callback callback : callbacks) {
      if (callback instanceof PasswordCallback) {
        PasswordCallback pcb = (PasswordCallback) callback;
        pcb.setPassword(pwCallback.getPassword());
      } else {
        throw new UnsupportedCallbackException(callback);
      }
    }
  }
}
