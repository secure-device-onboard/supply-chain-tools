// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.domain;

/**
 * Defines custom behaviors specific to the {@link ServerSettings} repository.
 */
public interface ServerSettingsRepoCustom {

  /**
   * Fetches the single entry in the server settings table.
   *
   * <p>Since this table can have only one row, common boilerplate
   * for fetching that row is provided by the repository.
   */
  ServerSettings get();
}
