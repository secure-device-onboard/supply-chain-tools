// Copyright 2020 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A caching holder for {@link ResourceBundle} objects.
 */
public class ResourceBundleHolder {

  private final String name;
  private ResourceBundle resourceBundle = null;

  /**
   * Constructor.
   *
   * @param name The name of the resource bundle.
   */
  public ResourceBundleHolder(final String name) {
    this.name = name;
  }

  /**
   * Return the cached resource bundle, loading it if necessary.
   *
   * @return the {@link ResourceBundle}
   */
  public ResourceBundle get() {
    if (null == resourceBundle) {
      resourceBundle = ResourceBundle.getBundle(name, Locale.getDefault());
    }
    return resourceBundle;
  }
}
