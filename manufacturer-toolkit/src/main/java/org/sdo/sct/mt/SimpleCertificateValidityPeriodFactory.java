// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import java.time.Period;
import org.sdo.sct.domain.ServerSettings;
import org.sdo.sct.domain.ServerSettingsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("unused")
public class SimpleCertificateValidityPeriodFactory implements CertificateValidityPeriodFactory {

  private final ServerSettingsRepo serverSettingsRepo;

  @Autowired
  SimpleCertificateValidityPeriodFactory(ServerSettingsRepo serverSettingsRepo) {
    this.serverSettingsRepo = serverSettingsRepo;
  }

  @Override
  public Period get() {
    ServerSettings settings = serverSettingsRepo.get();
    return Period.parse(settings.getCertificateValidityPeriod());
  }
}
