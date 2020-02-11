// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.domain;

import java.util.NoSuchElementException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaQuery;

import org.sdo.sct.ResourceBundleHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements custom behaviors specific to the {@link ServerSettings} repository.
 */
@Repository
@Transactional(readOnly = true)
public class ServerSettingsRepoImpl implements ServerSettingsRepoCustom {

  private static ResourceBundleHolder resourceBundleHolder_ =
      new ResourceBundleHolder(ServerSettingsRepoImpl.class.getName());

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * {@inheritDoc}
   */
  @Override
  public ServerSettings get() {
    final CriteriaQuery<ServerSettings> query =
        entityManager.getCriteriaBuilder().createQuery(ServerSettings.class);
    query.select(query.from(ServerSettings.class));
    ServerSettings result = entityManager.createQuery(query).getSingleResult();
    if (null == result) {
      throw new NoSuchElementException(
        resourceBundleHolder_.get().getString("server.settings.not.found"));
    }
    return result;
  }
}
